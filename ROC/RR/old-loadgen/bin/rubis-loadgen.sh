#!/bin/tcsh

setenv LOADGEN_TOP  ${JAGR_TOP}/loadgen
setenv LOADGEN_JAR  ${LOADGEN_TOP}/dist/lib/loadgen4.jar
setenv SWIGUTIL_JAR ${ROC_TOP}/common/swig-util/dist/lib/swigutil.jar
setenv XERCES_JAR   ${ROC_TOP}/common/swig-util/lib/xercesImpl.jar
setenv XMLPARSE_JAR ${ROC_TOP}/common/swig-util/lib/xmlParserAPIs.jar
setenv LOG4J_JAR    ${JBOSS_TOP}/thirdparty/apache/log4j/lib/log4j.jar

setenv CLASSPATH ${LOADGEN_JAR}:${SWIGUTIL_JAR}:${XERCES_JAR}:${XMLPARSE_JAR}:${LOG4J_JAR}

java -DROC_TOP=${ROC_TOP} -ea roc.loadgen.Engine ${JAGR_TOP}/loadgen/conf/RubisLoad.conf \
                              config_file=${JAGR_TOP}/loadgen/conf/rubis.cfg runtime=300000
