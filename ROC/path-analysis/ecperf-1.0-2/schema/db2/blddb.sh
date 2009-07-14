#!/bin/sh


if [ $# -ne 2 ]
then
   echo "Usage: blddb.sh <database_name> <database dir>"
   exit 1
fi

DB=$1
DB_DIR=$2

db2 -v db2start

# Create the DB
createdb.sh ${DB} ${DB_DIR}

# Create the tablespaces and tables
schema_C.sh ${DB} ${DB_DIR}
schema_M.sh ${DB} ${DB_DIR}
schema_O.sh ${DB} ${DB_DIR}
schema_S.sh ${DB} ${DB_DIR}

# Create the (U??) tablespace and table
schema_U.sh ${DB} ${DB_DIR}

