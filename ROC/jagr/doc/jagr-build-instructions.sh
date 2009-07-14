#!/bin/bash

# $Id: jagr-build-instructions.sh,v 1.7 2004/07/22 00:48:31 candea Exp $

print_envvars() {
    echo ==================================================
    echo CVS_RSH=$CVS_RSH
    echo JAVA_HOME=$JAVA_HOME
    echo PATH=$PATH
    echo TOP=$TOP
    echo ROC_TOP=$ROC_TOP
    echo JAGR_TOP=$JAGR_TOP
    echo JBOSS_TOP=$JBOSS_TOP
    echo JBOSS_HOME=$JBOSS_HOME
    echo JBOSS_DIST=$JBOSS_DIST
    echo ==================================================
}

check_env() {
    if [ -z $2 ]; then
        echo ERROR: $1 is not defined
        exit;
    fi
}

check_program() {
    if [ -z $(which $1 2>/dev/null) ]; then
        echo ERROR: Could not find program $1 on the path
        exit;
    fi
}


#
# 1. check your environment variables
#
print_envvars;

check_env "CVS_RSH" $CVS_RSH;
check_env "JAVA_HOME" $JAVA_HOME;
check_env "TOP" $TOP;
check_env "ROC_TOP" $ROC_TOP;
check_env "JAGR_TOP" $JAGR_TOP;
check_env "JBOSS_TOP" $JBOSS_TOP;
check_env "JBOSS_HOME" $JBOSS_HOME;
check_env "JBOSS_DIST" $JBOSS_DIST;
check_env "PATH" $PATH;

check_program "java";
check_program "javac";
check_program "cvs";

# ant will be checked out of cvs!
#check_program "ant";

if [ -z $(echo $PATH | grep $JBOSS_TOP/tools/bin) ]; then
    echo ERROR: Ant, to be checked out of CVS, will not be in path.  ADD '$JBOSS_TOP/tools/bin' to your PATH
    exit;
fi

#
# 2. setup a working directory
#
if [ -a $ROC_TOP ]; then
  echo $ROC_TOP already exists.  Please remove it first. Exiting...;
  exit;
else
  mkdir --parents $ROC_TOP;
fi

#
# 3. cvs checkout all the important stuff
#
echo Checking out from CVS...
cd $TOP
cvs -d cvs:/home/CVS checkout ROC/jagr/
cvs -d cvs:/home/CVS checkout ROC/common/tools
cvs -d cvs:/home/CVS checkout ROC/common/scripts
cvs -d cvs:/home/CVS checkout ROC/common/swig-util
cvs -d cvs:/home/CVS checkout ROC/PP/pinpoint
cvs -d cvs:/home/CVS checkout ROC/RR/ssm
cvs -d cvs:/home/CVS checkout ROC/RR/rr-rubis/rubis-post-1.4.1

#
# 4. download jboss source code and copy over the jagr diff
#
echo Downloading and installing JBoss...
cd $TOP
rm -f jboss-3.2.1-src-nocvs.tgz
wget crash.stanford.edu/download/jboss-3.2.1-src-nocvs.tgz
cd $JAGR_TOP
mv jagr-jboss/ jagr-jboss-srcdiff
tar -zxvf $TOP/jboss-3.2.1-src-nocvs.tgz
rm -f jboss-3.2.1-src-nocvs.tgz
cp -r jagr-jboss-srcdiff/* jboss-3.2.1-src/
rm -rf jagr-jboss-srcdiff

#
# 5. build JBoss, Pinpoint, and the JAGR Recovery Manager
#    Don't worry about building them in any particular order.  There's
#    no build dependency between these trees
#
echo Building source trees...
cd $JBOSS_TOP/build/
./build.sh

cd $ROC_TOP/PP/pinpoint/build
./build.sh

cd $JAGR_TOP/jagr-recomgr/build
./build.sh

cd $JAGR_TOP/expttools/build
./build.sh

#
# 6. Finish up with some helpful guidance
#
echo Done.  You may want to take the following steps now:
echo --------------------------------------------------
echo cd $ROC_TOP/RR/ssm/roc/rr/ssm
echo edit Brick.java to set the right multicast address
echo cd $ROC_TOP/RR/ssm
echo ant
echo cd $ROC_TOP/RR/rr-rubis/rubis-post-1.4.1/EJB_local_remote_CMP2.0
echo ant rubis_ear
echo --------------------------------------------------
echo See http://crash.stanford.edu/resources/rr-rubis.html for more help.

