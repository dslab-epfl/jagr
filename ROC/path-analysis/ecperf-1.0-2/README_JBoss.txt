JBoss/ECPerf Readme

Copyright (c) 2002 CSIRO Australia, http://www.cmis.csiro.au/sact
=================================================================

This distribution contains all the files to build and deploy the 
ECperf 1.0 update 2 test suite for the JBoss 2.4.x application 
server. 

This document lists the basic steps and hints to build and run ECperf
with JBoss. Please refer to the ECperf and JBoss documentation for 
details about ECperf and JBoss.

1. Software Required
   a) This distribution, including all relevant scripts for JBoss
      and ant1.4.1 with optinal support
   b) JBoss 2.4.x with tomcat 3.2.x, download from www.jboss.org/binary.jsp
   d) Java 1.3.x or later, download from http://java.sun.com/j2se
   c) Database with its JDBC driver, such as Oracle 8.1.7
   e) ECperf 1.0 update 2 or later

   (Note: The easiest way to combine this kit and SUN's ECperf kit is
          to unzip the two kits into the same directory, say ecperf.
          The order is that SUN's ECperf kit first and then this kit!!!)

2. Configuration
   (We suppose that all the above software be installed and work well.)

   a) Edit the '${ECPERF_HOME}/config/JBoss.env' file by setting 
      the properties to the correct values for your environment. 
      The key properties that you may need to set are:
        - JAVA_HOME          (Java SDK directory)
        - J2EE_HOME          (JBoss directory)
        - ECPERF_HOME        (ecperf directoy)
        - JDBC_CLASSPATH     (database JDBC jar or zip file)
        - ECPERF_HOST        (hostname of running server) 
        - EMULATOR_HOST      (hostname of running emulator, can be on the same machine as the driver)
        - NAMING_PROVIDER    (default = jnp://ecperf_host:1098)
        (Note: ecperf_host should be the value of ECPERF_HOST;
         1098 is the JBoss JNDI port number, which should be consistent
         to the JNDI port specified in ${J2EE_HOME}/jboss/conf/tomcat/jboss.jcml.)

   b) Edit the following properties files in '${ECPERF_HOME}/config' 
      for database population:
        - corpdb.properties
        - mfgdb.properties
        - ordsdb.properties
        - suppdb.properties
        Here is an example for Oracle:
        #
        # For Oracle thin driver
        # 
        dbURL = jdbc:oracle:thin:@db_hostname:1521:ecperf
        dbDriver = oracle.jdbc.driver.OracleDriver
        jdbcVersion = 2

   c) Edit the 'J2EE_HOME/jboss/conf/tomcat/jboss.jcml' to 
      configuring ecperf datasource. The following is the 
      example of Oracle datasource for JBoss:
      <mbean code="org.jboss.jdbc.XADataSourceLoader" name="DefaultDomain:service=XADataSource,name=ECperf">
         <attribute name="PoolName">ECperf</attribute>
         <attribute name="DataSourceClass">org.jboss.pool.jdbc.xa.wrapper.XADataSourceImpl</attribute>
         <attribute name="URL">jdbc:oracle:thin:@db_hostname:1521:ecperf</attribute>
         <attribute name="JDBCUser">ecperf</attribute>
         <attribute name="Password">ecperf</attribute>
         <attribute name="MaxSize">50</attribute>
         <attribute name="PSCacheSize">10000</attribute>
         <attribute name="TransactionIsolation">TRANSACTION_READ_COMMITTED</attribute>
      </mbean>

3. Building
   a) Go to '${ECPERF_HOME}/build' and edit the setenv.bat to fit your systems.
   b) run setenv.bat
   c) Type 'ant' to build the EJB jar/war/ear files and driver scripts.
      The outputs that you need to know:
      	- ecperf.ear and emulator.ear in '${ECPERF_HOME}/dist/jboss'
	- JBoss_driver.bat in '${ECPERF_HOME}/bin'

4. Deploying
   (Assuming you have installed the JBoss on the ecperf 
    server machine and the emulator [can be the driver] machine)
 
   a) Copy 'jars/xerces.jar' to '${J2EE_HOME}/jboss/lib/ext' 
      on the server machine and driver machine. 
   b) Copy the ecperf.ear to the '${J2EE_HOME}/jboss/deploy' on the server machine.
   c) Copy the emulator.ear to the '${J2EE_HOME}/jboss/deploy on the emulator (driver) machine.
   d) Copy the jar files under '${ECPERF_HOME}/jars' to '${ECPERF_HOME}/jars' on the
      driver machine (For performance testing).

5. To run ECperf server
   (We suppose that you create and configure the ecperf database properly.)
   a) Start the database
   c) Load the ECperf database. For NT or W2K, 
      go to ${ECperf_HOME}/bin to load the database by: loadbd.nt scale 
      (Note: scale = txRate in run.properties, default = 1)
   b) Start JBoss (ecperf server) with tomcat on the server machine (${J2EE_HOME}/jboss/bin/run_with_tomcat
   b) Start JBoss (emulator) with tomcat on the driver machine (${J2EE_HOME}/jboss/bin/run_with_tomcat

5. Functional Test
   (It is always a good idea to perform functional test before running performance tests.)
   
   To access http://ecperf_host:8080/ECperf/index.html using IE 
   or Netscape on any machines connected to the intranet.
   You should see the first page of ECperf webpage. You can test different 
   functions in different domains by following the links on the page.

6. Performance Test (W2k/NT)
   (We suppose that the functional test goes well.)
   a) Synchronize the clocks on the all machines involved in ecperf test as closely as you can.
 
   b) Edit ${ECperf_HOME}/config/run.properties on the driver machine.
      The properties that you may need to set are:
        - txRate        (You 'd better increase it gradually, say 1, 2, 4, 8 etc.)
        - scaleFactor   ( scaleFactor = txRate)
        - stdyState     ( running time for test, stdyState >= 18000 for formal tests)
   c) Repeat (c) in Step 5 using the scaleFactor set in the above run.properties. 
      (Note: The database must be reloaded for each test run.) 
   d) Start the test driver on the driver machine by running ${ECperf_HOME}/bin/jboss_driver.bat.
      Note: This consists of 3 steps to run the performance test.
            Step 1. start one window where rmiregistry is started. 
            Step 2. start three windows where three loader agents are started and registered with rmiregistry. 
            Step 3. start the test
            To ensure each step OK, wait about 5 seconds before pressing enter on the original command window.

7. Performance Tunning Tips (W2k/NT)
   (We suppose that performance test go well except for that there are a few requirements failed in
    Mfg.summary and Orders.summary under $ECPERF_HOME/bin/output/xx, where xx is your run ID.)
 
   a) Update ${J2EE_HOME}/jboss/conf/tomcat/standardjboss.xml on the sever machine as follows:
        - Change the all commit-option to B, for A is not allowed by ECperf and C is too slow.
        - Enhance reuse by increasing 'container-pool size'
        - Reduce the frequency of ejbPassivating and ejbActivating 
          by increasing 'bean age' and 'resize period'.
        - Turn off log by setting `log4j.appender.Default.Threshold=INFO' 
          in ${J2EE_HOME}/jboss/conf/tomcat/log4j.xml on all machines.
   b) Increase heap sizes for both sever and driver.
   c) Increase the number of database connections (for both the database and the JBoss datasource).
   d) In order to be really J2EE compliant, pass by reference isn't allowed.
   e) Set optimised invoker to false in standardjboss.xml as follows:
      <container-invoker-conf>
        <RMIObjectPort>4444</RMIObjectPort>
        <Optimized>False</Optimized>
        </container-invoker-conf>
   Please refer to standardjboss.xml in ${ECPERF_HOME}/etc/config/tomcat for your system.
 
To get the best BBops/min: Keep on increasing txRate until something fails in the test.
The highest txRate which passes will give you the best BBops/min.


Good Luck!
