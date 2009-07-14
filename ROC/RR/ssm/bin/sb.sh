#!/bin/sh

# 
# Start a SonicMQ JMS client.
# 
# This file sets the JDK and Sonic install directory to
# allow you to run the sample code.  Parameters to this
# file are passed onto the main() method in the java class.
#

# Set common variables
# JRE/JVM and library paths must be set by setenv
SONICMQ_HOME="/work/bling/SonicMQ"
. "$SONICMQ_HOME/bin/setenv"

SONICMQ_CLASSPATH=~/ROC/PP/pinpoint/dist/lib/pinpoint.jar${ps}~/ROC/common/swig-util/dist/lib/swigutil.jar

PUBLISHER=-Droc.pinpoint.tracing.Publisher=roc.pinpoint.tracing.java.TCPObservationPublisher
HOSTNAME=-Droc.pinpoint.publishto.hostname=x1.millennium.berkeley.edu

$SONICMQ_JRE $PUBLISHER $HOSTNAME -Xms1024M -Xmx1024M -classpath ".${ps}$SONICMQ_CLASSPATH" Start b $GEXEC_MY_VNN x1:2506 $* 








