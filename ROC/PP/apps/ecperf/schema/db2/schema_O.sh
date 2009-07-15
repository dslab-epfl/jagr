#!/bin/sh


if [ $# -ne 2 ]
then
   echo "Usage: schema_O.sh <database_name> <database dir>"
   exit 1
fi

DB=$1
DB_DIR=$2

######  datafiles #####
CUST=${DB_DIR}/O_cust
ORDS=${DB_DIR}/O_ords
ITEM=${DB_DIR}/O_item
ORDL=${DB_DIR}/O_ordl

##############################################
#  clean up datafiles from  earlier attempts
##############################################
db2 -v connect to ${DB}
echo "Cleaning up old table spaces"
db2 -v DROP TABLESPACE O_cust_space
db2 -v DROP TABLESPACE O_ords_space
db2 -v DROP TABLESPACE O_ordl_space
db2 -v DROP TABLESPACE O_item_space

# echo "Removing Datafiles"
rm -f $CUST > /dev/null 2>&1
rm -f $ORDS > /dev/null 2>&1
rm -f $ITEM > /dev/null 2>&1
rm -f $ORDL > /dev/null 2>&1

##############################################
# Create the required tablespaces
##############################################
echo "Creating O table spaces and tables..."
# Using SMS for now (until more precise numbers are known)

db2 -v "CREATE TABLESPACE O_cust_space \
        managed by system using \
        ('$CUST') \
        extentsize 1M prefetchsize 1M"

db2 -v "CREATE TABLESPACE O_ords_space \
        managed by system using \
        ('$ORDS') \
        extentsize 1M prefetchsize 1M"

db2 -v "CREATE TABLESPACE O_ordl_space \
        managed by system using \
        ('$ORDL') \
        extentsize 1M prefetchsize 1M"

db2 -v "CREATE TABLESPACE O_item_space \
        managed by system using \
        ('$ITEM') \
        extentsize 1M prefetchsize 100K"
db2 -v connect reset


##############################################
# Running scripts to create schema and indexes
##############################################
db2 -v connect to ${DB}
db2 -tvf sql/schema_O.sql
db2 -v connect reset

