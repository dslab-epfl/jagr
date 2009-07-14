#!/bin/bash

export BASEDIR=$(dirname $0)

cd $BASEDIR

export RECOMGR_HOME=$HOME/recomgr

ORIGDIR=$(pwd)
RECOMGR_JAR=$RECOMGR_HOME/dist/lib/recomgr.jar

# cd $RECOMGR_HOME/lib
# for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

cd $RECOMGR_HOME/lib
for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

CLASSPATH=$CLASSPATH:$RECOMGR_JAR

cd $ORIGDIR

# java $JAVA_OPTS -Xss128M -Xmx512M -cp $CLASSPATH roc.recomgr.RecoveryManager $@  
java -Denv.log4j=${RECOMGR_HOME}/conf/log4j.cfg \
     -Drecomgr.bindir=${RECOMGR_HOME}/bin \
     $JAVA_OPTS -Xms128M -Xmx512M -cp $CLASSPATH roc.recomgr.RecoveryManager \
     worker=roc.recomgr.worker.ReportWorker \
     policy=roc.recomgr.policy.SimpleMicrorebootPolicy $@  
#     policy=roc.recomgr.policy.FailoverAndRebootPolicy $@  



