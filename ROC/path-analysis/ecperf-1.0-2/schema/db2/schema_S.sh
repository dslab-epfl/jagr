#!/bin/sh


if [ $# -ne 2 ]
then
   echo "Usage: schema_S.sh <database_name> <database dir>"
   exit 1
fi

DB=$1
DB_DIR=$2


######  datafiles #####
COMP=${DB_DIR}/S_comp
SC=${DB_DIR}/S_suppcomp
SITE=${DB_DIR}/S_site
SUPP=${DB_DIR}/S_supp
PO=${DB_DIR}/S_po


##############################################
#  clean up datafiles from  earlier attempts
##############################################
db2 -v connect to ${DB}
echo "Cleaning up old table spaces"
db2 -v DROP TABLESPACE S_comp_space
db2 -v DROP TABLESPACE S_sc_space
db2 -v DROP TABLESPACE S_po_space
db2 -v DROP TABLESPACE S_site_space
db2 -v DROP TABLESPACE S_supp_space

# echo "Removing Datafiles"
rm -f $COMP > /dev/null 2>&1
rm -f $SC > /dev/null 2>&1
rm -f $SITE > /dev/null 2>&1
rm -f $SUPP > /dev/null 2>&1
rm -f $PO > /dev/null 2>&1
rm -f $BIDS > /dev/null 2>&1

##############################################
# Create the required tablespaces
##############################################
echo "Creating S table spaces and tables..."
# Using SMS for now (until more precise numbers are known)

db2 -v "CREATE TABLESPACE S_comp_space \
        managed by system using \
        ('$COMP') \
        extentsize 1M prefetchsize 100K"

db2 -v "CREATE TABLESPACE S_sc_space \
        managed by system using \
        ('$SC') \
        extentsize 1M prefetchsize 100K"

db2 -v "CREATE TABLESPACE S_po_space \
        managed by system using \
        ('$PO') \
        extentsize 1M prefetchsize 1M"

db2 -v "CREATE TABLESPACE S_site_space \
        managed by system using \
        ('$SITE') \
        extentsize 100K prefetchsize 100K"

db2 -v "CREATE TABLESPACE S_supp_space \
        managed by system using \
        ('$SUPP') \
        extentsize 100K prefetchsize 100K"

db2 -v connect reset


##############################################
# Running scripts to create schema and indexes
##############################################
db2 -v connect to ${DB}
db2 -tvf sql/schema_S.sql
db2 -v connect reset

