#!/bin/sh
#
# Copyright (c) 1998 by Sun Microsystems, Inc.
#
# $Id: schema_M.sh,v 1.1.1.1 2003/04/25 08:04:21 mdelgado Exp $
#
# Script to create mfg database

# Tom Daly Jan 2000 
#     modify script to build mfg specific 
#     tables and tablespaces against an existing database
#     This is to allow all of ECperf to run on one database
#     which will allow for easier testing and installation.
#     schema_M.sh now takes database name and database directory 
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
	echo "Usage: schema_M.sh <database_name> <database dir>"
	exit 1
fi

DB=$1
DB_DIR=$2
ORACLE_SID=$DB
export ORACLE_SID

######  datafiles #####
PART=${DB_DIR}/M_parts
BOM=${DB_DIR}/M_bom
WO=${DB_DIR}/M_wrkorder
LO=${DB_DIR}/M_lrgorder
INV=${DB_DIR}/M_inv

svrmgrl lmode=y <<EOT  
CONNECT internal
shutdown
startup pfile=${ORACLE_HOME}/dbs/p_build_${DB}.ora
EOT

##############################################
#  clean up datafiles from  earlier attempts
##############################################
echo "Cleaning up old tables spaces"
svrmgrl lmode=y <<EOT 
CONNECT system/manager 
DROP TABLESPACE M_parts_space INCLUDING CONTENTS;
DROP TABLESPACE M_bom_space INCLUDING CONTENTS;
DROP TABLESPACE M_wo_space INCLUDING CONTENTS;
DROP TABLESPACE M_lo_space INCLUDING CONTENTS;
DROP TABLESPACE M_inv_space INCLUDING CONTENTS;
EOT

echo "Removing Datafiles"
rm -f $BOM $WO $LO $INV $PART


##############################################
# Create the required tablespaces
##############################################
svrmgrl lmode=y <<EOT  &
CONNECT system/manager
CREATE TABLESPACE M_parts_space
	DATAFILE '${PART}' SIZE 6M
	DEFAULT STORAGE ( INITIAL 2M NEXT 100K);
EXIT
EOT

svrmgrl lmode=y <<EOT  &
CONNECT system/manager
CREATE TABLESPACE M_bom_space
	DATAFILE '${BOM}' SIZE 4M
	DEFAULT STORAGE ( INITIAL 2M NEXT 100K);
EXIT
EOT

svrmgrl lmode=y <<EOT  &
CONNECT system/manager
CREATE TABLESPACE M_wo_space
	DATAFILE '${WO}' SIZE 4M
	DEFAULT STORAGE ( INITIAL 2M NEXT 1M MAXEXTENTS UNLIMITED PCTINCREASE 0);
EXIT
EOT

svrmgrl lmode=y <<EOT  &
CONNECT system/manager
CREATE TABLESPACE M_lo_space
	DATAFILE '${LO}' SIZE 4M
	DEFAULT STORAGE ( INITIAL 2M NEXT 100K MAXEXTENTS UNLIMITED PCTINCREASE 0);
EXIT
EOT

svrmgrl lmode=y <<EOT  &
CONNECT system/manager
CREATE TABLESPACE M_inv_space
	DATAFILE '${INV}' SIZE 4M
	DEFAULT STORAGE ( INITIAL 2M NEXT 100K);
EXIT
EOT


wait

##############################################
# Running scripts to create schema and indexes
##############################################
svrmgrl <<EOT
CONNECT ecperf/ecperf
@sql/schema_M
EOT
