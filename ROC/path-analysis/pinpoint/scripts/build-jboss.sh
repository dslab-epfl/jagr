#!/bin/sh

olddir=$(pwd)

source config.env

cd $JBOSS_SRC_HOME/build

./build.sh

cd $olddir
