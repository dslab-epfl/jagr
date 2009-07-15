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

echo Resetting MySql

rm -rf $PP_ROOT/apps/rubis-new/mysql-max-3.23.58-pc-linux-i686/
tar -C $PP_ROOT/apps/rubis-new/ -zxf $PP_ROOT/apps/rubis-new/tarballs/mysql-max-rubis-3.23.58-v2.tgz

echo Starting MySql DB

# 2. start db
cd $PP_ROOT/apps/rubis-new/mysql-max-3.23.58-pc-linux-i686
bin/safe_mysqld --pid-file=$PP_ROOT/apps/rubis-new/mysqlpid --pid-file=$PP_ROOT/apps/rubis-new/mysqlpid &

cd $OLDDIR

#3. deploy rubis
./deploy-rubis.sh rubis



echo Starting Pinpoint observer on $OBSERVATION_HOSTNAME
export GEXEC_SVRS=$OBSERVATION_HOSTNAME
gexec -v -n 1 $PP_ROOT/pinpoint/bin/run-pinpoint.sh ../conf/main/save-observations-to-disk.conf output=$OUTPUT &

echo "Waiting for 15 seconds..."
sleep 15

# 3. start jboss
$PP_ROOT/expts/bin/start-jboss.sh $JBOSS_HOME/ rubis &

cd $OLDDIR

