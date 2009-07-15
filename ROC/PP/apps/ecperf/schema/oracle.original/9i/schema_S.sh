#!/bin/sh
#
# Copyright (c) 1998 by Sun Microsystems, Inc.
#
# $Id: schema_S.sh,v 1.2 2003/03/22 04:55:01 emrek Exp $
#
# Script to create supp database

# Create init.ora tables for SUPP database


# Tom Daly Jan 2000 
#     modify script to build Supplier specific 
#     tables and tablespaces against an existing database
#     This is to allow all of ECperf to run on one database
#     which will allow for easier testing and installation.
#     schema_S.sh now takes database name and database directory 
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
	echo "Usage: schema_S.sh <database_name> <database dir>"
	exit 1
fi

DB=$1
DB_DIR=$2
ORACLE_SID=$DB
export ORACLE_SID


######  datafiles #####
COMP=${DB_DIR}/S_comp
SC=${DB_DIR}/S_suppcomp
SITE=${DB_DIR}/S_site
SUPP=${DB_DIR}/S_supp
PO=${DB_DIR}/S_po

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
DROP TABLESPACE S_comp_space INCLUDING CONTENTS;
DROP TABLESPACE S_sc_space INCLUDING CONTENTS;
DROP TABLESPACE S_po_space INCLUDING CONTENTS;
DROP TABLESPACE S_site_space INCLUDING CONTENTS;
DROP TABLESPACE S_supp_space INCLUDING CONTENTS;
EOT

rm -f $COMP $SC $SITE $SUPP $PO $BIDS

##############################################
# Create the required tablespaces
##############################################

sqlplus <<EOT
CONNECT / as sysdba
CREATE TABLESPACE S_comp_space
	DATAFILE '${COMP}' SIZE 90M
	DEFAULT STORAGE ( INITIAL 6M NEXT 100K);
EXIT
EOT

sqlplus <<EOT
CONNECT / as sysdba
CREATE TABLESPACE S_sc_space
	DATAFILE '${SC}' SIZE 90M
	DEFAULT STORAGE ( INITIAL 6M NEXT 100K);	
EXIT
EOT

sqlplus <<EOT
CONNECT / as sysdba
CREATE TABLESPACE S_po_space
	DATAFILE '${PO}' SIZE 90M
	DEFAULT STORAGE ( INITIAL 6M NEXT 1M MAXEXTENTS UNLIMITED PCTINCREASE 0);	
EXIT
EOT

sqlplus <<EOT
CONNECT / as sysdba
CREATE TABLESPACE S_site_space
	DATAFILE '${SITE}' SIZE 10M
	DEFAULT STORAGE ( INITIAL 200K NEXT 100K);
EXIT
EOT

sqlplus <<EOT
CONNECT / as sysdba
CREATE TABLESPACE S_supp_space
	DATAFILE '${SUPP}' SIZE 10M
	DEFAULT STORAGE ( INITIAL 200K NEXT 100K);
EXIT
EOT

wait

##############################################
# Running scripts to create schema and indexes
##############################################
sqlplus ecperf/ecperf <<EOT
@sql/schema_S
EOT
