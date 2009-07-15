#!/bin/bash

export LOADGEN_TOP=${HOME}/loadgen
export LOADGEN_JAR=${LOADGEN_TOP}/dist/lib/loadgen.jar
export SWIGUTIL_JAR=${ROC_TOP}/common/swig-util/dist/lib/swigutil.jar
export XERCES_JAR=${ROC_TOP}/common/swig-util/lib/xercesImpl.jar
export XMLPARSE_JAR=${ROC_TOP}/common/swig-util/lib/xmlParserAPIs.jar
export LOG4J_JAR=${JBOSS_TOP}/thirdparty/apache/log4j/lib/log4j.jar

export CLASSPATH=${LOADGEN_JAR}:${SWIGUTIL_JAR}:${XERCES_JAR}:${XMLPARSE_JAR}:${LOG4J_JAR}

pushd . > /dev/null
cd $LOADGEN_TOP/lib
for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done
popd > /dev/null

java -Denv.log4j=${LOADGEN_TOP}/conf/log4j.cfg \
     -ea roc.loadgen.util.PerformanceAnalysis $@
