#!/bin/sh

source config.env

export OBSERVATION_HOSTNAME=$OBSERVATION_MACHINE
export TRIGGERFILE=

export OLDPWD=$(pwd)

cd $PP_ROOT/apps/rubis-new/RUBiS/Client
make emulator


cd $OLDPWD












