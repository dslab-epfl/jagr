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

# Set SSL client 
#SONICMQ_SSL_SAMPLES_CLIENT="-DSSL_CA_CERTIFICATES_DIR=$SONICMQ_HOME/certs/CA"

SONICMQ_CLASSPATH=$SONICMQ_LIB/sonic_Client.jar${ps}$SONICMQ_LIB/gnu-regexp-1.0.6.jar${ps}$SONICMQ_LIB/jaxp.jar${ps}$SONICMQ_LIB/xerces.jar${ps}$SONICMQ_LIB/swingall.jar${ps}$SONICMQ_SSL_LIB${ps}$SONICMQ_LIB/tools.jar

PUBLISHER=-Droc.pinpoint.tracing.Publisher=roc.pinpoint.tracing.java.TCPObservationPublisher
HOSTNAME=Droc.pinpoint.publishto.hostname=x1.millennium.berkeley.edu

$SONICMQ_JRE $SONICMQ_SSL_SAMPLES_CLIENT -Xms1024M -Xmx1024M $CLIENT_JVM_ARGS -classpath ".${ps}$SONICMQ_CLASSPATH" Start s $GEXEC_MY_VNN x1:2506 $* 








