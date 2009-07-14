#!/bin/sh
#
# manualsb.sh: Brick startup script
#    originaly written by Ben
#    modified by S.Kawamoto  Mar/16/2004
#

PUBLISHER=-Droc.pinpoint.tracing.Publisher=roc.pinpoint.tracing.java.TCPObservationPublisher
#HOSTNAME=-Droc.pinpoint.publishto.hostname=x1.millennium.berkeley.edu
HOSTNAME=-Droc.pinpoint.publishto.hostname=localhost

if [ -f ../ssm.jar ]
then

    if [ $# -eq 1 ]
    then
	java -classpath ${CLASSPATH}:../lib/pinpoint.jar:../ssm.jar:. ${PUBLISHER} ${HOSTNAME} -Xms384M -Xmx384M roc.rr.ssm.Start b $1
    else
	echo "usage: manualsb.sh <numerical id of invoking brick>"
    fi

else

  echo "Ssm.jar doesn't exist. Build it first!"

fi



