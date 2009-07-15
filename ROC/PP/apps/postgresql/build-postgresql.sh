#!/bin/sh

if [ -z $JBOSS_HOME ]; then
  echo Please set JBOSS_HOME
  exit 1;
fi;
                                                                                                                             
if [ -z $PP_ROOT ]; then
  echo Please set PP_ROOT
  exit 1;
fi;

export POSTGRESQL_ROOT=$PP_ROOT/apps/postgresql
export POSTGRESQL_HOME=$POSTGRESQL_ROOT/output
export POSTGRESQL=$POSTGRESQL_HOME/bin/postgreql

export OLDDIR=$(pwd)

cd $POSTGRESQL_ROOT

make clean
./configure --prefix=$POSTGRESQL_HOME  --without-readline
make
make install

cd $OLDDIR

