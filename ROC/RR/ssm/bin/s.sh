#!/bin/sh

export GEXEC_SVRS="x34 x33 x32 x31 x30 x29 x28 x27 x26 x25" 

echo $GEXEC_SVRS

echo $1

gexec -n $1 ss.sh 2000 10 3 2 1 60 $2


#gexec -n $1 ss.sh 2000 10 1 1 1 60 $2




#gexec -n $1 ss.sh 2000 6 3 2 1 60 $2




#gexec -n $1 ss.sh 4000 6 1 1 1 50 $2


