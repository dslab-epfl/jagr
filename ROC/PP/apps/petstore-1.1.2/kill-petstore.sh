#!/bin/sh

if [ -z $PP_ROOT ]; then
  echo Please define PP_ROOT
  exit 1;
fi;

source config.env

export CLOUDSCAPE_HOSTNAME=$DB_MACHINE
export OBSERVATION_HOSTNAME=$OBSERVATION_MACHINE

export CLOUDSCAPE_HOME=$PP_ROOT/apps/cloudscape
export PETSTORE112_HOME=$PP_ROOT/apps/petstore-1.1.2
export PETSTORE131_HOME=$PP_ROOT/apps/petstore


echo Killing cloudscape database on $CLOUDSCAPE_HOSTNAME
export GEXEC_SVRS=$CLOUDSCAPE_HOSTNAME
gexec -n 1 killall cloudscape
gexec -n 1 killall java


# run observation machine as a backend
echo Killing observation machine on $OBSERVATION_HOSTNAME
export GEXEC_SVRS=$OBSERVATION_HOSTNAME
gexec -n 1 killall -9 java

echo Killing frontend machines...
for i in $FRONTEND_MACHINES; do
  export GEXEC_SVRS=$i;
  gexec -v -n 1 killall -9 java
done;

echo Killing backend machines...
for i in $BACKEND_MACHINES; do
  export GEXEC_SVRS=$i;
  gexec -v -n 1 killall -9 java
done;


