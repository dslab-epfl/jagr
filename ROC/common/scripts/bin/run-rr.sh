#!/bin/sh

print_envvars() {
    echo ==================================================
    echo CVS_RSH=$CVS_RSH
    echo JAVA_HOME=$JAVA_HOME
    echo PATH=$PATH
    echo TOP=$TOP
    echo ROC_TOP=$ROC_TOP
    echo JAGR_TOP=$JAGR_TOP
    echo J2EE_HOME=$J2EE_HOME
    echo JBOSS_TOP=$JBOSS_TOP
    echo JBOSS_HOME=$JBOSS_HOME
    echo JBOSS_DIST=$JBOSS_DIST
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
check_env "JAGR_TOP" $JAGR_TOP;
check_env "JBOSS_TOP" $JBOSS_TOP;
check_env "JBOSS_HOME" $JBOSS_HOME;
check_env "JBOSS_DIST" $JBOSS_DIST;
check_env "J2EE_HOME" $J2EE_HOME;
check_env "PATH" $PATH;
check_env "MYSQLDIR" $MYSQLDIR;

check_program "java";
check_program "javac";
check_program "cvs";
check_program "ant";

#
### 2. setup  additional environment variables (for convenience)
#
# a couple directories we want to reference
export SCRIPT_BIN=$ROC_TOP/common/scripts/bin
export PINPOINT_ROOT=$ROC_TOP/PP
export RUBIS_HOME=$ROC_TOP/RR/rr-rubis/rubis-post-1.4.1/
export SSM_HOME=$ROC_TOP/RR/ssm

### run in localhost environment
#export ENV_SCRIPT=$SCRIPT_BIN/env/localhost/env.pl
#export MACHINE_CONFIG=$SCRIPT_BIN/localhost.machine

### run in ssh-based cluster (RR machines)
export ENV_SCRIPT=$SCRIPT_BIN/env/ssh-cluster/env.pl
export MACHINE_CONFIG=$SCRIPT_BIN/almostlocalhost.machine

# config file for pinpoint to run an online-analysis and send results to 
# recovery manager (the default configuration just saves a log of observations)
export PINPOINT_CONFIG=$PINPOINT_ROOT/pinpoint/conf/main/ci-analysis-online.conf

#
## 3. check usage and print help information if needed
#

if [ -z $1 ]; then
   echo ""
   echo "Usage: run-rr.sh --app=[one of the application scripts in scripts/bin/app/**/*.pl] \\"
   echo "                 --expt=[one of the experiment scripts in scripts/bin/expt/**/*.pl]"
   echo ""
   echo "For example use:" 
   echo "    ./run-rr.sh --app=apps/rr-rubis/app.pl --expt=expts/open-ended/expt.pl"
   echo "to run rr-rubis (with Pinpoint monitor and Recovery Manager running) with an"
   echo "open-ended experiment (no no set fault load and no predetermined running time)"
   echo ""
   exit;
fi;

#
### 4. run the run.pl script
#
$SCRIPT_BIN/run.pl --pprootdir=$PINPOINT_ROOT \
                   --rocrootdir=$ROC_TOP \
                   --jbossdir=$JBOSS_HOME \
                   --j2eedir=$J2EE_HOME \
                   --jagrrootdir=$JAGR_TOP \
                   --pinpointdontcopyconfig=true \
                   --ppconf=$PINPOINT_CONFIG \
                   --runrecomgr=true \
                   --machineconfig=$MACHINE_CONFIG \
		   --env=$ENV_SCRIPT \
                   --rubisdir=$RUBIS_HOME \
                   --ssmdir=$SSM_HOME \
                   --mysqldir=$MYSQLDIR \
                   $@




