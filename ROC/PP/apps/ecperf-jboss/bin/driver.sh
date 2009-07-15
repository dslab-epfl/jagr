#!/bin/sh

BINDIR=`dirname $0`

if [ -n "$BINDIR" ]
then
    ECPERF_HOME=`cd $BINDIR/.. > /dev/null 2>&1 && pwd`
    export ECPERF_HOME
fi

# Configuration file location
CONFIG_DIR=${ECPERF_HOME}/config

# We check for the apps server and load the necessary environment
# to make a run
APPSSERVER=`cat ${CONFIG_DIR}/appsserver`
echo "Apps server is ${APPSSERVER}"

ENVFILE=${CONFIG_DIR}/${APPSSERVER}.env

if [ ! -f ${ENVFILE} ] ; then
    echo "Cannot find config/${APPSSERVER}.env" >&2
    exit 1
fi

# Just get JAVA_HOME

JAVA_HOME=`awk -F"=" '{ if ($1 == "JAVA_HOME") { print $2 }}' ${ENVFILE}`

# We check for JAVA_HOME. If not set, we do not
# continue processing.

CHECKPASSED=1
if [ -z "${JAVA_HOME}" ] ; then
    echo "JAVA_HOME not set" >&2
    exit 1
fi

# On X based systems, multiple environment variables are needed for
# the child process. So we just pass everything. This might not be
# needed for win32
shellProps() {
    for i in `env`
    do
	echo "-Denvironment.$i"
    done
}

${JAVA_HOME}/bin/java -classpath ${ECPERF_HOME}/jars/launcher.jar \
    -Djava.compiler=NONE \
    -Droc.pinpoint.tracing.Publisher=roc.pinpoint.tracing.java.TCPObservationPublisher \
    -Droc.pinpoint.publishto.hostname=$OBSERVATION_HOSTNAME \
    -Droc.pinpoint.injection.FaultTriggerFile=$TRIGGERFILE \
    -Decperf.home=${ECPERF_HOME} \
    -Dnode.name=`uname -n` \
    `shellProps` \
  com.sun.ecperf.launcher.Script Driver $*
