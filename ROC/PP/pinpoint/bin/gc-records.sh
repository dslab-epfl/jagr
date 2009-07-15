#!/bin/sh

export HGCBDIR=/home/emrek/Projects/swig/ROC/PP/expts/ams-2003/intermediate/historicalbehavior-rr-petstore-04-07-2003-nofaults-notcleaned/
export NEWHGCBDIR=/home/emrek/Projects/swig/ROC/PP/expts/ams-2003/intermediate/historicalbehavior-rr-petstore-04-07-2003-nofaults


for i in $HGCBDIR/*; do
  export outfile=$NEWHGCBDIR/$(basename $i)

  if [[ -a $outfile ]]; then
    echo Skipping $outfile: already exists
    continue;
  fi;

  ./run-pinpoint.sh ../conf/gc-saved-records.conf \
      inputfile=$i \
      outputfile=$outfile &
  sleep 250
  killall java
done
