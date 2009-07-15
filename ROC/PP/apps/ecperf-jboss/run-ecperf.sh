#!/bin/sh

export PP_ROOT=/work/emrek/ROC/PP

if [ -z $1 ]; then
  echo Usage: run-ecperf.sh observationsfile
  exit
fi;

source config.env

export OBSERVATION_HOSTNAME=$OBSERVATION_MACHINE
export OUTPUT=$1

OLDDIR=$(pwd)

# 1. reset postgres db

echo Resetting Postgres

rm -rf $PP_ROOT/apps/ecperf-db
tar -C $PP_ROOT/apps -zxf $PP_ROOT/apps/ecperf-db.tar.gz

echo Starting Postgres db

# 2. start postgres db
$PP_ROOT/apps/postgresql/start-postgresql.sh $PP_ROOT/apps/ecperf-db &

echo Starting Pinpoint observer on $OBSERVATION_HOSTNAME
export GEXEC_SVRS=$OBSERVATION_HOSTNAME
gexec -v -n 1 $PP_ROOT/pinpoint/bin/run-pinpoint.sh ../conf/main/save-observations-to-disk.conf output=$OUTPUT > pinpoint.stdout 2>&1 &

echo "Waiting for 15 seconds..."
sleep 15

# 3. start jboss
$PP_ROOT/expts/bin/start-jboss.sh $JBOSS_HOME/ ecperf &

cd $OLDDIR

