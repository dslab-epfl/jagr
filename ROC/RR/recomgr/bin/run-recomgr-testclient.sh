#!/bin/bash

export BASEDIR=$(dirname $0)

cd $BASEDIR

export RECOMGR_HOME=$HOME/recomgr

ORIGDIR=$(pwd)
RECOMGR_JAR=$RECOMGR_HOME/dist/lib/recomgr.jar

cd $RECOMGR_HOME/lib

for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

CLASSPATH=$CLASSPATH:$RECOMGR_JAR
cd $ORIGDIR

java $JAVA_OPTS -Xms128M -Xmx512M -cp $CLASSPATH roc.recomgr.test.TestClient $@  




