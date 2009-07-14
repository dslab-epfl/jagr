#!/bin/sh

source config.env

# run cloudscape db
echo Starting cloudscape database on $DB_MACHINE
export GEXEC_SVRS=$DB_MACHINE
gexec -v -d -n 1 $J2EE_HOME/bin/cloudscape -start


# run observation machine as a backend
echo Starting observation machine on $OBSERVATION_MACHINE
export GEXEC_SVRS=$OBSERVATION_MACHINE
gexec -v -d -n 1 $PINPOINT_HOME/scripts/run-jboss.sh petstore-backend-1

echo "Waiting for 30 seconds..."
sleep 30

# todo: ? start analysis engine?

echo Starting backend machines...

# run backend machines with gexec
count=2
for i in $BACKEND_MACHINES; do
  echo "    ...spawning backend-$count on $i"
  export GEXEC_SVRS=$i;
  gexec -v -d -n 1 $PINPOINT_HOME/scripts/run-jboss.sh petstore-backend-$count;
  count=$(($count+1));
done

echo Starting frontend machines...

# run frontend machines
count=1
for i in $FRONTEND_MACHINES; do
  echo "    ...spawning frontend-$count on $i"
  export GEXEC_SVRS=$i;
  gexec -v -d -n 1 $PINPOINT_HOME/scripts/run-jboss.sh petstore-frontend-$count;
  count=$(($count+1));
done



