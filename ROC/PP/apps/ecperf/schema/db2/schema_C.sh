#!/bin/sh


if [ $# -ne 2 ]
then
   echo "Usage: schema_C.sh <database_name> <database dir>"
   exit 1
fi

DB=$1
DB_DIR=$2

######  datafiles #####

CUST=${DB_DIR}/C_cust
SUPP=${DB_DIR}/C_supp
SITE=${DB_DIR}/C_site
PARTS=${DB_DIR}/C_parts


##############################################
#  clean up tablespaces and
#  datafiles from  earlier attempts
##############################################
db2 -v connect to ${DB}
echo "Cleaning up old table spaces"
db2 -v DROP TABLESPACE C_cust_space
db2 -v DROP TABLESPACE C_supp_space
db2 -v DROP TABLESPACE C_site_space
db2 -v DROP TABLESPACE C_parts_space
db2 -v connect reset

#echo "Removing Datafiles"
rm -f $CUST > /dev/null 2>&1
rm -f $SUPP > /dev/null 2>&1
rm -f $SITE > /dev/null 2>&1
rm -f $PARTS > /dev/null 2>&1


##############################################
# Create the required tablespaces
##############################################
echo "Creating C table spaces and tables..."
# Using SMS for now (until more precise numbers are known)

db2 -v connect to ${DB}
db2 -v "CREATE TABLESPACE C_cust_space \
       managed by system using \
       ('%CUST%') \
       extentsize 500K prefetchsize 1M"

db2 -v "CREATE TABLESPACE C_supp_space \
       managed by system using \
       ('%SUPP%') \
       extentsize 100K prefetchsize 100K"

db2 -v "CREATE TABLESPACE C_site_space \
       managed by system using \
       ('%SITE%') \
       extentsize 100K prefetchsize 100K"

db2 -v "CREATE TABLESPACE C_parts_space \
        managed by system using \
        ('%PARTS%') \
        extentsize 500K prefetchsize 100K"
db2 -v connect reset


##############################################
# Running scripts to create schema and indexes
##############################################
db2 -v connect to ${DB}
db2 -tvf sql/schema_C.sql
db2 -v connect reset

