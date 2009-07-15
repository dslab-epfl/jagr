# Generic environment needed for the apps server
set JAVA_HOME=C:/jdk1.3
set J2EE_HOME=C:/jboss-3.0.3RC1

# Environment needed for the driver
set CLASSPATH=${J2EE_HOME}/client/jboss-j2ee.jar:${ECPERF_HOME}/jars/ecperf-client.jar
set BINDWAIT=3

# Environment needed for the DB loader
set JDBC_CLASSPATH=${J2EE_HOME}/server/default/deploy/pgjdbc2.jar

# Additional environment needed for make
set JAVAX_JAR=${J2EE_HOME}/client/jboss-j2ee.jar

# The following variables specify the URL of the 
# server in which the ECperf beans are deployed
# ECPERF_PREFIX used to access the Web components 
# For Example DeliveryServlet. Default is /
set ECPERF_HOST=localhost
set ECPERF_PORT=8080
set ECPERF_PREFIX=/

# The following variables specify the URL of the server
# in which the Supplier Emulator is deployed
# EMULATOR_PREFIX used to access the Web components 
# For Example EmulatorServlet. Default is /
set EMULATOR_HOST=localhost
set EMULATOR_PORT=8080
set EMULATOR_PREFIX=/

# needed if the App server host is not specified in the cmd line
set JAVA="$JAVA_HOME/bin/java "

