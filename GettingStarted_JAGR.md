# IN PREPARATION ... !! #

```
#!/bin/tcsh

#
#            GETTING STARTED WITH JAGR
#
#
# You can run most of these commands by doing
# wget -q -O - crash.stanford.edu/resources/jagr.txt | /bin/tcsh
#

#
# Set up your environment
#
setenv TOP        ${HOME}
setenv ROC_TOP    ${TOP}/ROC
setenv JAGR_TOP   ${ROC_TOP}/jagr
setenv JBOSS_TOP  ${JAGR_TOP}/jboss-3.2.1-src
setenv JBOSS_HOME ${JBOSS_TOP}/build/output/jboss-3.2.1
setenv JBOSS_DIST ${JBOSS_HOME}
setenv JAVA_HOME /usr/java/j2sdk1.4.1_04
setenv J2EE_HOME /usr/java/j2sdkee1.3.1
setenv CVS_RSH ssh

#
# Adjust the PATH for 'java' and 'ant'
#
setenv PATH ${PATH}:${JAVA_HOME}/bin:${JBOSS_TOP}/tools/bin

#
# Setup the work directory
#
mkdir $ROC_TOP

#
# Check out miscellaneous stuff
#
cd $TOP
cvs -d cvs:/home/CVS co ROC/common/tools
cvs -d cvs:/home/CVS co ROC/common/scripts
cvs -d cvs:/home/CVS co ROC/common/swig-util

#
# Download and build JBoss
#
cd $TOP
cvs -d cvs:/home/CVS co ROC/jagr/
rm -f jboss-3.2.1-src-nocvs.tgz
wget crash.stanford.edu/download/jboss-3.2.1-src-nocvs.tgz
cd $JAGR_TOP
mv jagr-jboss/ jagr-jboss-srcdiff
tar -zxvf $TOP/jboss-3.2.1-src-nocvs.tgz
rm -f jboss-3.2.1-src-nocvs.tgz
cd jagr-jboss-srcdiff
cp -r * jboss-3.2.1-src/
rm -rf ../jagr-jboss-srcdiff
cd $JBOSS_TOP
build/build.sh
rehash

#
# Build load generator, recovery manager, etc.
#
cd $ROC_TOP
cd common/swig-util
build/build.sh
cd $JAGR_TOP
jagr-recomgr/build/build.sh
loadgen/build/build.sh
expttools/build/build.sh

#
# Download and build Pinpoint
#
cd $TOP
cvs -d cvs:/home/CVS co ROC/PP/pinpoint
cd $ROC_TOP/PP/pinpoint
build/build.sh

#
# Download and build SSM (modify multicast group manually)
#
cd $TOP
cvs -d cvs:/home/CVS co ROC/RR/ssm
cd $ROC_TOP/RR/ssm/
echo ---------------------------------------------------
echo Change MC_GROUP to the group address for ${USER}
echo ---------------------------------------------------
/usr/bin/emacs +64 roc/rr/ssm/Brick.java
ant

#
# Download and build RUBiS
#
cd $TOP
cvs -d cvs:/home/CVS co ROC/RR/rr-rubis/rubis-post-1.4.1
cd $ROC_TOP/RR/rr-rubis/rubis-post-1.4.1/EJB_local_remote_CMP2.0
echo ---------------------------------------------------------------------------------------
echo Change HTMLFilesPath to ${ROC_TOP}/RR/rr-rubis/rubis-post-1.4.1/ejb_rubis_web
echo ---------------------------------------------------------------------------------------
emacs +25 edu/rice/rubis/beans/servlets/ConfigJBoss.java
ant rubis_ear

#
# Download and set up MySQL
#
cd $JAGR_TOP
wget crash.stanford.edu/download/mysql-server_v4.tgz
wget crash.stanford.edu/download/mysql-datafiles_v1.tgz
tar xvzf mysql-server_v4.tgz
tar xvzf mysql-datafiles_v1.tgz
rm mysql-server_v4.tgz

#
# Start up MySQL, SSM, and JBoss
# (make sure you don't have any other MySQL installed)
#
cd $ROC_TOP/RR/ssm/bin
./brick.sh 1 &
./brick.sh 2 &
./brick.sh 3 &
cd $JAGR_TOP/mysql-max-3.23.58-pc-linux-i686
bin/safe_mysqld &
$JBOSS_HOME/bin/run.sh
```