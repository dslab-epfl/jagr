#!/bin/tcsh
echo 'starting LoadGenPauseUtil ...'
exec xterm -e java -cp ./roc/rr/rm/ LoadGenPauseUtil "./roc/rr/rm/startload.sh" "./roc/rr/rm/stopload.sh" &
echo 'starting Recovery Manager ...'
echo 'java -cp . roc.rr.rm.TheBrain'
java -cp . roc.rr.rm.TheBrain
