#!/bin/sh

source config.env

export OUTPUTDIR=$PP_ROOT/expts/head/ecperf-1.1

if [[ -d $OUTPUTDIR ]]; then
  echo Found output directory $OUTPUTDIR;
else
  echo Output directory does not exist: $OUTPUTDIR;
  exit;
fi

if [ -z $1 ]; then
  echo "Usage: ./run-fault-experiments.sh [faultfiles...]"
  echo " IMPORTANT: faultfiles MUST be given with absolute path"
  exit 1;
fi

for faultfile in $@; do

  export obsfile=$OUTPUTDIR/$(basename $faultfile).log
  export serverlog=$OUTPUTDIR/$(basename $faultfile).server

  if [[ -a $obsfile ]]; then
    echo Skipping $faultfile: $obsfile exists
    continue;
  fi;

  echo Beginning experiments: $faultfile;

  # setup triggerfile 
  export TRIGGERFILE=$faultfile

  echo starting ecperf
  ./run-ecperf.sh $obsfile > $OUTPUTDIR/$(basename $faultfile).expt-stdout 2>&1 &

  echo Waiting for 120 seconds...
  sleep 120;
  
  echo Starting Load Generator \(ECPerf Driver\)
    $PP_ROOT/apps/ecperf-jboss/run-ecperf-loadgen.sh > $OUTPUTDIR/$(basename $faultfile).loadgen-stdout 2>&1 &

  echo Waiting for 2:00 minutes
  sleep 120

  # kill everything
  ./kill-ecperf.sh

  JBOSS_HOME=$PP_ROOT/pp-jboss-3.2.1/build/output/jboss-3.2.1

  cp $JBOSS_HOME/server/ecperf/log/server.log ${serverlog}.b1

  cp $JBOSS_HOME/server/ecperf/log/*.request.log ${requestlog}.b1


  if [[ -a $obsfile ]]; then
    echo logfile $obsfile exists\: test appears successful
  else
    echo ERROR: logfile $obsfile NOT GENERATED\: TEST FAILED\?
  fi

done


