#!/bin/sh

source config.env

cd $LOADGEN_HOME

java -cp . LoadGen 1 $OBSERVATION_MACHINE 8080

