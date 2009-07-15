#!/bin/sh
#
# Copyright (c) 1998 by Sun Microsystems, Inc.
#
# $Id: create_init.sh,v 1.2 2003/03/22 04:55:01 emrek Exp $
#
# create_init.sh <db>
# Script to create the init.ora files for a particular database

if [ $# -ne 2 ]
then
	echo "Usage: create_init.sh <database_name> <database dir>"
	exit 1
fi

DB=$1
DB_DIR=$2
T=/tmp/c$$

echo "Creating p_create_${DB}.ora"
cp p_create_templ.ora $T
chmod u+w $T
sed "s/DBNAME/$DB/" $T | sed "s'cntrl.dbf'${DB_DIR}/cntrl${DB}'" \
	> ${ORACLE_HOME}/dbs/p_create_${DB}.ora

echo "Creating p_build_${DB}.ora"
cp p_build_templ.ora $T
chmod u+w $T
sed "s/DBNAME/$DB/" $T | sed "s'cntrl.dbf'${DB_DIR}/cntrl${DB}'" \
	> ${ORACLE_HOME}/dbs/p_build_${DB}.ora

echo "Creating init${DB}.ora"
cp init_templ.ora $T
chmod u+w $T
sed "s/DBNAME/$DB/" $T | sed "s'cntrl.dbf'${DB_DIR}/cntrl${DB}'" \
	> ${ORACLE_HOME}/dbs/init${DB}.ora

rm -f $T
