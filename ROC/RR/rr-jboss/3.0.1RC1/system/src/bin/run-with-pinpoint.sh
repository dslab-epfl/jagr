#!/bin/sh

#
#  first, don't forget to start pinpoint analysis engine separately:
#    $PINPOINT_HOME/bin/run-pinpoint.sh $PINPOINT_HOME/conf/rr-jboss-online-monitoring.conf 
#    

## don't trigger any faults; we only use RR faults for these tests
export TRIGGERFILE=

# the hostname where Pinpoint analysis engine is running
export OBSERVATION_HOSTNAME=localhost

# run.sh will pick up these JAVA_OPTS and pass them to jboss
export JAVA_OPTS="-server -Droc.pinpoint.tracing.Publisher=roc.pinpoint.tracing.java.TCPObservationPublisher -Droc.pinpoint.publishto.hostname=$OBSERVATION_HOSTNAME -Droc.pinpoint.injection.FaultTriggerFile=$TRIGGERFILE"

# start up jboss, pass through any command line arguments we received
./run.sh $@
