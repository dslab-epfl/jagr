#!/bin/sh
#
# Copyright (c) 2000 by Sybase, Inc.
#
# $Id: createdevice.sh 
#
# Script to create devices(disk init)
#####################################################

##################
# check Parameters
##################
if [ $# -ne 3 ]
then
	echo "Usage: createdevice.sh <db> <scale> <devdir>"
	echo "db can be corpdb | orderdb | mfgdb | supplierdb | ecperfdb"
	echo "scale is the scale factor i.e. Ir"
	echo "devdir is the directory under which the device files will be created"
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
DEVDIR=$3
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

DATASIZE=`expr $DATASIZE \* 512`
LOGSIZE=`expr $LOGSIZE \* 512`

###################################################
# If the devices already exists this will fail
####################################################

# Create database
isql -Uecperf -Pecperf <<EOT
declare @ddev int, @ldev int,@maxdev int
-- select to get max used vdevno
select @ddev = max(convert(tinyint, substring(convert(binary(4), 
							d.low), v.low, 1))) + 1
from master.dbo.sysdevices d, master.dbo.spt_values v
where  v.type = 'E' and v.number = 3
select @maxdev = value, @ldev = @ddev+1 from sysconfigures 
where name like 'number of devices'
if (@ldev >= @maxdev) 
begin
	print "Not enough devices configured."
end
else 
begin
	disk init name = ${DB}_data , physname = '${DEVDIR}/${DB}data.dev',
	vdevno = @ddev , size = $DATASIZE 
	disk init name = ${DB}_log , physname = '${DEVDIR}/${DB}log.dev' ,
	vdevno = @ldev , size = $LOGSIZE
end
go
EOT

########################
# END OF DEVICE BUILD
########################
