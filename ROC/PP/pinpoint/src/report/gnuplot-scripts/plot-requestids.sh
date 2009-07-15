#!/bin/sh

export PP_ROOT=/home/emrek/Projects/swig/ROC/PP

#export INJECTEDDIR=/home/emrek/Projects/swig/ROC/PP/expts/usenix-04/results/injected/ps-1.1-take10/
#export INJECTEDDIR=/home/emrek/Projects/swig/ROC/PP/expts/usenix-04/results/allfail/ps-1.3.1-take-10/
#export INJECTEDDIR=/home/emrek/Projects/swig/ROC/PP/expts/usenix-04/results/allfail/ps-1.1.2-take-10/
export INJECTEDDIR=/home/emrek/Projects/swig/ROC/PP/expts/osdi-04/srccodebugs/injecteddir


if [ -z $1 ]; then
  echo "Usage: ./plot-requestids.sh [analysis output files...]"
  exit;
fi

for analysisfile in $@; do

  echo Analyzing $analysisfile

  java -cp $PP_ROOT/pinpoint/dist/lib/pinpoint.jar \
    roc.pinpoint.report.GraphRequestIdScores \
    $INJECTEDDIR $analysisfile

  echo "running gnuplot"
  gnuplot plot-requestids.gp

  echo "copying graph to $analysisfile.allfail.png"
  mv requestids.png $analysisfile.allfail.png

done
