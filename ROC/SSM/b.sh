#!/bin/sh

export GEXEC_SVRS="x21 x22 x23 x24 x25 x26 x27 x28 x29 x2 x3 x4 x5 x6 x7 x8 x9 x10"

echo $GEXEC_SVRS

echo $1 "total bricks"
echo $2 "faulty bricks"

gexec -n $1 sb.sh f 3200000 $2 $3








