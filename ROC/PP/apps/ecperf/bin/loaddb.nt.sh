#!/bin/sh

# This script loads all data into the database based on the given scale factor.
# Pass the orders_injection_rate as an argument. If not given, defaults to 1
# Akara Sucharitakul 8/8/2000

if [ -z "$1" ] ; then
    echo "orders_injection_rate not specified, setting to 1"
    SCALE=1
else
    echo "Loading database with orders_injection_rate = $1"
    SCALE="$1"
fi

# findHome() finds the package home directory based on it's command.
# Assuming the command file is located in $PKG_HOME/bin
findHome() {
  scriptName=${1##*/}
  case "$1" in
	/bin/*sh)	
	    echo "This script is not supposed to run in a sourced environment!" >&2
	    exit 1 ;;
	/sbin/*sh)
	    echo "This script is not supposed to run in a sourced environment!" >&2
	    exit 1 ;;
	/usr/bin/*sh)
	    echo "This script is not supposed to run in a sourced environment!" >&2
	    exit 1 ;;
	/*/bin/${scriptName}) echo ${1%/bin/${scriptName}} ;;
	*/bin/${scriptName})  homedir=${1%/bin/${scriptName}}
			      echo ${PWD}/${homedir} ;;
	\./${scriptName})     homedir=${PWD%/bin}
			      if [ ${homedir} = ${PWD} ] ; then
				  echo "Not in \$PKG_HOME/bin structure" >&2
				  exit 1
			      fi
			      echo ${homedir} ;;
	*/${scriptName})      homedir=${1%/${scriptName}}
			      cd ${homedir}
			      homedir=${PWD%/bin}
			      if [ ${homedir} = ${PWD} ] ; then
				  echo "Not in \$PKG_HOME/bin structure" >&2
				  exit 1
			      fi
			      echo ${homedir} ;;
	${scriptName})	      script=`which ${scriptName}`
			      homedir=${script%/bin/${scriptName}}
			      if [ ${homedir} = ${PWD} ] ; then
				  echo "Not in \$PKG_HOME/bin structure" >&2
				  exit 1
			      fi
			      echo ${homedir} ;;
	*)		      echo "Your execution path cannot be determined" >&2
			      exit 1 ;;
  esac
}

ECPERF_HOME=`findHome "$0"`

# Configuration file location
CONFIG_DIR=${ECPERF_HOME}/config

# Package containing driver class files
LOAD_PKG=com.sun.ecperf.load

# We check for the apps server and load the necessary environment
# to make a run
APPSSERVER=`cat ${CONFIG_DIR}/appsserver`
echo "Apps server is ${APPSSERVER}"

ENVFILE=${CONFIG_DIR}/${APPSSERVER}.env

if [ ! -f ${ENVFILE} ] ; then
    echo "Cannot find config/${APPSSERVER}.env" >&2
    exit 1
fi

. ${ENVFILE}

# We check for JAVA_HOME. If not set, we do not
# continue processing.

CHECKPASSED=1
if [ -z "${JAVA_HOME}" ] ; then
    echo "JAVA_HOME not set" >&2
    CHECKPASSED=0
fi

# Also we need to check for JDBC_CLASSPATH
if [ -z "${JDBC_CLASSPATH}" ] ; then
    echo "JDBC_CLASSPATH not set" >&2
    CHECKPASSED=0
fi

#If not passing checks, exit
if [ ${CHECKPASSED} = 0 ] ; then
    exit 1
fi
CLASSPATH="${JDBC_CLASSPATH};${ECPERF_HOME}/jars/load.jar"
export CLASSPATH


echo "Loading Corp Database..."
${JAVA_HOME}/bin/java -Decperf.home=${ECPERF_HOME} ${LOAD_PKG}.LoadCorp ${SCALE}
echo "Loading Orders Database..."
${JAVA_HOME}/bin/java -Decperf.home=${ECPERF_HOME} ${LOAD_PKG}.LoadOrds ${SCALE}
echo "Loading Manufacturing Database..."
${JAVA_HOME}/bin/java -Decperf.home=${ECPERF_HOME} ${LOAD_PKG}.LoadMfg ${SCALE}
echo "Loading Supplier Database..."
${JAVA_HOME}/bin/java -Decperf.home=${ECPERF_HOME} ${LOAD_PKG}.LoadSupp ${SCALE}
echo "Loading Discount Rules..."
${JAVA_HOME}/bin/java -Decperf.home=${ECPERF_HOME} ${LOAD_PKG}.LoadRules \
    discount ${ECPERF_HOME}/schema/discount.rules
