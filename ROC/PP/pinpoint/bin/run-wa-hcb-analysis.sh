#!/bin/sh

source config.env

export OUTPUTDIR=$PP_ROOT/expts/usenix-04/results/wahgcb/ps-1.3.2-upgrade

if [[ -d $OUTPUTDIR ]]; then
  echo Found output directory $OUTPUTDIR;
else
  echo Output directory does not exist: $OUTPUTDIR;
  exit;
fi


for logfile in $@; do

  export outfile=$OUTPUTDIR/$(basename $logfile).analysis

  if [[ -a $outfile ]]; then
    echo Skipping $logfile: $outfile exists
    continue;
  fi;

  echo Beginning experiments: $logfile;

  echo Starting pinpoint to save observations.
  ./run-pinpoint.sh ../conf/old/workload-adjusted-save-hcb-to-disk.conf observationsfile=$logfile outputfile=$outfile > $outfile-std 2>&1 &


  echo Waiting for 3 min ...
  sleep 180;
  
  echo Killing Pinpoint
  killall -9 java
   
  if [[ -a $outfile ]]; then
    echo logfile $outfile exists\: test appears successful
  else
    echo ERROR: logfile $outfile NOT GENERATED\: TEST FAILED\?
  fi

done
