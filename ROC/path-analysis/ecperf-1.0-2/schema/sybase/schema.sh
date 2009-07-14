#!/bin/sh
#
# Copyright (c) 1998 by Sun Microsystems, Inc.
#
# $Id: schema.sh,v 1.1.1.1 2002/11/16 05:35:25 emrek Exp $
# 
#
# Script to create schema for ecperf databases
#
# Arun Patnaik Sep 2000 
#                     

if [ "$SYBASE" = "" ]
then
	echo "$SYBASE"
	echo "You must set the SYBASE environment variable"
	exit 1
fi

if [ "$DSQUERY" = "" ]
then
	echo "$DSQUERY"
	echo "You must set the DSQUERY environment variable"
	exit 1
fi

if [ $# -ne 1 ]
then
	echo "Usage: schema.sh <database_name>"
	echo "db must be corpdb | orderdb | mfgdb | supplierdb | ecperfdb"
	exit 1
fi

DB=$1
if [ "$DB" = "ecperfdb" ] 
then
echo "use $DB" > /tmp/_ecperf_schema_sql.sql
echo "go" >> /tmp/_ecperf_schema_sql.sql
cat sql/schema_C.sql >> /tmp/_ecperf_schema_sql.sql
cat sql/schema_M.sql >> /tmp/_ecperf_schema_sql.sql
cat sql/schema_O.sql >> /tmp/_ecperf_schema_sql.sql
cat sql/schema_S.sql >> /tmp/_ecperf_schema_sql.sql
cat sql/schema_U.sql >> /tmp/_ecperf_schema_sql.sql
elif [ "$DB" = "corpdb" ] 
then
echo "use $DB" > /tmp/_ecperf_schema_sql.sql
echo "go" >> /tmp/_ecperf_schema_sql.sql
cat sql/schema_C.sql >> /tmp/_ecperf_schema_sql.sql
cat sql/schema_U.sql >> /tmp/_ecperf_schema_sql.sql
elif [ "$DB" = "mfgdb" ] 
then
echo "use $DB" > /tmp/_ecperf_schema_sql.sql
echo "go" >> /tmp/_ecperf_schema_sql.sql
cat sql/schema_M.sql >> /tmp/_ecperf_schema_sql.sql
cat sql/schema_U.sql >> /tmp/_ecperf_schema_sql.sql
elif [ "$DB" = "orderdb" ] 
then
echo "use $DB" > /tmp/_ecperf_schema_sql.sql
echo "go" >> /tmp/_ecperf_schema_sql.sql
cat sql/schema_O.sql >> /tmp/_ecperf_schema_sql.sql
cat sql/schema_U.sql >> /tmp/_ecperf_schema_sql.sql
elif [ "$DB" = "supplierdb" ] 
then
echo "use $DB" > /tmp/_ecperf_schema_sql.sql
echo "go" >> /tmp/_ecperf_schema_sql.sql
cat sql/schema_S.sql >> /tmp/_ecperf_schema_sql.sql
cat sql/schema_U.sql >> /tmp/_ecperf_schema_sql.sql
else
	echo "DB=$DB"
	echo "db must be corpdb | orderdb | mfgdb | supplierdb | ecperfdb"
	exit 1
fi

##############################################
# Running scripts to create schema and indexes
##############################################
isql -Uecperf -Pecperf -i /tmp/_ecperf_schema_sql.sql
