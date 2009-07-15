#!/bin/sh

###############################
# Script to create DB2 database
###############################

##################
# check Parameters
##################

if [ $# -ne 2 ]
then
   echo "Usage: createdb.sh <database_name> <database dir>"
   exit 1
fi

####################
# Set the database and database directory vars
#####################
DB=$1
DB_DIR=$2


####################################
# Environment vars an params look ok
# so go build the database
####################################

SYS=${DB_DIR}/sys_${DB}
LOG1=${DB_DIR}/log1_${DB}
LOG2=${DB_DIR}/log2_${DB}
ROLL=${DB_DIR}/roll_${DB}
TEMP=${DB_DIR}/temp_${DB}

#echo Deleting existing $SYS...
rm -f $SYS > /dev/null 2>&1
#echo Deleting existing $LOG1...
rm -f $LOG1 > /dev/null 2>&1
#echo Deleting existing $LOG2...
rm -f $LOG2 > /dev/null 2>&1
#echo Deleting existing $ROLL...
rm -f $ROLL > /dev/null 2>&1
#echo Deleting existing $TEMP...
rm -f $TEMP > /dev/null 2>&1


################
# Create database
################
# Using SMS for now (until more precise numbers are known)

db2 -v drop database ${DB}
db2 -v "create database ${DB} on ${DB_DIR} \
       TEMPORARY TABLESPACE \
       managed by system using \
       ('$TEMP')"



#############################
# Create the ROLL space
#############################
# Using SMS for now (until more precise numbers are known)

db2 -v connect to ${DB}
db2 -v "CREATE TABLESPACE roll_space \
       managed by system using \
       ('$ROLL') \
       extentsize 200 prefetchsize 100K"
db2 -v connect reset

########################
# END OF DATABASE BUILD
########################

