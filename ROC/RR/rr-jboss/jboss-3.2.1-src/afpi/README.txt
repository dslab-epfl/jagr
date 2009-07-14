
=================
AFPI Instructions
=================
$Id: README.txt,v 1.5 2004/02/07 02:24:35 candea Exp $


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
 
3. Download the JBoss source tree from
   http://crash.stanford.edu/download/jboss-3.2.1-src-nocvs.tar.gz
   (this is vanilla JBoss with the left-over CVS directories removed)

4. Untar the source.  Then set the $JBOSS_TOP environment variable to
   the path that takes to the newly created jboss-3.2.1-src directory
   and set JBOSS_HOME as a function of JBOSS_TOP.  It's best if you
   add these variable definitions to your .cshrc (or equivalent):
   
   % tar xvzf jboss-3.2.1-src-nocvs.tar.gz
   % cd jboss-3.2.1-src
   % setenv JBOSS_TOP `pwd`
   % setenv JBOSS_HOME $JBOSS_TOP/build/output/jboss-3.2.1

5. Get the RR-AFPI modifications from CVS into the JBoss tree:

   % cd /tmp
   % cvs -d swig:/opt/CVS co ROC/RR/rr-jboss/jboss-3.2.1-src
   % cd ROC/RR/rr-jboss/jboss-3.2.1-src
   % cp -rf * $JBOSS_TOP

6. Build the JBoss tree:

   % cd $JBOSS_TOP/build
   % ./build.sh

7. Now you can run JBoss. You can also deploy your favorite J2EE
   application(s), if you like.  Use the JBoss JMX console to access
   "RR:service=FaultInjectionService" and perform AFPI operations.

   % cd $JBOSS_HOME/bin
   % ./run.sh


====================
DEVELOPING AFPI CODE
====================

If you only modify AFPI classes that reside under $JBOSS_TOP/afpi, it is
sufficient to do:

   % cd $JBOSS_TOP/afpi
   % ./build.sh

If you modified any of the files within the JBoss source, then you need to
rebuild all of JBoss:

   % cd $JBOSS_TOP/build
   % ./build.sh


======
NOTES:
======

Please email any problems to rr-afpi-bugs@lists.stanford.edu.
