#!/bin/sh

source config.env

export HISTORICAL=$PP_ROOT/expts/sosp-2003/intermediate/historical-path-behavior-petstore-1.3.1-03-22-2003-1
export OUTPUTDIR=$PP_ROOT/expts/sosp-2003/path-diag/petstore-1.3.1-03-22-2003-0.5sens

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
  ./run-pinpoint.sh $PINPOINT_HOME/conf/pb-correlation-save-to-disk.conf historicalfile=$HISTORICAL observationsfile=$logfile outputfile=$outfile &


  echo Waiting for 2.5 min ...
  sleep 150;
  
  echo Killing Pinpoint
  killall -9 java
   
  if [[ -a $outfile ]]; then
    echo logfile $outfile exists\: test appears successful
  else
    echo ERROR: logfile $outfile NOT GENERATED\: TEST FAILED\?
  fi

done
