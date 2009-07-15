#!/bin/sh

if [ -z $JBOSS_HOME ]; then
  echo Please set JBOSS_HOME
  exit 1;
fi;
                                                                                                                             
if [ -z $PP_ROOT ]; then
  echo Please set PP_ROOT
  exit 1;
fi;
                                                                                                                            export ECPERF_HOME=$PP_ROOT/apps/ecperf
export PINPOINT_HOME=$PP_ROOT/pinpoint
export ANT=$PP_ROOT/pp-jboss/tools/bin/ant

export ECPERFCONFIG=ecperf
export BASECONFIG=all

export OLDDIR=$(pwd)

cd $ECPERF_HOME

echo Removing old postgresql db
rm -rf $ECPERF_HOME/datadir

echo And restoring a freshly populated db
tar -zxf $ECPERF_HOME/jboss/ecperf-data-postgresql.tar.gz

echo Removing the old Ecperf configuration in JBoss
rm -rf $JBOSS_HOME/server/$ECPERFCONFIG

echo Duplicating JBoss ALL configuration
cp -R $JBOSS_HOME/server/$BASECONFIG $JBOSS_HOME/server/$ECPERFCONFIG

###
# copy pinpoint.jar and dependencies
###
cp $PINPOINT_HOME/dist/lib/pinpoint.jar $JBOSS_HOME/server/$ECPERFCONFIG/lib
cp $PINPOINT_HOME/lib/xercesImpl.jar $JBOSS_HOME/server/$ECPERFCONFIG/lib
cp $PINPOINT_HOME/lib/xmlParserAPIs.jar $JBOSS_HOME/server/$ECPERFCONFIG/lib
                                                                                                                            
###
# copy configuration modifications
###
cp $ECPERF_HOME/jboss/cluster-service.xml $JBOSS_HOME/server/$ECPERFCONFIG/deploy
cp $ECPERF_HOME/jboss/jboss-service.xml $JBOSS_HOME/server/$ECPERFCONFIG/conf

echo Deploying Ecperf
$ANT deploy

cd $OLDDIR
