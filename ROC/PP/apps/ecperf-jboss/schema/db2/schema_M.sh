#!/bin/sh


if [ $# -ne 2 ]
then
   echo "Usage: schema_M.sh <database_name> <database dir>"
   exit 1
fi

DB=$1
DB_DIR=$2

######  datafiles #####
PART=${DB_DIR}/M_parts
BOM=${DB_DIR}/M_bom
WO=${DB_DIR}/M_wrkorder
LO=${DB_DIR}/M_lrgorder
INV=${DB_DIR}/M_inv

##############################################
#  clean up datafiles from  earlier attempts
##############################################
db2 -v connect to ${DB}
echo "Cleaning up old table spaces"
db2 -v DROP TABLESPACE M_parts_space
db2 -v DROP TABLESPACE M_bom_space
db2 -v DROP TABLESPACE M_wo_space
db2 -v DROP TABLESPACE M_lo_space
db2 -v DROP TABLESPACE M_inv_space

#echo "Removing Datafiles"
rm -f $BOM > /dev/null 2>&1
rm -f $WO > /dev/null 2>&1
rm -f $LO > /dev/null 2>&1
rm -f $INV > /dev/null 2>&1
rm -f $PART > /dev/null 2>&1

##############################################
# Create the required tablespaces
##############################################
echo "Creating M table spaces and tables..."
# Using SMS for now (until more precise numbers are known)

db2 -v "CREATE TABLESPACE M_parts_space \
        managed by system using \
        ('$PART') \
        extentsize 1M prefetchsize 100K"

db2 -v "CREATE TABLESPACE M_bom_space \
        managed by system using \
        ('$BOM') \
        extentsize 1M prefetchsize 100K"

db2 -v "CREATE TABLESPACE M_wo_space \
        managed by system using \
        ('$WO') \
        extentsize 1M prefetchsize 1M"

db2 -v "CREATE TABLESPACE M_lo_space \
        managed by system using \
        ('$LO') \
        extentsize 1M prefetchsize 100K"

db2 -v "CREATE TABLESPACE M_inv_space \
        managed by system using \
        ('$INV') \
        extentsize 1M prefetchsize 100K"
db2 -v connect reset


##############################################
# Running scripts to create schema and indexes
##############################################
db2 -v connect to ${DB}
db2 -tvf sql/schema_M.sql
db2 -v connect reset

