FAULTDIR=/work/emrek/ROC/PP/expts/faults/rubis-beans/

PRIORITY=

RUNTIME_EX=$FAULTDIR/faultconfig-name*runtime_exception
EXPECTED_EX=$FAULTDIR/faultconfig-name*expected_exception
INF_LOOP=$FAULTDIR/faultconfig-name*infinite_loop
NULL_CALL=$FAULTDIR/faultconfig-name*null*

CONST_PERF=$FAULTDIR/faultconfig-name*constantPerformance
CUML_PERF=$FAULTDIR/faultconfig-name*cumulativePerformance
STUTT_PERF=$FAULTDIR/faultconfig-name*stutterPerformance

NOFAULT_A=$FAULTDIR/faultconfig-nofault[12]
NOFAULT_B=$FAULTDIR/faultconfig-nofault[34]

./run-fault-experiments.sh \
  $PRIORITY    \
  $NOFAULT_A   \
  $RUNTIME_EX  \
  $EXPECTED_EX \
  $NULL_CALL \
  $NOFAULT_B

#  $CONST_PERF  \
#  $CUML_PERF   \
#  $STUTT_PERF  \
#  $INF_LOOP    \



