Prequisites:
- A database server setup with an ecperf user with password ecperf that
can acommodate the test load.
- A checkout of the ecperf module from sourceforge:
For the 3.0.x versions of JBoss use:
cvs -d :pserver:anonymous@cvs.jboss.sourceforge.net:/cvsroot/jboss -r Branch_3_0 co ecperf

For the 3.2.x and latter versions of JBoss use:
cvs -d :pserver:anonymous@cvs.jboss.sourceforge.net:/cvsroot/jboss co ecperf

The resulting ecperf directory is the ECPERF_HOME location referred to
below.

- Either checkout and build the JBoss version you want, or download a
precompiled binary. The jboss-3.x.y directory in either the build/output
directory of the source module or binary release is the JBOSS_DIST root
location referred to below.

Now, customize the ECPERF_HOME by:

1. Edit ECPERF_HOME/config/jboss.env to point to the right directories and
JDBC paths and correct hostnames. In particular check the following:
JAVA_HOME
J2EE_HOME
DB and the associated JDBC_XXX values
ECPERF_HOST
EMULATOR_HOST

2. Edit ECPERF_HOME/bin/driver.bat correct paths and hostnames. If you are
fortunate enough to use the driver.sh script then the only file you need to
edit is jboss.env of step 1.
3. Edit JBOSS_DIST/server/default/confg/jboss-service.xml so that JNDI runs
under port 1100 so as not to conflict with the rmiregistry 1099 port.
4. Edit ECPERF_HOME/config/run.properties to set correct directories and to
set Transaction Rate (txRate).
5. Edit ECPERF_HOME/build.xml to set SCALE to match txRate.

Next, build the test jars and deploy them:
6. ant
7. ant deploy

Next, populate the test database:
8. ant create-schema (there is a drop-schema if needed)
9. ant loaddb

Next, start the JBoss server
10. export JAVA_OPTS=-Xmx1024m -Xms512m (or whatever for your VM) in the
shell you are running JBoss in. If you fail to do this you will likely see
OutOfMemoryErrors for any reasonble txRate, SCALE settings.
11. cd JBOSS_DIST/bin
12. Execute the run.bat or run.sh startup script


Next, start the ecperf testsuite:
13. cd ECPERF_HOME/bin
14. Execute driver.bat or driver.sh
15. View results in the outDir specified in run.properties on driver's machine

I may have missed some points here.
- Build only works with one DB, meaning you can't partition different tables onto 
different DBs.  You'll have to configure this yourself if you want to test in this way.

To run with clustering.

1. ant clean
2. Edit build.xml:
  <property name="jbossconfig" value="all"/>

  <property name="jdbcsuffix" value="cluster.xml"/>
  <property name="jbosssuffix" value="CMP.CLUSTER"/>
3. in cluster-service.xml, comment out the HAJNDI service.  It is not used.

  <!--
  <mbean code="org.jboss.ha.jndi.HANamingService" 
         name="jboss:service=HAJNDI">
     <depends>jboss:service=DefaultPartition</depends>
  </mbean>
  -->

