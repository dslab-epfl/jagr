#!/bin/sh
#
# Copyright (c) 1998 by Sun Microsystems, Inc.
#
# $Id: schema_U.sh,v 1.1.1.1 2002/09/18 03:13:12 patriot1burke Exp $
# 
#
# Script to create corp tables
#
# Tom Daly Jan 2000 
#     modify script to build corp specific 
#     tables and tablespaces against an existing database
#     This is to allow all of ECperf to run on one database
#     which will allow for easier testing and installation.
#     schema_U.sh now takes database name and database directory 
#     parameters.
#     Note: The database must now exist, refer to the createdb.sh script
#     to build the database prior to running this script.
#                     

if [ "$ORACLE_HOME" = "" ]
then
	echo "$ORACLE_HOME"
	echo "You must set the ORACLE_HOME environment variable"
	exit 1
fi

if [ $# -ne 2 ]
then
	echo "Usage: schema_U.sh <database_name> <database dir>"
	exit 1
fi

DB=$1
DB_DIR=$2
ORACLE_SID=$DB
export ORACLE_SID

######  datafiles #####

UTABS=${DB_DIR}/U_tables

svrmgrl lmode=y <<EOT  
CONNECT internal
shutdown
startup pfile=${ORACLE_HOME}/dbs/p_build_${DB}.ora
EOT

##############################################
#  clean up tablespaces and 
#  datafiles from  earlier attempts
##############################################
echo "Cleaning up old tables spaces"
svrmgrl lmode=y <<EOT 
CONNECT system/manager 
DROP TABLESPACE U_space INCLUDING CONTENTS;
EOT

echo "Removing Datafiles"
rm -f $UTABS


##############################################
# Create the required tablespaces
##############################################
svrmgrl lmode=y <<EOT
CONNECT system/manager 
CREATE TABLESPACE U_space
	DATAFILE '${UTABS}' SIZE 1M 
	DEFAULT STORAGE ( INITIAL 10K NEXT 10K);
EXIT
EOT

##############################################
# Running scripts to create schema and indexes
##############################################
svrmgrl <<EOT
CONNECT ecperf/ecperf
@sql/schema_U
EOT
