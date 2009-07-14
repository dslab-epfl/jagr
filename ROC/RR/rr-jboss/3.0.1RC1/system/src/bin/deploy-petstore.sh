#!/bin/sh

if [ -z $J2EE_HOME ]; then
  echo Please define J2EE_HOME
  exit 1
fi

if [ -z $PETSTORE ]; then
  echo Please define PETSTORE
  exit 1
fi

if [ -z $JBOSS_HOME ]; then
  echo Please define JBOSS_HOME
  exit 1
fi

cp $J2EE_HOME/lib/system/cloudscape.jar $JBOSS_HOME/server/default/lib
cp $J2EE_HOME/lib/system/cloudutil.jar $JBOSS_HOME/server/default/lib
cp $J2EE_HOME/lib/cloudscape/cloudclient.jar $JBOSS_HOME/server/default/lib
cp $J2EE_HOME/lib/cloudscape/RmiJdbc.jar $JBOSS_HOME/server/default/lib
cp $PETSTORE/cloudscape-service.xml $JBOSS_HOME/server/default/deploy 
rm -rf /tmp/petstore.ear
mkdir /tmp/petstore.ear
cd /tmp/petstore.ear
unzip $PETSTORE/src/petstore/build/petstore.ear
rm -rf $JBOSS_HOME/server/default/deploy/petstore.ear
mv /tmp/petstore.ear $JBOSS_HOME/server/default/deploy 
