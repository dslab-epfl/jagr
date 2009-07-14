#!/bin/bash

export BASEDIR=$(dirname $0)

cd $BASEDIR

export EXPTTOOLS_HOME=$JAGR_TOP/expttools

ORIGDIR=$(pwd)
EXPTTOOLS_JAR=$EXPTTOOLS_HOME/dist/lib/expttools.jar

cd $EXPTTOOLS_HOME/lib
for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

cd $JBOSS_TOP/console/output/lib
for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

CLASSPATH=$CLASSPATH:$EXPTTOOLS_JAR

cd $ORIGDIR

#echo classpath is $CLASSPATH


#JAVA_OPTS="-Xrunjmp:nomethods $JAVA_OPTS"
 
java $JAVA_OPTS -cp $CLASSPATH roc.jboss.tools.RRFaultInjector $@  




