#!/bin/bash

export BASEDIR=$(dirname $0)

cd $BASEDIR

export RECOMGR_HOME=$JAGR_TOP/jagr-recomgr

ORIGDIR=$(pwd)
RECOMGR_JAR=$RECOMGR_HOME/dist/lib/jagrrecomgr.jar

cd $RECOMGR_HOME/lib
for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

cd $JBOSS_TOP/console/output/lib
for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

cd $JBOSS_TOP/thirdparty/apache/log4j/lib/
for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

CLASSPATH=$CLASSPATH:$RECOMGR_JAR

cd $ORIGDIR

#echo classpath is $CLASSPATH


#JAVA_OPTS="-Xrunjmp:nomethods $JAVA_OPTS"
 
java -ea -DROC_TOP=${ROC_TOP} $JAVA_OPTS -Xss128M -Xmx512M -cp $CLASSPATH roc.jagr.recomgr.RecoveryManager $@




