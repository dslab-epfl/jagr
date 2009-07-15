#!/bin/sh
#
# Copyright (c) 1998 by Sun Microsystems, Inc.
#
# $Id: schema_O.sh,v 1.2 2003/03/22 04:55:01 emrek Exp $
#
# script to create the orders tables
# 
# Tom Daly Jan 2000 
#     modify script to build corp specific 
#     tables and tablespaces against an existing database
#     This is to allow all of ECperf to run on one database
#     which will allow for easier testng and installation.
#     schema_O.sh now takes database name and database directory 
#     parameters.
#     Note: The database must now exist prior to running this script
#     use the createdb.sh script to create it.
#                     

if [ "$ORACLE_HOME" = "" ]
then
	echo "$ORACLE_HOME"
	echo "You must set the ORACLE_HOME environment variable"
	exit 1
fi

if [ $# -ne 2 ]
then
	echo "Usage: schema_O.sh <database_name> <database dir>"
	exit 1
fi

DB=$1
DB_DIR=$2
ORACLE_SID=$DB
export ORACLE_SID


######  datafiles #####
CUST=${DB_DIR}/O_cust
ORDS=${DB_DIR}/O_ords
ITEM=${DB_DIR}/O_item
ORDL=${DB_DIR}/O_ordl

sqlplus <<EOT  
CONNECT / as sysdba
shutdown
startup pfile=${ORACLE_HOME}/dbs/p_build_${DB}.ora
EOT

##############################################
#  clean up datafiles from  earlier attempts
##############################################
echo "Cleaning up old tables spaces"
sqlplus <<EOT 
CONNECT / as sysdba
DROP TABLESPACE O_cust_space INCLUDING CONTENTS;
DROP TABLESPACE O_ords_space INCLUDING CONTENTS;
DROP TABLESPACE O_ordl_space INCLUDING CONTENTS;
DROP TABLESPACE O_item_space INCLUDING CONTENTS;
EOT

rm -f ${CUST} ${ORDS} ${ITEM} ${ORDL} 

##############################################
# Create the required tablespaces
##############################################
# already up
#sqlplus <<EOT  
#CONNECT / as sysdba
#startup pfile=${ORACLE_HOME}/dbs/p_build_${DB}.ora
#EOT


sqlplus <<EOT 
CONNECT / as sysdba
CREATE TABLESPACE O_cust_space
	DATAFILE '${CUST}' SIZE 50M
	DEFAULT STORAGE ( INITIAL 6M NEXT 1M MAXEXTENTS UNLIMITED PCTINCREASE 0);
EXIT
EOT

sqlplus <<EOT
CONNECT / as sysdba
CREATE TABLESPACE O_ords_space
	DATAFILE '${ORDS}' SIZE 200M
	DEFAULT STORAGE ( INITIAL 20M NEXT 1M MAXEXTENTS UNLIMITED PCTINCREASE 0);
EXIT
EOT

sqlplus <<EOT
CONNECT / as sysdba
CREATE TABLESPACE O_ordl_space
	DATAFILE '${ORDL}' SIZE 200M
	DEFAULT STORAGE ( INITIAL 20M NEXT 1M MAXEXTENTS UNLIMITED PCTINCREASE 0);
EXIT
EOT

sqlplus <<EOT
CONNECT / as sysdba
CREATE TABLESPACE O_item_space
	DATAFILE '${ITEM}' SIZE 40M
	DEFAULT STORAGE ( INITIAL 3M NEXT 100K);
EXIT
EOT


wait


##############################################
# Running scripts to create schema and indexes
##############################################
sqlplus ecperf/ecperf <<EOT
@sql/schema_O
EOT
