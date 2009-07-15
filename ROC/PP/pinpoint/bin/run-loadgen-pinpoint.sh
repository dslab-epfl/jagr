#!/bin/sh

export BASEDIR=$(dirname $0)

cd $BASEDIR

source config.env

ORIGDIR=$(pwd)
PINPOINT_JAR=$PINPOINT_HOME/dist/lib/pinpoint.jar

cd $PINPOINT_HOME/lib

for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

CLASSPATH=$CLASSPATH:$PINPOINT_JAR
cd $ORIGDIR

#echo classpath is $CLASSPATH


#JAVA_OPTS="-Xrunjmp:nomethods $JAVA_OPTS"

JAVA_OPTS="-Droc.pinpoint.tracing.Publisher=roc.pinpoint.tracing.java.TCPObservationPublisher"
 
java $JAVA_OPTS -Xss128M -Xmx512M -cp $CLASSPATH roc.loadgen.Engine ../conf/loadgen/PinpointLoad.conf $@  




