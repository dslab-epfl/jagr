#!/bin/sh
                                                                                                                             
if [ -z $JBOSS_HOME ]; then
  echo Please set JBOSS_HOME
  exit 1;
fi;
                                                                                                                            \                                                                                                                             
if [ -z $PP_ROOT ]; then
  echo Please set PP_ROOT
  exit 1;
fi;
                                                                                                                             



export CONFIGURATION=rubis
export BASECONFIG=default
                                                                                                                             
export PINPOINT_HOME=$PP_ROOT/pinpoint
export RUBIS_HOME=$PP_ROOT/apps/RUBiS
export JBOSS_SERVER=$JBOSS_HOME/server
export MYSQL_HOME=$PP_ROOT/apps/mysql/mysql-standard-4.0.12-pc-linux-i686
                                                                                                                             



killall java

cd $MYSQL_HOME
./bin/mysqladmin -u root shutdown
