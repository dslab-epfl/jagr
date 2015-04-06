# Introduction #

Add your content here.


# Details #

# IN PREPARATION ... !! #

## Downloads ##

These instructions were verified to work on the following platform:

  * RedHat Linux 9.0
  * Java j2sdk1.4.1\_04 [download](download.md)
  * Java j2sdkee1.3.1 [download](download.md)

## Installation Steps ##
# Everything will be downloaded and installed in a directory; we'll refer to it as $TOP.
# This example assumes you are using the tcsh shell and the directory for RR-JBoss is
# $HOME/work. First, set your environment up (you can put this in your ~/.cshrc):
setenv TOP ~/work
setenv JBOSS_HOME $TOP/jboss-3.2.1-src/build/output/jboss-3.2.1
setenv JAVA_HOME /usr/java/j2sdk1.4.1_04
setenv J2EE_HOME /usr/java/j2sdkee1.3.1
setenv PATH ${PATH}:${JAVA_HOME}/bin

#
# Download JBoss in $TOP:
#
mkdir $TOP
cd $TOP
wget crash.stanford.edu/download/jboss-3.2.1-src-nocvs.tgz
tar xvzf jboss-3.2.1-src-nocvs.tgz
rm jboss-3.2.1-src-nocvs.tgz
                
#
# Check out from CVS the RR-specific changes and then build:
#
setenv CVS_RSH ssh
cd /tmp
rm -rf ROC
cvs -d cvs:/home/CVS co ROC/RR/rr-jboss/jboss-3.2.1-src
cd $TOP/jboss-3.2.1-src
cp -r /tmp/ROC/RR/rr-jboss/jboss-3.2.1-src/* . 
cd build
./build.sh
                
#
# Start up RR-JBoss:
#
$JBOSS_HOME/bin/run.sh
              ```