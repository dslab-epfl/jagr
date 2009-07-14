=================
AFPI Instructions
=================
$Id: README.txt,v 1.10 2003/09/03 01:04:35 candea Exp $


These instructions are verified to work with:
   RedHat Linux 7.2 (kernel 2.4.9-31)
   JBoss 3.2.1
   Java j2sdk1.4.1_04
   Java j2sdkee1.3.1
   MySQL-3.23.56-1
   MySQL-client-3.23.56-1
   MySQL-connector-java-3.0.8-stable


=======================
INSTALLING JBOSS + AFPI
=======================

1. Make sure MySQL is running on localhost; it must have a database
   called 'afpi', and user 'afpi' with password 'afpi' must be able to
   log into MySQL and modify the 'afpi' database.

2. You'll need a JDBC driver for MySQL; download Connector-J from
   http://crash.stanford.edu/download/mysql-connector-java-3.0.8-stable.tar.gz
   and install as follows:

   % tar xvzf mysql-connector-java-3.0.8-stable.tar.gz
   % cd mysql-connector-java-3.0.8-stable
   % su   (become root)
   root# cp mysql-connector-java-3.0.8-stable-bin.jar $JAVA_HOME/jre/lib/ext
   root# exit (leave the root account)

   For more detailed instructions on installing Connector-J, see
   http://www.mysql.com/documentation/connector-j/index.html#id2800725.
 
3. Download the vanilla JBoss source tree from
   http://crash.stanford.edu/download/jboss-3.2.1-src.tgz

4. Untar the source.  Then set the $JBOSS_TOP environment variable to
   the path that takes to the newly created jboss-3.2.1-src directory
   and set JBOSS_HOME as a function of JBOSS_TOP.  It's best if you
   add these variable definitions to your .cshrc (or equivalent):
   
   % tar xvzf jboss-3.2.1-src.tgz
   % cd jboss-3.2.1-src
   % setenv JBOSS_TOP `pwd`
   % setenv JBOSS_HOME $JBOSS_TOP/build/output/jboss-3.2.1

5. Build JBoss.  It's important to do this prior to applying the AFPI
   patch, because there are circular dependencies between AFPI's 'rr'
   package and the 'jmx' module in JBoss.

   % cd $JBOSS_TOP/build
   % ./build.sh

6. Get the AFPI source for this version of JBoss and splice it into
   your new JBoss source tree:

   % cd /tmp
   % cvs -d swig.stanford.edu:/opt/CVS co ROC/RR/afpi/3.2.1
   % mv ROC/RR/afpi/3.2.1 $JBOSS_TOP/afpi

7. Patch the JBoss source tree with jboss.diff from this directory. To
   apply the patch to your fresh JBoss 3.2.1 tree, do the following:

   % cd $JBOSS_TOP
   % patch -p1 < $JBOSS_TOP/afpi/jboss.diff

8. Build the AFPI code:

   % cd $JBOSS_TOP/afpi
   % ./build.sh all

9. Rebuild the JBoss tree, in order to incorporate the AFPI patch:

   % cd $JBOSS_TOP/build
   % ./build.sh

10.Now you can run JBoss; you must use the 'afpi' configuration to get
   AFPI functionality. You can also deploy your favorite J2EE
   application(s), if you like.  Use the JBoss JMX console to access
   "RR:service=FaultInjectionService" and perform AFPI operations.

   % cd $JBOSS_HOME/bin
   % ./run.sh -c afpi


====================
DEVELOPING AFPI CODE
====================

After modifying AFPI code, rebuild by doing

   % cd $JBOSS_TOP/afpi
   % ./build.sh

If you didn't change AFPI classes that go into run.jar (see the
run.jar target in $JBOSS_TOP/system/build.xml) or interceptors, then
you don't need to restart JBoss; otherwise, you do need to.

If you modified any of the files within the JBoss source (e.g., under
$JBOSS_TOP/jmx), then you need to rebuild JBoss prior to restarting:

   % cd $JBOSS_TOP/build
   % ./build.sh


======
NOTES:
======

The AFPI patch file was generated with the following command line:

   diff --recursive --ignore-space-change --minimal --unified=5 \
        --exclude=build --exclude=output --exclude=\*.log --exclude=afpi \
        --exclude=module.version original.jboss-3.2.1-src jboss-3.2.1-src \
        > $JBOSS_TOP/afpi/jboss_afpi.diff

Please email any problems to rr-afpi-bugs@lists.stanford.edu.