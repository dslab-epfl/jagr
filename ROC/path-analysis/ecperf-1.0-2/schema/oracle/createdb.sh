#!/bin/sh
#
# Copyright (c) 1998 by Sun Microsystems, Inc.
#
# $Id: createdb.sh 
#
# Script to create oracle database
#####################################################

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
create_init.sh $DB

####################################
# Environment vars an params look ok
# so go build the database
####################################

SYS=${DB_DIR}/sys_${DB}
LOG1=${DB_DIR}/log1_${DB}
LOG2=${DB_DIR}/log2_${DB}
ROLL=${DB_DIR}/roll_${DB}
TEMP=${DB_DIR}/temp_${DB}

rm -f $SYS $LOG1 $LOG2 $ROLL $TEMP

# Create database
svrmgrl <<EOT
CONNECT internal
shutdown abort
startup pfile=${ORACLE_HOME}/dbs/p_create_$DB.ora nomount

create database $DB	
controlfile reuse
	datafile '${SYS}' size 150M reuse
	logfile '${LOG1}' size 400M reuse, 
		'${LOG2}' size 400M reuse
	maxdatafiles 100;
exit
EOT

################
# BEGIN COMMON
################

svrmgrl <<EOT
CONNECT system/manager
create rollback segment r01
	storage (initial 100K minextents 2 next 100K);
create rollback segment r02
	storage (initial 100K minextents 2 next 100K);
create rollback segment r03
	storage (initial 100K minextents 2 next 100K);
create rollback segment r04
	storage (initial 100K minextents 2 next 100K);
create rollback segment r05
	storage (initial 100K minextents 2 next 100K);
create rollback segment r06
	storage (initial 100K minextents 2 next 100K);
create rollback segment r07
	storage (initial 100K minextents 2 next 100K);
create rollback segment r08
	storage (initial 100K minextents 2 next 100K);
create rollback segment r09
	storage (initial 100K minextents 2 next 100K);
create rollback segment r10
	storage (initial 100K minextents 2 next 100K);
create rollback segment r11
	storage (initial 100K minextents 2 next 100K);
create rollback segment r12
	storage (initial 100K minextents 2 next 100K);
create rollback segment r13
	storage (initial 100K minextents 2 next 100K);
create rollback segment r14
	storage (initial 100K minextents 2 next 100K);
create rollback segment r15
	storage (initial 100K minextents 2 next 100K);
create rollback segment r16
	storage (initial 100K minextents 2 next 100K);
create rollback segment r17
	storage (initial 100K minextents 2 next 100K);
create rollback segment r18
	storage (initial 100K minextents 2 next 100K);
create rollback segment r19
	storage (initial 100K minextents 2 next 100K);
create rollback segment r20
	storage (initial 100K minextents 2 next 100K);

DISCONNECT
CONNECT internal

shutdown
startup pfile=${ORACLE_HOME}/dbs/p_build_$DB.ora
exit
EOT

echo "Running catalog scripts ..."
svrmgrl lmode=y >/dev/null 2>&1 <<EOT
CONNECT sys/change_on_install
@?/rdbms/admin/catalog.sql;
@?/rdbms/admin/catexp.sql
@?/rdbms/admin/catldr.sql
@?/rdbms/admin/catproc.sql
DISCONNECT;
EXIT
EOT


svrmgrl lmode=y <<EOT
CONNECT internal
CREATE USER ecperf IDENTIFIED BY ecperf;
GRANT CONNECT, RESOURCE TO ecperf;
EXIT
EOT


#############################
# Create the TEMP space
#############################
svrmgrl lmode=y <<EOT  &
CONNECT system/manager
CREATE TABLESPACE temp_space
	DATAFILE '${TEMP}' SIZE 9M;
EXIT
EOT

wait 

svrmgrl lmode=y <<EOT
CONNECT internal
ALTER USER ecperf
	TEMPORARY TABLESPACE temp_space
	DEFAULT TABLESPACE temp_space;
EXIT
EOT

#############################
# Create more rollbacks 
#############################

svrmgrl lmode=y <<EOT  &
CONNECT system/manager
CREATE TABLESPACE roll_space
	DATAFILE '${ROLL}' SIZE 8M
	DEFAULT STORAGE ( INITIAL 4M NEXT 100K);
EXIT
EOT

wait
svrmgrl lmode=y <<EOT
CONNECT system/manager

CREATE ROLLBACK SEGMENT t1
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);

CREATE ROLLBACK SEGMENT t2
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);

CREATE ROLLBACK SEGMENT t3
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);
 
CREATE ROLLBACK SEGMENT t4
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);
 
CREATE ROLLBACK SEGMENT t5
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);
 
CREATE ROLLBACK SEGMENT t6
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);

CREATE ROLLBACK SEGMENT t7
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);

CREATE ROLLBACK SEGMENT t8
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);

CREATE ROLLBACK SEGMENT t9
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);

CREATE ROLLBACK SEGMENT t10
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);

CREATE ROLLBACK SEGMENT t11
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);

CREATE ROLLBACK SEGMENT t12
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);

CREATE ROLLBACK SEGMENT t13
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);
 
CREATE ROLLBACK SEGMENT t14
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);
 
CREATE ROLLBACK SEGMENT t15
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);
 
CREATE ROLLBACK SEGMENT t16
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);

CREATE ROLLBACK SEGMENT t17
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);

CREATE ROLLBACK SEGMENT t18
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);

CREATE ROLLBACK SEGMENT t19
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);

CREATE ROLLBACK SEGMENT t20
  TABLESPACE roll_space
  STORAGE (INITIAL 100K
       MINEXTENTS 2
       NEXT 100K);
EXIT
EOT

########################
# END OF DATABASE BUILD
########################
