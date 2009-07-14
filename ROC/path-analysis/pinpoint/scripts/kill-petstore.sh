#!/bin/sh

source config.env

# run cloudscape db
echo Killing cloudscape database on $DB_MACHINE
export GEXEC_SVRS=$DB_MACHINE
gexec -n 1 killall cloudscape
gexec -n 1 killall java

# run observation machine as a backend
echo Killing observation machine on $OBSERVATION_MACHINE
export GEXEC_SVRS=$OBSERVATION_MACHINE
gexec -n 1 killall run-jboss.sh
gexec -n 1 killall -9 java

# todo: ? start analysis engine?

echo Killing backend machines...

# run backend machines with gexec
count=2
for i in $BACKEND_MACHINES; do
  echo "    ...killing backend-$count on $i"
  export GEXEC_SVRS=$i;
  gexec -n 1 killall run-jboss.sh
  gexec -n 1 killall -9 java
  count=$(($count+1));
done

echo Killing frontend machines...

# run frontend machines
count=1
for i in $FRONTEND_MACHINES; do
  echo "    ...killing frontend-$count on $i"
  export GEXEC_SVRS=$i;
  gexec -n 1 killall run-jboss.sh
  gexec -n 1 killall -9 java
  count=$(($count+1));
done



