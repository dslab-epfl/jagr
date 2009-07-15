#!/bin/sh

if [ -z $JBOSS_HOME ]; then
  echo Please set JBOSS_HOME
  exit 1;
fi;
                                                                                
if [ -z $PP_ROOT ]; then
  echo Please set PP_ROOT
  exit 1;
fi;

if [ -z $1 ]; then
  echo Please specify configuration name.
  echo "    ./deploy-petstore-backend.sh [configname]";
  exit 1;
fi;


export PINPOINT_HOME=$PP_ROOT/pinpoint
export RUBIS_HOME=$PP_ROOT/apps/rubis-new
export JBOSS_SERVER=$JBOSS_HOME/server

export NEWCONFIG=$1
export BASECONFIG=default

export ORIGPWD=$(pwd)

cd $JBOSS_SERVER

###
# 0. remove any existing configuration with this name
###
rm -rf $NEWCONFIG

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
cp $PP_ROOT/../common/swig-util/dist/lib/swigutil.jar $JBOSS_SERVER/$NEWCONFIG/lib

cp $PINPOINT_HOME/src/jboss-ext/conf/standardjboss.xml-jboss-3.2.1 $JBOSS_SERVER/$NEWCONFIG/conf/standardjboss.xml

###
# 3. copy rubis-support files
###
cp $RUBIS_HOME/jboss/default/conf/* $JBOSS_SERVER/$NEWCONFIG/conf/
cp $RUBIS_HOME/jboss/default/lib/* $JBOSS_SERVER/$NEWCONFIG/lib/
cp $RUBIS_HOME/jboss/default/deploy/* $JBOSS_SERVER/$NEWCONFIG/deploy/

###
# 4. deploy rubis jar
###
cp $RUBIS_HOME/RUBiS/EJB_EntityBean_id_BMP/rubis.jar $JBOSS_SERVER/$NEWCONFIG/deploy/
cp $RUBIS_HOME/RUBiS/EJB_EntityBean_id_BMP/ejb_rubis_web.war $JBOSS_SERVER/$NEWCONFIG/deploy/



cd $ORIGPWD
                                                                                
###
echo Done creating RUBiS configuration\: $NEWCONFIG
