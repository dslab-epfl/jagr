#!/bin/sh

# Modify these to use it other setups
# interval for stats is set in run.properties
APP_SERVER=localhost
DB_SERVER=localhost

START_TIME=$1
DURATION=$2
INTERVAL=$3
COUNT=$4
OUTPUT_DIR=$5

# Sleep till the start time

cat "Start time : After ${START_TIME} sec & Duration : ${DURATION} sec" >  $OUTPUT_DIR/stat.log

sleep $1

# Execute these to get the data
rsh -n $APP_SERVER statit sleep $DURATION > $OUTPUT_DIR/appserver.statit.log &
rsh -n $DB_SERVER statit sleep $DURATION > $OUTPUT_DIR/dbserver.statit.log &

rsh -n $APP_SERVER "kstat -n hme1 > /tmp/tcp1; sleep $DURATION; kstat -n hme1 > /tmp/tcp2" &

rsh -n $DB_SERVER "kstat -n hme1 > /tmp/tcp_db1; sleep $DURATION; kstat -n hme1 > /tmp/tcp_db2" &

rsh -n $APP_SERVER mpstat $INTERVAL $COUNT > $OUTPUT_DIR/appserver.mpstat.log &
rsh -n $DB_SERVER mpstat $INTERVAL $COUNT > $OUTPUT_DIR/dbserver.mpstat.log &

rsh -n $APP_SERVER iostat -xn $INTERVAL $COUNT > $OUTPUT_DIR/appserver.iostat.log &
rsh -n $DB_SERVER iostat -xn $INTERVAL $COUNT > $OUTPUT_DIR/dbserver.iostat.log &

rsh -n $APP_SERVER netstat -i $INTERVAL $COUNT > $OUTPUT_DIR/appserver.netstat.log &
rsh -n $DB_SERVER netstat -i $INTERVAL $COUNT > $OUTPUT_DIR/dbserver.netstat.log &

statit sleep $DURATION > $OUTPUT_DIR/driver.statit.log &
mpstat $INTERVAL $COUNT > $OUTPUT_DIR/driver.mpstat.log &
iostat -xn $INTERVAL $COUNT > $OUTPUT_DIR/driver.iostat.log &
netstat -i $INTERVAL $COUNT > $OUTPUT_DIR/driver..netstat.log &

sleep $DURATION
rcp $APP_SERVER:/tmp/tcp1 $OUTPUT_DIR/appserver.tcp1.log
rcp $APP_SERVER:/tmp/tcp2 $OUTPUT_DIR/appserver.tcp2.log
rcp $DB_SERVER:/tmp/tcp_db1 $OUTPUT_DIR/dbserver.tcp1.log
rcp $DB_SERVER:/tmp/tcp_db2 $OUTPUT_DIR/dbserver.tcp2.log
exit 0
