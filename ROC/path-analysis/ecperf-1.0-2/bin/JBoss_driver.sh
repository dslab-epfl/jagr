#!/bin/sh

export J2EE_HOME=/work/emrek/path-analysis/jboss/build/output/jboss-3.0.3

export ECPERF_HOME=/work/emrek/path-analysis/ecperf-1.0-2
export JAVA_HOME=/usr/local/j2sdk1.4.0

export ECPERF_HOST=x4.millennium.berkeley.edu
export ECPERF_PORT=8080

export EMULATOR_HOST=x6.millennium.berkeley.edu
export EMULATOR_PORT=8080

export JNDI_CLASS=org.jnp.interfaces.NamingContextFactory
export NAMING_PROVIDER=jnp://x4.millennium.berkeley.edu:1099

###
### set classpath for J2EE server and XML
###
export SERVER_HOME=$ECPERF_HOME
#export SERVER_CLASSPATH=$J2EE_HOME/lib/ext/jboss-j2ee.jar:$J2EE_HOME/client/jboss-common-client.jar:$J2EE_HOME/lib/ext/xerces.jar:$J2EE_HOME/client/jnp-client.jar

for i in $(ls $J2EE_HOME/client/*.jar); do \
  SERVER_CLASSPATH=$SERVER_CLASSPATH:$i; done

###
### set classpath for ECperf clients
###
export CLASSPATH=$ECPERF_HOME/jars/ecperf-client.jar:$SERVER_CLASSPATH:$ECPERF_HOME/jars/driver.jar

###
### set classpath for J2EE clients
###
export CLASSPATH=$CLASSPATH:$J2EE_HOME/client/jndi.jar:$J2EE_HOME/client/jboss-client.jar
export CLASSPATH=$CLASSPATH:$J2EE_HOME/client/jnp-client-client.jar


export APPSSERVER=$ECPERF_HOST
echo Apps server is: $APPSERVER

export SUT_MACHINE=$ECPERF_HOST
export DRIVER_MACHINE=localhost

export CONFIG_DIR=$ECPERF_HOME/config
export DRIVER_POLICY=$CONFIG_DIR/security/driver.policy
export DRIVER_PACKAGE=com.sun.ecperf.driver

echo Driver Host: $DRIVER_MACHINE
echo Application Server Host is: $SUT_MACHINE

###
### ---------------------------------------------------------------- **
###  Section 3 - The following code starts the driver.  There are
###    four steps.
###
###    1. Start the RMI Registry
###    2. Start the Controller
###    3. Start the driver modules
###    4. Start the Driver
$JAVA_HOME/bin/rmiregistry &

##sleep 1

echo Classpath is $CLASSPATH

$JAVA_HOME/bin/java -Djava.security.policy=$DRIVER_POLICY $DRIVER_PACKAGE.ControllerImpl &

sleep 10

$JAVA_HOME/bin/java -Djava.naming.factory.initial=$JNDI_CLASS -Djava.naming.provider.url=$NAMING_PROVIDER -Djava.security.policy=$DRIVER_POLICY  -Dorg.omg.CORBA.ORBInitialHost=$SUT_MACHINE  $DRIVER_PACKAGE.MfgAgent $CONFIG_DIR/agent.properties M1 $DRIVER_MACHINE &

$JAVA_HOME/bin/java -Djava.naming.factory.initial=$JNDI_CLASS -Djava.naming.provider.url=$NAMING_PROVIDER -Djava.security.policy=$DRIVER_POLICY  -Dorg.omg.CORBA.ORBInitialHost=$SUT_MACHINE  $DRIVER_PACKAGE.LargeOLAgent $CONFIG_DIR/agent.properties L1 $DRIVER_MACHINE &

$JAVA_HOME/bin/java -Djava.naming.factory.initial=$JNDI_CLASS -Djava.naming.provider.url=$NAMING_PROVIDER -Djava.security.policy=$DRIVER_POLICY -Dorg.omg.CORBA.ORBInitialHost=$SUT_MACHINE  $DRIVER_PACKAGE.OrdersAgent $CONFIG_DIR/agent.properties O1 $DRIVER_MACHINE &

sleep 10

$JAVA_HOME/bin/java -Djava.naming.factory.initial=$JNDI_CLASS -Djava.naming.provider.url=$NAMING_PROVIDER $DRIVER_PACKAGE.Driver $CONFIG_DIR/run.properties





