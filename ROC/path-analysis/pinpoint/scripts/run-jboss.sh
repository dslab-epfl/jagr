#!/bin/sh

source config.env

cd $JBOSS_HOME/bin

./run.sh --configuration=$1
