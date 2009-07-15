#!/bin/sh

print_envvars() {
    echo ==================================================
    echo CVS_RSH=$CVS_RSH
    echo JAVA_HOME=$JAVA_HOME
    echo PATH=$PATH
    echo TOP=$TOP
    echo ROC_TOP=$ROC_TOP
    echo PP_ROOT=$PP_ROOT
    echo ==================================================
}

check_env() {
    if [ -z $2 ]; then
        echo ERROR: $1 is not defined
        exit;
    fi
}

check_program() {
    if [ -z $(which $1 2>/dev/null) ]; then
        echo ERROR: Could not find program $1 on the path
        exit;
    fi
}


#
# 1. check your environment variables
#
print_envvars;

check_env "CVS_RSH" $CVS_RSH;
check_env "JAVA_HOME" $JAVA_HOME;
check_env "TOP" $TOP;
check_env "ROC_TOP" $ROC_TOP;
check_env "PATH" $PATH;

check_program "java";

export PP_ROOT=$ROC_TOP/PP
export PINPOINT_HOME=$PP_ROOT/pinpoint

###########################################

export BASEDIR=$(dirname $0)

cd $BASEDIR


#source config.env

ORIGDIR=$(pwd)
PINPOINT_JAR=$PINPOINT_HOME/dist/lib/pinpoint.jar

cd $PINPOINT_HOME/lib

for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

#temp
#cd $JBOSS_HOME/client
#
#for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

CLASSPATH=$CLASSPATH:$PINPOINT_JAR



cd $ORIGDIR

#echo classpath is $CLASSPATH


#JAVA_OPTS="-Xrunjmp:nomethods $JAVA_OPTS"
#JAVA_OPTS="-Xruncontentionprofiler"
 
java $JAVA_OPTS -Xms250M -Xmx800M -cp $CLASSPATH -DROC_TOP=$ROC_TOP roc.pinpoint.analysis.AnalysisEngine $@  




