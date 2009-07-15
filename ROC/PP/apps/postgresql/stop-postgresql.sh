#!/bin/sh

if [ -z $PP_ROOT ]; then
  echo Please set PP_ROOT
  exit 1;
fi;

if [ -z $1 ]; then
  echo 'Usage: run-postgreql.sh DATABASEDIR'
  exit 1;
fi;

export DATABASEDIR=$1

export POSTGRESQL_ROOT=$PP_ROOT/apps/postgresql
export POSTGRESQL_HOME=$POSTGRESQL_ROOT/output
export POSTGRESQL=$POSTGRESQL_HOME/bin/pg_ctl

export OLDDIR=$(pwd)

cd $POSTGRESQL_HOME

$POSTGRESQL stop -D $DATABASEDIR

cd $OLDDIR

