#!/bin/sh


if [ $# -ne 2 ]
then
   echo "Usage: schema_U.sh <database_name> <database dir>"
   exit 1
fi

DB=$1
DB_DIR=$2

######  datafiles #####

UTABS=${DB_DIR}/U_tables

##############################################
#  clean up tablespaces and
#  datafiles from  earlier attempts
##############################################
db2 -v connect to ${DB}
echo "Cleaning up old table spaces"
db2 -v DROP TABLESPACE U_space

# echo "Removing Datafiles"
rm -f $UTABS > /dev/null 2>&1


##############################################
# Create the required tablespaces
##############################################
echo "Creating U table space and table..."
# Using SMS for now (until more precise numbers are known)

db2 -v "CREATE TABLESPACE U_space \
       managed by system using \
       ('$UTABS') \
       extentsize 10K prefetchsize 10K"
db2 -v connect reset

##############################################
# Running scripts to create schema and indexes
##############################################
db2 -v connect to ${DB}
db2 -tvf sql/schema_U.sql
db2 -v connect reset

