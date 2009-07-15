#!/bin/sh


export d=1100

INPUTDIR=amzn-fault2-log/$d
OUTPUTDIR=$INPUTDIR/5min-cluster-chi-2

mkdir -p $OUTPUTDIR

for t in 0 5 10 15 20 25 30 35 40 45 50 55; do 
  echo clustering minutes $t to $(($t+4))
  ./run-pinpoint.sh ../conf/main/unsupported/inspect-query-log-cb.conf \
     inputfile=$INPUTDIR/$(($t+0)).gz,\
$INPUTDIR/$(($t+1)).gz,\
$INPUTDIR/$(($t+2)).gz,\
$INPUTDIR/$(($t+3)).gz,\
$INPUTDIR/$(($t+4)).gz,\
  outputfile=$OUTPUTDIR/$t-$(($t+4)).5min.cluster
done
