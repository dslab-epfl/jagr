#!/bin/sh

if [ -z $RUBIS ]; then
  echo Please define RUBIS
  exit 1
fi

if [ -z $JBOSS_HOME ]; then
  echo Please define JBOSS_HOME
  exit 1
fi


cp $RUBIS/EJB_EntityBean_id_BMP/rubis.jar $JBOSS_HOME/server/default/deploy
cp $RUBIS/EJB_EntityBean_id_BMP/ejb_rubis_web.war $JBOSS_HOME/server/default/deploy
cp $RUBIS/jboss_config/mysql-service.xml $JBOSS_HOME/server/default/deploy
cp $RUBIS/jboss_config/login-config.xml $JBOSS_HOME/server/default/conf
cp $RUBIS/jboss_config/mm.mysql-2.0.12-bin.jar $JBOSS_HOME/server/default/lib
