#!/bin/sh

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

cd $ECPERF_HOME/bin

./driver.sh

cd $OLDDIR










