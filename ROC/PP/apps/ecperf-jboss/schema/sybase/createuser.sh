#!/bin/sh
#
# Copyright (c) 2000 by Sybase, Inc.
#
# $Id: createuser.sh 
#
# Script to create users
#####################################################

##################
# check Parameters
##################
if [ $# -ne 0 ]
then
	echo "Usage: createuser.sh"
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

# Create user
isql -Usa -Psybase <<EOT
sp_addlogin ecperf, ecperf
go
sp_adduser ecperf
go
grant role 'sa_role'  to ecperf
go
EOT

########################
# END OF CREATE USER
########################
