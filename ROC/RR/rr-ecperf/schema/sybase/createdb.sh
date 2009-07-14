#!/bin/sh
#
# Copyright (c) 2000 by Sybase, Inc.
#
# $Id: createdb.sh 
#
# Script to create database
#####################################################

##################
# check Parameters
##################
if [ $# -ne 2 ]
then
	echo "Usage: createdb.sh <db> <scale>"
	echo "db can be corpdb | orderdb | mfgdb | supplierdb | ecperfdb"
	echo "scale is the scale factor i.e. Ir"
	exit 1
fi

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

####################
# Set the required variables
#####################
DB=$1
SCALE=$2
if [ "$DB" = "ecperfdb" ] 
then
	DATASIZE=`expr $SCALE \* 140`
	LOGSIZE=`expr $SCALE \* 15 + 15`
elif [ "$DB" = "corpdb" ] 
then
	DATASIZE=`expr $SCALE \* 20`
	LOGSIZE=`expr $SCALE \* 2 + 2`
elif [ "$DB" = "mfgdb" ] 
then
	DATASIZE=`expr $SCALE \* 22`
	LOGSIZE=`expr $SCALE \* 3 + 2`
elif [ "$DB" = "orderdb" ] 
then
	DATASIZE=`expr $SCALE \* 50`
	LOGSIZE=`expr $SCALE \* 5 + 5`
elif [ "$DB" = "supplierdb" ] 
then
	DATASIZE=`expr $SCALE \* 38`
	LOGSIZE=`expr $SCALE \* 4 + 4`
else
	echo "DB=$DB"
	echo "db must be corpdb | orderdb | mfgdb | supplierdb | ecperfdb"
	exit 1
fi
###################################################
# If the database already exists this will fail
####################################################

# Create database
isql -Uecperf -Pecperf <<EOT
create database $DB	on ${DB}_data = $DATASIZE log on ${DB}_log = $LOGSIZE
go
sp_dboption $DB, "allow nulls by default", true
go
use $DB
go
checkpoint
go
EOT

########################
# END OF DATABASE BUILD
########################
