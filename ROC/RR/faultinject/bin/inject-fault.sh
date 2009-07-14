#!/bin/bash

export BASEDIR=$(dirname $0)

cd $BASEDIR

ORIGDIR=$(pwd)
EXPTTOOLS_JAR=$HOME/faultinject/dist/lib/faultinject.jar

cd $HOME/faultinject/lib
for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

CLASSPATH=$CLASSPATH:$EXPTTOOLS_JAR

cd $ORIGDIR

java -Denv.log4j=${HOME}/faultinject/conf/log4j.cfg \
     $JAVA_OPTS -cp $CLASSPATH roc.faultinject.InjectSingleFault $@  




