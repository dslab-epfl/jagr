#!/bin/sh

source config.env

export OUTPUTDIR=$PINPOINT_HOME/logs/fault-expts

if [[ -d $OUTPUTDIR ]]; then
  echo Found output directory $OUTPUTDIR;
else
  echo Output directory does not exist: $OUTPUTDIR;
  exit;
fi


for faultfile in $@; do

  export obsfile=$OUTPUTDIR/$faultfile.log

  if [[ -a $obsfile ]]; then
    echo Skipping $faultfile: $obsfile exists
    continue;
  fi;

  echo Beginning experiments: $faultfile;

  ##TODO reset cloudscape db
  
  export CLOUDSCAPEDB=$J2EE_HOME/cloudscape

  if [[ -a $CLOUDSCAPEDB/EstoreDB ]]; then
    echo Removing cloudscape\'s old petstore database
    rm -rf $CLOUDSCAPEDB/EstoreDB;
  fi

  echo restoring clean petstore database
  tar -C $CLOUDSCAPEDB -xzf $PETSTORE_HOME/clean-cloudscape/petstore-db.tar.gz
  

  # setup triggerfile 
  export TRIGGERFILE=$(pwd)/$faultfile

  echo starting petstore
  ./run-petstore.sh

  echo Starting pinpoint to save observations.
  ./run-pinpoint.sh config/save-observations.conf filename=$obsfile &


  echo Waiting for 30 seconds...
  sleep 30;
  
  echo Starting Load Generator
  ./run-petstore-loadgen.sh
  
  ## waiting for loadgen to finish

  echo Killing Pinpoint
  killall -9 java
   
  echo Killing Petstore
  ./kill-petstore.sh
  
  if [[ -a $obsfile ]]; then
    echo logfile $obsfile exists\: test appears successful
  else
    echo ERROR: logfile $obsfile NOT GENERATED\: TEST FAILED\?
  fi

done
