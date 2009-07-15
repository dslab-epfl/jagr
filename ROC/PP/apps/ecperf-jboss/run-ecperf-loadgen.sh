#!/bin/sh

source config.env

export OBSERVATION_HOSTNAME=$OBSERVATION_MACHINE
export TRIGGERFILE=

cd $PP_ROOT/apps/ecperf-jboss/bin

./driver.sh 











