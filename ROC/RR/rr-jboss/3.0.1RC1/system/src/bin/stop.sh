#!/bin/sh

# $Id: stop.sh,v 1.4 2003/04/07 08:41:51 emrek Exp $
#
# Run this script to stop all the auxiliary RR-JBoss services
# (should be run after stopping JBoss)

if [ -z $J2EE_HOME ]; then
  echo Please define J2EE_HOME
  exit 1
fi


RUNDIR="/tmp/RR-JBoss"

echo ">>> Stopping TheBrain..."
PID=`cat $RUNDIR/TheBrain.pid`
exec kill -INT $PID &

echo ">>> Stopping DelayProxy..."
PID=`cat $RUNDIR/DelayProxy.pid`
exec kill -INT $PID &

# For the time being, JNP is inside JBoss...
#
# echo ">>> Stopping external JNP..."
# PID=`cat $RUNDIR/JNP.pid`
# exec kill -INT $PID &

echo ">>> Stopping Cloudscape database..."
PID=`cat $RUNDIR/Cloudscape.pid`
exec $J2EE_HOME/bin/cloudscape -stop
