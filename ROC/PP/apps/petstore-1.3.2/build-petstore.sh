#!/bin/sh

if [ -z $JBOSS_HOME ]; then
  echo Please set JBOSS_HOME
  exit 1;
fi;

if [ -z $PP_ROOT ]; then
  echo Please set PP_ROOT
  exit 1;
fi;

export PINPOINT_HOME=$PP_ROOT/pinpoint
export PETSTORE_HOME=$PP_ROOT/apps/petstore-1.3.2
export CLOUDSCAPE_HOME=$PP_ROOT/apps/cloudscape
export JBOSS_SERVER=$JBOSS_HOME/server

export ORIGPWD=$(pwd)

cd $PETSTORE_HOME/src
./build.sh $@

cd $ORIGPWD

###
echo Done building Petstore
