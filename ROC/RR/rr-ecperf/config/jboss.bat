rem Generic environment needed for the apps server
set JAVA_HOME=C:/jdk1.3
set J2EE_HOME=C:/jboss-3.0.3RC1

rem Environment needed for the driver
set CLASSPATH=${J2EE_HOME}/client/jboss-j2ee.jar:${ECPERF_HOME}/jars/ecperf-client.jar
set BINDWAIT=3

rem Environment needed for the DB loader
set JDBC_CLASSPATH=${J2EE_HOME}/server/default/deploy/pgjdbc2.jar

rem Additional environment needed for make
set JAVAX_JAR=${J2EE_HOME}/client/jboss-j2ee.jar

rem The following variables specify the URL of the 
rem server in which the ECperf beans are deployed
rem ECPERF_PREFIX used to access the Web components 
rem For Example DeliveryServlet. Default is /
set ECPERF_HOST=localhost
set ECPERF_PORT=8080
set ECPERF_PREFIX=/

rem The following variables specify the URL of the server
rem in which the Supplier Emulator is deployed
rem EMULATOR_PREFIX used to access the Web components 
rem For Example EmulatorServlet. Default is /
set EMULATOR_HOST=localhost
set EMULATOR_PORT=8080
set EMULATOR_PREFIX=/

rem needed if the App server host is not specified in the cmd line
set JAVA="$JAVA_HOME/bin/java "


