#!/bin/sh

if [ -z $JBOSS_HOME ]; then
  echo error\: JBOSS_HOME is not set;
  exit;
fi

if [ -z $J2EE_HOME ]; then
  echo error\: J2EE_HOME is not set;
  exit;
fi

if [ -z $PETSTORE_HOME ]; then
  echo error\: PETSTORE_HOME is not set;
  exit;
fi

if [ -z $PINPOINT_HOME ]; then
  echo error\: PINPOINT_HOME is not set;
  exit;
fi

for i in $(seq 1 $1); do 
  CONFIGURATION=petstore-backend-$i;
  rm -rf $JBOSS_HOME/server/$CONFIGURATION;
  cp -R $JBOSS_HOME/server/all $JBOSS_HOME/server/$CONFIGURATION;
  cp $PETSTORE_HOME/clustering/login-config.xml $JBOSS_HOME/server/$CONFIGURATION/conf/;
  cp $PETSTORE_HOME/clustering/jboss-minimal.xml $JBOSS_HOME/server/$CONFIGURATION/conf;
  cp $PETSTORE_HOME/clustering/jboss-service.xml $JBOSS_HOME/server/$CONFIGURATION/conf/;
  cp $J2EE_HOME/lib/system/cloudscape.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $J2EE_HOME/lib/system/cloudutil.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $J2EE_HOME/lib/cloudscape/cloudclient.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $J2EE_HOME/lib/cloudscape/RmiJdbc.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PETSTORE_HOME/clustering/petstore.ear $JBOSS_HOME/server/$CONFIGURATION/deploy;
  cp $PETSTORE_HOME/clustering/cloudscape-service.xml $JBOSS_HOME/server/$CONFIGURATION/deploy;
  cp $PETSTORE_HOME/clustering/emk-util.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PINPOINT_HOME/output/pinpoint.jar $JBOSS_HOME/lib;
  cp $PINPOINT_HOME/output/pinpoint.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PINPOINT_HOME/lib/xercesImpl.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PINPOINT_HOME/lib/xmlParserAPIs.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
done

# and make copies for the web frontend
for i in $(seq 1 $2); do 
  CONFIGURATION=petstore-frontend-$i;
  rm -rf $JBOSS_HOME/server/$CONFIGURATION;
  cp -R $JBOSS_HOME/server/all $JBOSS_HOME/server/$CONFIGURATION;
  cp $PETSTORE_HOME/clustering/login-config.xml $JBOSS_HOME/server/$CONFIGURATION/conf/;
  cp $PETSTORE_HOME/clustering/jboss-minimal.xml $JBOSS_HOME/server/$CONFIGURATION/conf;
  cp $PETSTORE_HOME/clustering/jboss-service.xml $JBOSS_HOME/server/$CONFIGURATION/conf/;
  cp $J2EE_HOME/lib/system/cloudscape.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $J2EE_HOME/lib/system/cloudutil.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $J2EE_HOME/lib/cloudscape/cloudclient.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $J2EE_HOME/lib/cloudscape/RmiJdbc.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PETSTORE_HOME/clustering/petstore-web.ear $JBOSS_HOME/server/$CONFIGURATION/deploy;
  cp $PETSTORE_HOME/clustering/cloudscape-service.xml $JBOSS_HOME/server/$CONFIGURATION/deploy;
  cp $PETSTORE_HOME/clustering/emk-util.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PINPOINT_HOME/output/pinpoint.jar $JBOSS_HOME/lib;
  cp $PINPOINT_HOME/output/pinpoint.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PINPOINT_HOME/lib/xercesImpl.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PINPOINT_HOME/lib/xmlParserAPIs.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
done
