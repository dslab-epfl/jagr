#!/bin/sh
#
# Script to truncate tables before re-loading. It is advisable to
# do this periodically to avoid errors in the Driver.
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
	echo "You must set the DSQUERY environment variable to point to the ecperf db"
	exit 1
fi

if [ $# -ne 1 ]
then
	echo "Usage: trunc.sh <database_name>"
	echo "db must be corpdb | orderdb | mfgdb | supplierdb | ecperfdb"
	exit 1
fi

DB=$1


if [ "$DB" = "corpdb" ] 
then

isql -Uecperf -Pecperf << EOT
use corpdb
go

	truncate table C_customer
	truncate table C_parts
	truncate table C_supplier
	truncate table C_site
	truncate table C_rule
	truncate table U_sequences

go
EOT

elif [ "$DB" = "orderdb" ] 
then

isql -Uecperf -Pecperf << EOT
use orderdb
go
	truncate table O_customer
	truncate table O_item
	truncate table O_orders
	truncate table O_orderline
	truncate table U_sequences
go
EOT

elif [ "$DB" = "mfgdb" ] 
then

isql -Uecperf -Pecperf << EOT
use mfgdb
go
	truncate table M_parts
	truncate table M_bom
	truncate table M_workorder
	truncate table M_inventory
	truncate table U_sequences
go
EOT
elif [ "$DB" = "supplierdb" ] 
then

isql -Uecperf -Pecperf << EOT
use supplierdb
go
	truncate table S_site
	truncate table S_supplier
	truncate table S_component
	truncate table S_supp_component
	truncate table S_purchase_order
	truncate table S_purchase_orderline
	truncate table U_sequences
go
EOT

elif [ "$DB" = "ecperfdb" ] 
then

isql -Uecperf -Pecperf << EOT
use ecperfdb
go
	truncate table C_customer
	truncate table C_parts
	truncate table C_supplier
	truncate table C_site
	truncate table C_rule
	truncate table O_customer
	truncate table O_item
	truncate table O_orders
	truncate table O_orderline
	truncate table M_parts
	truncate table M_bom
	truncate table M_workorder
	truncate table M_inventory
	truncate table S_site
	truncate table S_supplier
	truncate table S_component
	truncate table S_supp_component
	truncate table S_purchase_order
	truncate table S_purchase_orderline
	truncate table U_sequences
go
EOT

else
	echo "db must be corpdb | orderdb | mfgdb | supplierdb | ecperfdb"
fi
