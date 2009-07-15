#!/bin/sh

count[0]=000-004
count[1]=005-009
count[2]=010-014
count[3]=015-019
count[4]=020-024
count[5]=025-029
count[6]=030-034
count[7]=035-039
count[8]=040-044
count[9]=045-049
count[10]=050-054
count[11]=055-059
count[12]=060-064
count[13]=065-069
count[14]=070-074
count[15]=075-079
count[16]=080-084
count[17]=085-089
count[18]=090-094
count[19]=095-099
count[20]=100-104
count[21]=105-109
count[22]=110-114
count[23]=115-119


INPUTDIR=amzn-fault2-log/all/5min-cluster-chi-2
OUTPUTDIR=$INPUTDIR

i=0

while [ $i -le 22 ]; do
  
  j=$(($i+1))
  while [ $j -le 23 ]; do
    echo comparing 5min cluster ${count[$i]} to ${count[$j]}
    ./run-pinpoint.sh ../conf/main/unsupported/compare-cluster-records.conf \
       reffile=$INPUTDIR/${count[$i]}.5min.cluster \
       testfile=$INPUTDIR/${count[$j]}.5min.cluster \
       > $OUTPUTDIR/${count[$i]}_${count[$j]}.compare 2>&1
    j=$(($j+1))
  done

  i=$(($i+1))
done


