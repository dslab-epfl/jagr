#!/bin/sh

if [ -z $JBOSS_HOME ]; then
  echo Please set JBOSS_HOME
  exit 1;
fi;

if [ -z $PP_ROOT ]; then
  echo Please set PP_ROOT
  exit 1;
fi;

if [ -z $CLOUDSCAPE_HOSTNAME ]; then
  echo Please set CLOUDSCAPE_HOSTNAME
  exit 1;
fi;

if [ -z $1 ]; then
  echo Please specify configuration name.
  echo "    ./deploy-petstore-backend.sh [configname]";
  exit 1;
fi;

export PINPOINT_HOME=$PP_ROOT/pinpoint
export PETSTORE_HOME=$PP_ROOT/apps/petstore-1.3.2
export CLOUDSCAPE_HOME=$PP_ROOT/apps/cloudscape
export JBOSS_SERVER=$JBOSS_HOME/server

# which of jboss' existing configurations to copy...
export NEWCONFIG=$1
#export BASECONFIG=all
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


###
# 3. copy cloudscape jars
###
cp $CLOUDSCAPE_HOME/lib/system/cloudscape.jar $JBOSS_SERVER/$NEWCONFIG/lib
cp $CLOUDSCAPE_HOME/lib/system/cloudutil.jar $JBOSS_SERVER/$NEWCONFIG/lib
cp $CLOUDSCAPE_HOME/lib/cloudscape/cloudclient.jar $JBOSS_SERVER/$NEWCONFIG/lib
cp $CLOUDSCAPE_HOME/lib/cloudscape/RmiJdbc.jar $JBOSS_SERVER/$NEWCONFIG/lib


###
# 4. copy configuration modifications
###

## copy this to turn on Pinpoint's EJB monitoring
#cp $PINPOINT_HOME/src/jboss-ext/conf/standardjboss.xml $JBOSS_SERVER/$NEWCONFIG/conf

cat $PETSTORE_HOME/jboss/cloudscape-ds.xml.in \
   | sed 's/\$CLOUDSCAPEHOST\$/'$CLOUDSCAPE_HOSTNAME/ > \
   $JBOSS_SERVER/$NEWCONFIG/deploy/cloudscape-ds.xml

cp $PETSTORE_HOME/jboss/login-config.xml  $JBOSS_SERVER/$NEWCONFIG/conf
cp $PETSTORE_HOME/jboss/petstoremq-destinations-service.xml $JBOSS_SERVER/$NEWCONFIG/deploy
cp $PETSTORE_HOME/jboss/mail-service.xml $JBOSS_SERVER/$NEWCONFIG/deploy


###
# 5. copy petstore deployment files
###
cp $PETSTORE_HOME/src/apps/petstore/build/petstore.ear $JBOSS_SERVER/$NEWCONFIG/deploy
cp $PETSTORE_HOME/src/apps/opc/build/opc.ear $JBOSS_SERVER/$NEWCONFIG/deploy
cp $PETSTORE_HOME/src/apps/admin/build/petstoreadmin.ear $JBOSS_SERVER/$NEWCONFIG/deploy
cp $PETSTORE_HOME/src/apps/supplier/build/supplier.ear $JBOSS_SERVER/$NEWCONFIG/deploy


###
# 6. copy pkgen.jar library
###
cp $PETSTORE_HOME/src/components/pkgen/build/pkgen.jar $JBOSS_SERVER/$NEWCONFIG/lib


cd $ORIGPWD

###
echo Done creating Petstore backend configuration\: $NEWCONFIG
