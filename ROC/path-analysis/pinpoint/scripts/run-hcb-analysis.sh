#!/bin/sh

source config.env

export HISTORICAL=$PINPOINT_HOME/logs/ams-expts/historicalbehavior-faultconfig-nofaults
export OUTPUTDIR=$PINPOINT_HOME/analysis-output/ams-expts

if [[ -d $OUTPUTDIR ]]; then
  echo Found output directory $OUTPUTDIR;
else
  echo Output directory does not exist: $OUTPUTDIR;
  exit;
fi


for logfile in $@; do

  export outfile=$OUTPUTDIR/$logfile.analysis

  if [[ -a $outfile ]]; then
    echo Skipping $logfile: $outfile exists
    continue;
  fi;

  echo Beginning experiments: $logfile;

  echo Starting pinpoint to save observations.
  ./run-pinpoint.sh config/hcb-anomaly-to-disk.conf historicalfile=$HISTORICAL observationsfile=$logfile outputfile=$outfile &


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
