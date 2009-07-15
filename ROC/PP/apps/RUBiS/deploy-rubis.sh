#!/bin/sh

if [ -z $JBOSS_HOME ]; then
  echo Please set JBOSS_HOME
  exit 1;
fi;
                                                                                                                             
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

                                                                                                                       
export ORIGDIR=$(pwd)


cd $JBOSS_SERVER

###
# 0. remove any existing configuration with this name
###
rm -rf $NEWCONFIG
                                                                                                      
###
# 0.1 clean MySQL Database
###
echo Cleaning MySQL DB
rm -rf $MYSQL_HOME/data/

###
# 0.2 restore fresh MySQL Database
###
echo Restoring fresh MySQL DB
tar -C $MYSQL_HOME/data/ jxf $RUBIS_HOME/rubis-proto-db.tar.bz2                             

###
# 1. copy the base confiuration
###
cp -R $BASECONFIG $NEWCONFIG

###
# 2. copy pinpoint.jar and dependencies
###
cp $PINPOINT_HOME/dist/lib/pinpoint.jar $JBOSS_SERVER/$NEWCONFIG/lib
cp $PINPOINT_HOME/lib/xercesImpl.jar $JBOSS_SERVER/$NEWCONFIG/lib
cp $PINPOINT_HOME/lib/xmlParserAPIs.jar $JBOSS_SERVER/$NEWCONFIG/lib

###
# 4. copy configuration modifications
###
cp $RUBIS_HOME/jboss_config/mysql-service.xml $JBOSS_SERVER/$NEWCONFIG/deploy
cp $RUBIS_HOME/jboss_config/login-config.xml $JBOSS_SERVER/$NEWCONFIG/conf
cp $RUBIS_HOME/jboss_config/mm.mysql-2.0.12-bin.jar $JBOSS_SERVER/$NEWCONFIG/lib

###
# 5. copy RUBiS application packages
###
cp $RUBIS_HOME/EJB_EntityBean_id_BMP/rubis.jar $JBOSS_SERVER/$NEWCONFIG/deploy
cp $RUBIS_HOME/EJB_EntityBean_id_BMP/ejb_rubis_web.war $JBOSS_SERVER/$NEWCONFIG/deploy




cd $ORIGDIR

