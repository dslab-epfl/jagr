#!/bin/sh

if [ -z $PP_ROOT ]; then
  echo Please define PP_ROOT
  exit 1;
fi;

source config.env

export OBSERVATION_HOSTNAME=$OBSERVATION_MACHINE

export ECPERF_DB=$PP_ROOT/apps/ecperf-db

# run observation machine as a backend
echo Killing observation machine on $OBSERVATION_HOSTNAME
export GEXEC_SVRS=$OBSERVATION_HOSTNAME
gexec -n 1 killall -9 java

echo Stopping any existing JBoss servers
killall -9 java
killall -9 rmiregistry

echo Stopping Postgres database 
$PP_ROOT/apps/postgresql/stop-postgresql.sh $ECPERF_DB





