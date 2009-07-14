#!/bin/sh

source config.env

OLDPWD=$(pwd)

# modify cloudscape-service.xml

cat $PETSTORE_HOME/clustering/cloudscape-service.xml.in \
  | sed 's/\$CLOUDSCAPEHOST\$/'$DB_MACHINE/ > \
  $PETSTORE_HOME/clustering/cloudscape-service.xml


# run prepare-petstore.sh with NUM_BACKENDS and NUM_FRONTEND

cd $PETSTORE_HOME/clustering
./prepare-petstore-cluster.sh $NUM_BACKENDS $NUM_FRONTENDS
cd $OLDPWD


