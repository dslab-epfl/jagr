#!/bin/sh

source config.env

# run cloudscape db
echo Starting cloudscape database on $DB_MACHINE
$J2EE_HOME/bin/cloudscape -start &

# run observation machine as a backend
echo Starting observation machine on $OBSERVATION_MACHINE
$PINPOINT_HOME/scripts/run-jboss.sh petstore-backend-1 &

#echo "Waiting for 30 seconds..."
#sleep 30

# todo: ? start analysis engine?




