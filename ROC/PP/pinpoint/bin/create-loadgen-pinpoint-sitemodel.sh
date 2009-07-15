#!/bin/sh

export BASEDIR=$(dirname $0)

cd $BASEDIR

source config.env

ORIGDIR=$(pwd)
PINPOINT_JAR=$PINPOINT_HOME/dist/lib/pinpoint.jar

cd $PINPOINT_HOME/lib

for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

CLASSPATH=$CLASSPATH:$PINPOINT_JAR
cd $ORIGDIR

NUMTIERS=5
NUMCOMPONENTSMIN=5
NUMCOMPONENTSMAX=20
NUMMACHINESMIN=20
NUMMACHINESMAX=80
CONNECTEDMIN=3
CONNECTEDMAX=10

C="roc.pinpoint.loadgen.model.SiteTemplate --numtiers $NUMTIERS --numcomponentsmin $NUMCOMPONENTSMIN --numcomponentsmax $NUMCOMPONENTSMAX --nummachinesmin $NUMMACHINESMIN --nummachinesmax $NUMMACHINESMAX --connectednessmin $CONNECTEDMIN --connectednessmax $CONNECTEDMAX $@"

echo "Running: $C"
 
java $JAVA_OPTS -Xss128M -Xmx512M -cp $CLASSPATH $C




