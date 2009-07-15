#!/bin/sh

if [ -z $PP_ROOT ]; then
  echo Please define PP_ROOT
  exit 1;
fi;

if [ -z $1 ]; then
  echo Usage: run-petstore.sh observationsfile
  exit
fi;

source config.env

export CLOUDSCAPE_HOSTNAME=$DB_MACHINE
export OBSERVATION_HOSTNAME=$OBSERVATION_MACHINE

export CLOUDSCAPE_HOME=$PP_ROOT/apps/cloudscape
export PETSTORE112_HOME=$PP_ROOT/apps/petstore-1.1.2
export JBOSS_HOME=$PP_ROOT/pp-jboss/build/output/jboss-3.0.6
export OUTPUT=$1

echo killing any old instances of petstore
./kill-petstore.sh

echo Deploying petstore...
./prepare-petstore-cluster.sh $NUM_BACKENDS $NUM_FRONTENDS

echo Restoring a fresh Cloudscape Estore DB
rm -rf $CLOUDSCAPE_HOME/cloudscape/EstoreDB
tar -zxC $PP_ROOT/apps/cloudscape/cloudscape -f $PETSTORE112_HOME/deploy/cloudscape-petstore-1.1.2.tar.gz

# run cloudscape db
echo Starting cloudscape database on $DB_MACHINE
export GEXEC_SVRS=$DB_MACHINE
gexec -v -d -n 1 $CLOUDSCAPE_HOME/bin/cloudscape -start

echo Starting Pinpoint observer on $OBSERVATION_HOSTNAME
export GEXEC_SVRS=$OBSERVATION_HOSTNAME
gexec -v -d -n 1 $PP_ROOT/pinpoint/bin/run-pinpoint.sh ../conf/save-observations-to-disk.conf output=$OUTPUT


echo "Waiting for 15 seconds..."
sleep 15

echo Starting backend machines...

# run backend machines with gexec
count=1
for i in $BACKEND_MACHINES; do
  echo "    ...spawning backend-$count on $i"
  export GEXEC_SVRS=$i;
  gexec -v -d -n 1 $PP_ROOT/expts/bin/start-jboss.sh $JBOSS_HOME  petstore-backend-$count;
  count=$(($count+1));
done


echo Starting frontend machines...

# run frontend machines
count=1
for i in $FRONTEND_MACHINES; do
  echo "    ...spawning frontend-$count on $i"
  export GEXEC_SVRS=$i;
  gexec -v -d -n 1 $PP_ROOT/expts/bin/start-jboss.sh $JBOSS_HOME petstore-frontend-$count;
  count=$(($count+1));
done



