@ECHO OFF
set J2EE_HOME=@J2EE_HOME@

set ECPERF_HOME=@ECPERF_HOME@
set JAVA_HOME=@JAVA_HOME@

set ECPERF_HOST=@ECPERF_HOST@
set ECPERF_PORT=@ECPERF_PORT@

set EMULATOR_HOST=@EMULATOR_HOST@
set EMULATOR_PORT=@EMULATOR_PORT@

set JNDI_CLASS=@JNDI_CLASS@
set NAMING_PROVIDER=@NAMING_PROVIDER@

rem
rem set classpath for J2EE server and XML
rem
set SERVER_HOME=%ECPERF_HOME%
set SERVER_CLASSPATH=%J2EE_HOME%\lib\ext\jboss-j2ee.jar;%J2EE_HOME%\lib\ext\xerces.jar

rem
rem set classpath for ECperf clients
rem
set CLASSPATH=%ECPERF_HOME%\jars\ecperf-client.jar;%SERVER_CLASSPATH%;%ECPERF_HOME%\jars\driver.jar

rem
rem set classpath for J2EE clients
rem
set CLASSPATH=%CLASSPATH%;%J2EE_HOME%\client\jndi.jar;%J2EE_HOME%\client\jboss-client.jar
set CLASSPATH=%CLASSPATH%;%J2EE_HOME%\client\jnp-client-client.jar


set APPSSERVER=%ECPERF_HOST%
echo Apps server is: %APPSERVER%

set SUT_MACHINE=%ECPERF_HOST%
set DRIVER_MACHINE=localhost

set CONFIG_DIR=%ECPERF_HOME%\config
set DRIVER_POLICY=%CONFIG_DIR%\security\driver.policy
set DRIVER_PACKAGE=com.sun.ecperf.driver

echo Driver Host: %DRIVER_MACHINE%
echo Application Server Host is: %SUT_MACHINE%

REM
REM ---------------------------------------------------------------- **
REM  Section 3 - The following code starts the driver.  There are
REM    four steps.
REM
REM    1. Start the RMI Registry
REM    2. Start the Controller
REM    3. Start the driver modules
REM    4. Start the Driver
start %JAVA_HOME%\bin\rmiregistry
Pause

start %JAVA_HOME%\bin\java -Djava.security.policy=%DRIVER_POLICY% %DRIVER_PACKAGE%.ControllerImpl
Pause

start %JAVA_HOME%\bin\java -Djava.naming.factory.initial=%JNDI_CLASS% -Djava.naming.provider.url=%NAMING_PROVIDER% -Djava.security.policy=%DRIVER_POLICY%  -Dorg.omg.CORBA.ORBInitialHost=%SUT_MACHINE%  %DRIVER_PACKAGE%.MfgAgent %CONFIG_DIR%/agent.properties M1 %DRIVER_MACHINE%

start %JAVA_HOME%\bin\java -Djava.naming.factory.initial=%JNDI_CLASS% -Djava.naming.provider.url=%NAMING_PROVIDER% -Djava.security.policy=%DRIVER_POLICY%  -Dorg.omg.CORBA.ORBInitialHost=%SUT_MACHINE%  %DRIVER_PACKAGE%.LargeOLAgent %CONFIG_DIR%/agent.properties L1 %DRIVER_MACHINE%

start %JAVA_HOME%\bin\java -Djava.naming.factory.initial=%JNDI_CLASS% -Djava.naming.provider.url=%NAMING_PROVIDER% -Djava.security.policy=%DRIVER_POLICY% -Dorg.omg.CORBA.ORBInitialHost=%SUT_MACHINE%  %DRIVER_PACKAGE%.OrdersAgent %CONFIG_DIR%/agent.properties O1 %DRIVER_MACHINE%
Pause

%JAVA_HOME%\bin\java -Djava.naming.factory.initial=%JNDI_CLASS% -Djava.naming.provider.url=%NAMING_PROVIDER% %DRIVER_PACKAGE%.Driver %CONFIG_DIR%/run.properties

:exit
