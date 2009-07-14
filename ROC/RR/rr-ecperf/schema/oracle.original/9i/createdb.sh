#!/bin/sh
#
# Copyright (c) 1998 by Sun Microsystems, Inc.
#
# $Id: createdb.sh 
#
# Script to create oracle database
# March 2002 Tom Daly:  Updated to create Oracle 9i database
############################################################

##################
# check Parameters
##################
if [ $# -ne 2 ]
then
	echo "Usage: createdb.sh <database_name> <database dir>"
	exit 1
fi

if [ "$ORACLE_HOME" = "" ]
then
	echo "$ORACLE_HOME"
	echo "You must set the ORACLE_HOME environment variable"
	exit 1
fi


####################
# Set the database and database directory vars
#####################
DB=$1
DB_DIR=$2
ORACLE_SID=$DB
export ORACLE_SID

###################################################
# If the database already exists warn that we will 
# destroy it.
####################################################
if [ -f "$ORACLE_HOME"/dbs/init${DB}.ora  ]
then
	echo  "$ORACLE_HOME"/dbs/init${DB}.ora
	/usr/bin/echo "Database already exists. Ok to destroy y/n ? \c"
	read junk
	if [ "$junk" != 'y' -a "$junk" != 'Y' ]
	then
		echo  "database create cancelled"
		exit 1
	fi
fi
####################################
# Check to see that the target database directory
# exists and has write permission.
####################################
if [ ! -d "$DB_DIR" ] 
then
	echo "$DB_DIR does not exist. Please create it first."
	exit 1
fi


########################################################
# Go and run the create_init.sh script
# to build the correct pfiles and .ora files for Oracle 
########################################################
echo "Creating the \$ORACLE_HOME/dbs .ora files ..."
create_init.sh $DB $DB_DIR

####################################
# Environment vars an params look ok
# so go build the database
####################################

SYS=${DB_DIR}/sys_${DB}
LOG1=${DB_DIR}/log1_${DB}
LOG2=${DB_DIR}/log2_${DB}
TEMP=${DB_DIR}/temp_${DB}
STATS=${DB_DIR}/stats_${DB}
UNDO=${DB_DIR}/undotbs_${DB}

#don't remove when using links
rm -f $SYS $LOG1 $LOG2 $UNDO $TEMP

# Create database
sqlplus <<EOT
CONNECT / as sysdba
shutdown abort
startup pfile=${ORACLE_HOME}/dbs/p_create_$DB.ora nomount

create database $DB	
controlfile reuse
	datafile '${SYS}' size 150M reuse
        UNDO TABLESPACE undotbs DATAFILE '${UNDO}'  SIZE 100M REUSE 
        AUTOEXTEND ON NEXT 5120K MAXSIZE UNLIMITED
	logfile '${LOG1}' size 400M reuse, 
		'${LOG2}' size 400M reuse
	maxdatafiles 100;
exit
EOT

################
# BEGIN COMMON
################

sqlplus <<EOT
CONNECT / as sysdba
shutdown
startup pfile=${ORACLE_HOME}/dbs/p_build_$DB.ora
exit
EOT

echo "Running catalog scripts ..."
sqlplus <<EOT
CONNECT / as sysdba
@?/rdbms/admin/catalog;
@?/rdbms/admin/catexp.sql
@?/rdbms/admin/catldr.sql
@?/rdbms/admin/catproc.sql
DISCONNECT;

CONNECT / as sysdba;
@?/rdbms/admin/utlmontr;
DISCONNECT;
EXIT
EOT


sqlplus <<EOT
CONNECT / as sysdba
CREATE USER ecperf IDENTIFIED BY ecperf;
GRANT CONNECT, RESOURCE TO ecperf;
GRANT ALL PRIVILEGES TO ecperf IDENTIFIED BY ecperf;
EXIT
EOT


#############################
# Create the TEMP space
#############################
sqlplus <<EOT
CONNECT / as sysdba
CREATE TEMPORARY TABLESPACE temp_space
	TEMPFILE '${TEMP}' SIZE 9M;
EXIT
EOT

wait 

sqlplus <<EOT
CONNECT / as sysdba
ALTER USER ecperf
	TEMPORARY TABLESPACE temp_space;
EXIT
EOT

########################
# END OF DATABASE BUILD
########################
