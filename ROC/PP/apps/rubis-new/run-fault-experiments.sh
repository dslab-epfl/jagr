#!/bin/sh

source config.env

export OUTPUTDIR=$PP_ROOT/expts/usenix-04/rubis

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

  echo starting rubis
  ./run-rubis.sh $obsfile > $OUTPUTDIR/$(basename $faultfile).expt-stdout &

  echo Waiting for 90 seconds...
  sleep 90;
  
  echo Starting Load Generator \(RUBiS Client Emulator\)
    $PP_ROOT/apps/rubis-new/run-rubis-loadgen.sh &

  echo Waiting for 5:00 minutes
  sleep 300

  # kill everything (twice for good measure)
  ./kill-rubis.sh
  ./kill-rubis.sh

  JBOSS_HOME=$PP_ROOT/pp-jboss-3.2.1/build/output/jboss-3.2.1

  cp $JBOSS_HOME/server/rubis/log/server.log ${serverlog}.b1
  cp $JBOSS_HOME/server/rubis/log/*request.log ${requestlog}.b1


  if [[ -a $obsfile ]]; then
    echo logfile $obsfile exists\: test appears successful
  else
    echo ERROR: logfile $obsfile NOT GENERATED\: TEST FAILED\?
  fi

done


