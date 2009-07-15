#!/bin/sh
#
# Copyright (c) 1998 by Sun Microsystems, Inc.
#
# $Id: schema_C.sh,v 1.2 2003/03/22 04:55:01 emrek Exp $
# 
#
# Script to create corp tables
#
# Tom Daly Jan 2000 
#     modify script to build corp specific 
#     tables and tablespaces against an existing database
#     This is to allow all of ECperf to run on one database
#     which will allow for easier testing and installation.
#     schema_C.sh now takes database name and database directory 
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
	echo "Usage: schema_C.sh <database_name> <database dir>"
	exit 1
fi

DB=$1
DB_DIR=$2
ORACLE_SID=$DB
export ORACLE_SID

######  datafiles #####

CUST=${DB_DIR}/C_cust
SUPP=${DB_DIR}/C_supp
SITE=${DB_DIR}/C_site
PARTS=${DB_DIR}/C_parts


sqlplus <<EOT  
CONNECT / as sysdba
shutdown
startup pfile=${ORACLE_HOME}/dbs/p_build_${DB}.ora
EOT

##############################################
#  clean up tablespaces and 
#  datafiles from  earlier attempts
##############################################
echo "Cleaning up old tables spaces"
sqlplus <<EOT 
CONNECT / as sysdba
DROP TABLESPACE C_cust_space INCLUDING CONTENTS;
DROP TABLESPACE C_supp_space INCLUDING CONTENTS;
DROP TABLESPACE C_site_space INCLUDING CONTENTS;
DROP TABLESPACE C_parts_space INCLUDING CONTENTS;
EOT

echo "Removing Datafiles"
rm -f $CUST $SUPP $SITE $PARTS


##############################################
# Create the required tablespaces
##############################################
sqlplus <<EOT
CONNECT / as sysdba
CREATE TABLESPACE C_cust_space
	DATAFILE '${CUST}' SIZE 50M 
	DEFAULT STORAGE ( INITIAL 10M NEXT 1M MAXEXTENTS UNLIMITED PCTINCREASE 0);
EXIT
EOT

sqlplus <<EOT
CONNECT / as sysdba
CREATE TABLESPACE C_supp_space
	DATAFILE '${SUPP}' SIZE 40M 
	DEFAULT STORAGE ( INITIAL 1M NEXT 1M);
EXIT
EOT

sqlplus <<EOT
CONNECT / as sysdba
CREATE TABLESPACE C_site_space
	DATAFILE '${SITE}' SIZE 40M 
	DEFAULT STORAGE ( INITIAL 1M NEXT 100K);
EXIT
EOT

sqlplus <<EOT
CONNECT / as sysdba
CREATE TABLESPACE C_parts_space
	DATAFILE '${PARTS}' SIZE 40M 
	DEFAULT STORAGE ( INITIAL 2M NEXT 100K);
EXIT
EOT


wait


##############################################
# Running scripts to create schema and indexes
##############################################
sqlplus ecperf/ecperf <<EOT
@sql/schema_C
EOT
