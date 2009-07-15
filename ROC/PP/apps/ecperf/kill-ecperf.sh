#!/bin/sh

export DB_MACHINE=x37
export ECPERF_MACHINE=x36

if [ -z $JBOSS_HOME ]; then
  echo Please set JBOSS_HOME
  exit 1;
fi;
                                                                                                                             
if [ -z $PP_ROOT ]; then
  echo Please set PP_ROOT
  exit 1;
fi;

export ECPERF_DATADIR=$PP_ROOT

                                                                                                                            export ECPERF_HOME=$PP_ROOT/apps/ecperf
export ANT=$PP_ROOT/pp-jboss/tools/bin/ant

export OLDDIR=$(pwd)

cd $ECPERF_HOME

echo Killing Postgresql database on $DB_MACHINE
export GEXEC_SVRS=$DB_MACHINE
gexec -v -d -n 1 $PP_ROOT/apps/postgresql/stop-postgresql.sh $ECPERF_HOME/datadir

echo Killing JBoss, with Ecperf
export GEXEC_SVRS=$ECPERF_MACHINE
gexec -n 1 killall run-jboss.sh
gexec -n 1 killall -9 java


cd $OLDDIR






