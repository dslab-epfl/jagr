#!/bin/sh

source config.env

ORIGDIR=$(pwd)
PINPOINT_JAR=$PINPOINT_HOME/output/pinpoint.jar

cd $PINPOINT_HOME/lib

for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

cd $JBOSS_HOME/client

for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

CLASSPATH=$CLASSPATH:$PINPOINT_JAR



cd $ORIGDIR

#echo classpath is $CLASSPATH


#JAVA_OPTS="-Xrunjmp:nomethods $JAVA_OPTS"
 
java $JAVA_OPTS -Xmx128M -cp $CLASSPATH roc.pinpoint.analysis.AnalysisEngine $@

