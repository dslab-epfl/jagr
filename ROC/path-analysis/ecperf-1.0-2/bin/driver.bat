@ECHO OFF
REM 
REM --------------------------------------------------------------- **
REM The following batch file is used to drive a workload against a 
REM system that is running the ECPerf Benchmark.  
REM
REM This batch file is divided into three sections.  First, are
REM environment variables that are site dependent and must be 
REM modified.  Suggested values and resonable defaults are 
REM provided where appropriate.  Second is a section that defines
REM environment variables that are rooted off of variables from the
REM first section.  These should not have to be modified unless 
REM there is something special about your setup.  Finally, the actual
REM runtime portion of the script is outlined.  For specific 
REM information on the actual running of the script see the prolog
REM of Section 3.
REM
REM What you need to do to run this script:
REM
REM  1. Set the environment variables for your installation in 
REM     section 1.
REM
REM  2. Verify the settings in section 2 are adequate for your 
REM     installation.
REM 
REM  3. Start the driver.
REM 
REM Script change activity:
REM  Author      Date        Modification
REM  $Hogstrom - 3/31/2001 - Creation of script.
REM 
REM ----------------------------------------------------------------- **
REM  Section 1. Specify environment specific variables.
REM 
REM This is where you unloaded the ECPerf kit to.  This is a suggested place.  If you have used another
REM drive or path please update as appropriate.
set ECPERF_HOME=C:\ECPerf
REM
REM This is where you specify your java and j2ee products are installed.  We'll assume
REM that these directories are.
set JAVA_HOME=C:\JDK1.3

If Not Exist %JAVA_HOME% (
  Echo JAVA_HOME %JAVA_HOME% not defined or does not exist.  Please specify to continue.
  goto exit )
  
set J2EE_HOME=C:\J2SDKEE1.2.1

If Not Exist %J2EE_HOME% (
  Echo J2EE_HOME %J2EE_HOME% not defined or does not exist.  Please specify to continue.
  goto exit )
  
REM
REM Specify the SUT here.  If you are driving a workload on the same system then this
REM is fine.
set ECPERF_HOST=localhost
set ECPERF_PORT=8000

REM The following variables specify the URL of the server
REM in which the Supplier Emulator is deployed.
set EMULATOR_HOST=localhost
set EMULATOR_PORT=8000

REM 
REM JNDI Naming factory class.  This class is used to resolve JNDI requests.  This class is 
REM specific to each application server.
REM
REM Here is a list of known classes.  See your appserver doc or get the latest 
REM kit for information:
REM 
REM WebSphere - com.ibm.ejs.ns.jndi.CNInitialContextFactory
REM WebLogic  - weblogic.jndi.WLInitialContextFactory
set JNDI_CLASS="None Specified"
If %JNDI_CLASS%=="None Specified" (
  Echo You must specify a JNDI class for the driver to work.
  Echo This class is specific to the application server you are
  Echo using.  Please refer to your products documentation for the 
  Echo correct value.  
  Echo 
  Echo For example:
  Echo    WebSphere - com.ibm.ejs.ns.jndi.CNInitialContextFactory
  Echo    WebLogic  - weblogic.jndi.WLInitialContextFactory
  Echo 
  Echo Update the JNDI_CLASS environment variable and restart the driver.
  goto exit )

    
REM
REM
REM The naming provider is the hostname that is being used for JNDI registration.
REM
REM For example, to specify localhost use the following URL iiop://localhost:900    
set NAMING_PROVIDER="None Specified"
If %NAMING_PROVIDER%=="None Specified" (
  Echo You must specify a JNDI naming provider.class for the driver to work.
  Echo For example, to specify localhost use the following URL iiop://localhost:900
  Echo Current setting is NAMING_PROVIDER=%NAMING_PROVIDER%
  goto exit )

REM
REM Note that the first jar should be replaced with the one you have your beans
REM deployed into.
REM
REM              This one
REM                  |
REM                  |
REM                  \/
set CLASSPATH=%ECPERF_HOME%\jars\ECPerf.jar;%J2EE_HOME%\lib\j2ee.jar;%ECPERF_HOME%\jars\ecperf-client.jar;%ECPERF_HOME%\jars\driver.jar

REM
REM The following variable is used to set Application Server properties.  The drivers will
REM look in %ECPERF_HOME%/config/ for the file specified.  Place any AppServer specific 
REM environment variables here.
set APPSSERVER="No App Server Specified"
echo "Apps server is %APPSSERVER%"


REM
REM This variable specifies the System Under Test where ECPerf is running.
set SUT_MACHINE=localhost

REM
REM The following variable is used to set Application Server properties.  The drivers will
REM look in %ECPERF_HOME%/config/ for the file specified.  Place any AppServer specific 
REM environment variables here.
set DRIVER_MACHINE=localhost

REM
REM  --------------------------------------------------------------- **
REM   Section 2.  Environment variables that should not have to be 
REM               changed.
REM
REM
set CONFIG_DIR=%ECPERF_HOME%\config
set DRIVER_POLICY=%CONFIG_DIR%\security\driver.policy
set DRIVER_PACKAGE=com.sun.ecperf.driver
set ENVFILE=%CONFIG_DIR%\%APPSSERVER%.env


echo "Driver Host:"%DRIVER_MACHINE%
echo "Application Server Host:"%SUT_MACHINE%

REM
REM ---------------------------------------------------------------- **
REM  Section 3 - The following code starts the driver.  There are 
REM    four steps.
REM 
REM    1. Start the RMI Registry
REM    2. Start the Controller
REM    3. Start the driver modules
REM    4. Start the Driver
start rmiregistry
Pause
start %JAVA_HOME%\bin\java -Djava.security.policy=%DRIVER_POLICY% %DRIVER_PACKAGE%.ControllerImpl
Pause

start %JAVA_HOME%\bin\java -Djava.naming.factory.initial=%JNDI_CLASS% -Djava.naming.provider.url=%NAMING_PROVIDER% -Djava.security.policy=%DRIVER_POLICY%  -Dorg.omg.CORBA.ORBInitialHost=%SUT_MACHINE%  %DRIVER_PACKAGE%.MfgAgent %CONFIG_DIR%/agent.properties M1 %DRIVER_MACHINE%

start %JAVA_HOME%\bin\java -Djava.naming.factory.initial=%JNDI_CLASS% -Djava.naming.provider.url=%NAMING_PROVIDER% -Djava.security.policy=%DRIVER_POLICY%  -Dorg.omg.CORBA.ORBInitialHost=%SUT_MACHINE%  %DRIVER_PACKAGE%.LargeOLAgent %CONFIG_DIR%/agent.properties L1 %DRIVER_MACHINE%

start %JAVA_HOME%\bin\java -Djava.naming.factory.initial=%JNDI_CLASS% -Djava.naming.provider.url=%NAMING_PROVIDER% -Djava.security.policy=%DRIVER_POLICY% -Dorg.omg.CORBA.ORBInitialHost=%SUT_MACHINE%  %DRIVER_PACKAGE%.OrdersAgent %CONFIG_DIR%/agent.properties O1 %DRIVER_MACHINE%
Pause

%JAVA_HOME%\bin\java -Djava.naming.factory.initial=%JNDI_CLASS% -Djava.naming.provider.url=%NAMING_PROVIDER% %DRIVER_PACKAGE%.Driver %CONFIG_DIR%/run.properties

:exit
