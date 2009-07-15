#!/bin/sh

if [ -z $PP_ROOT ]; then
  echo error\: PP_ROOT is not set;
  exit;
fi

if [ -z $1 ]; then
  echo Usage\: prepare-petstore-cluster.sh numbackends numfrontends
  exit;
fi

export JBOSS_HOME=$PP_ROOT/pp-jboss/build/output/jboss-3.0.6
export PETSTORE112_HOME=$PP_ROOT/apps/petstore-1.1.2
export PINPOINT_HOME=$PP_ROOT/pinpoint
export CLOUDSCAPE_HOME=$PP_ROOT/apps/cloudscape

for i in $(seq 1 $1); do 
  CONFIGURATION=petstore-backend-$i;
  rm -rf $JBOSS_HOME/server/$CONFIGURATION;
  cp -R $JBOSS_HOME/server/all $JBOSS_HOME/server/$CONFIGURATION;
  cp $PETSTORE112_HOME/deploy/login-config.xml $JBOSS_HOME/server/$CONFIGURATION/conf/;
  cp $PETSTORE112_HOME/deploy/jboss-minimal.xml $JBOSS_HOME/server/$CONFIGURATION/conf;
  cp $PETSTORE112_HOME/deploy/jboss-service.xml $JBOSS_HOME/server/$CONFIGURATION/conf/;
  cp $CLOUDSCAPE_HOME/lib/system/cloudscape.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $CLOUDSCAPE_HOME/lib/system/cloudutil.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $CLOUDSCAPE_HOME/lib/cloudscape/cloudclient.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $CLOUDSCAPE_HOME/lib/cloudscape/RmiJdbc.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PETSTORE112_HOME/deploy/petstore.ear $JBOSS_HOME/server/$CONFIGURATION/deploy;
  cat $PETSTORE112_HOME/deploy/cloudscape-service.xml.in \
       | sed 's/\$CLOUDSCAPEHOST\$/'$CLOUDSCAPE_HOSTNAME/ > \
       $JBOSS_HOME/server/$CONFIGURATION/deploy/cloudscape-service.xml
  cp $PETSTORE112_HOME/deploy/emk-util.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PINPOINT_HOME/dist/lib/pinpoint.jar $JBOSS_HOME/lib;
  cp $PINPOINT_HOME/dist/lib/pinpoint.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PINPOINT_HOME/lib/xercesImpl.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PINPOINT_HOME/lib/xmlParserAPIs.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
done

# and make copies for the web frontend
for i in $(seq 1 $2); do 
  CONFIGURATION=petstore-frontend-$i;
  rm -rf $JBOSS_HOME/server/$CONFIGURATION
  cp -R $JBOSS_HOME/server/all $JBOSS_HOME/server/$CONFIGURATION;
  cp $PETSTORE112_HOME/deploy/login-config.xml $JBOSS_HOME/server/$CONFIGURATION/conf/;
  cp $PETSTORE112_HOME/deploy/jboss-minimal.xml $JBOSS_HOME/server/$CONFIGURATION/conf;
  cp $PETSTORE112_HOME/deploy/jboss-service.xml $JBOSS_HOME/server/$CONFIGURATION/conf/;
  cp $CLOUDSCAPE_HOME/lib/system/cloudscape.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $CLOUDSCAPE_HOME/lib/system/cloudutil.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $CLOUDSCAPE_HOME/lib/cloudscape/cloudclient.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $CLOUDSCAPE_HOME/lib/cloudscape/RmiJdbc.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PETSTORE112_HOME/deploy/petstore-web.ear $JBOSS_HOME/server/$CONFIGURATION/deploy;
  cat $PETSTORE112_HOME/deploy/cloudscape-service.xml.in \
       | sed 's/\$CLOUDSCAPEHOST\$/'$CLOUDSCAPE_HOSTNAME/ > \
       $JBOSS_HOME/server/$CONFIGURATION/deploy/cloudscape-service.xml
  cp $PETSTORE112_HOME/deploy/emk-util.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PINPOINT_HOME/dist/lib/pinpoint.jar $JBOSS_HOME/lib;
  cp $PINPOINT_HOME/dist/lib/pinpoint.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PINPOINT_HOME/lib/xercesImpl.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
  cp $PINPOINT_HOME/lib/xmlParserAPIs.jar $JBOSS_HOME/server/$CONFIGURATION/lib;
done
