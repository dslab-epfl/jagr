#!/bin/sh
# This script truncates the newly added rows to the databases
# It brings the db back to the initially loaded state
# WARNING: The below numbers work only for a load with scale=1

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
	echo "db must be orderdb | mfgdb | supplierdb | ecperfdb"
	exit 1
fi

DB=$1


if [ "$DB" = "orderdb" ] 
then

isql -Uecperf -Pecperf << EOT
use orderdb
go
delete from O_orders where o_id > 750
delete from O_customer where c_id > 750
delete from O_orderline where ol_o_id > 750
update U_sequences set s_nextnum = 751 where s_id = 'customer'
update U_sequences set s_nextnum = 751 where s_id = 'order'
go
EOT

elif [ "$DB" = "mfgdb" ] 
then

isql -Uecperf -Pecperf << EOT
use mfgdb
go
	truncate table M_largeorder
	delete from M_workorder where wo_number > 100
	update U_sequences set s_nextnum = 101 where s_id = 'workorder'
	update U_sequences set s_nextnum = 1 where s_id = 'largeorder'
go
EOT
elif [ "$DB" = "supplierdb" ] 
then

isql -Uecperf -Pecperf << EOT
use supplierdb
go
	delete from S_purchase_order where po_number > 25
	delete from S_purchase_orderline where pol_po_id > 25
	update U_sequences set s_nextnum = 26 where s_id = 'purchaseorder'
go
EOT

elif [ "$DB" = "ecperfdb" ] 
then

isql -Uecperf -Pecperf << EOT
use ecperfdb
go
	delete from O_orders where o_id > 750
	delete from O_customer where c_id > 750
	delete from O_orderline where ol_o_id > 750
	update U_sequences set s_nextnum = 751 where s_id = 'customer'
	update U_sequences set s_nextnum = 751 where s_id = 'order'
	truncate table M_largeorder
	delete from M_workorder where wo_number > 100
	update U_sequences set s_nextnum = 101 where s_id = 'workorder'
	update U_sequences set s_nextnum = 1 where s_id = 'largeorder'
	delete from S_purchase_order where po_number > 25
	delete from S_purchase_orderline where pol_po_id > 25
	update U_sequences set s_nextnum = 26 where s_id = 'purchaseorder'
go
EOT

else
	echo "db must be orderdb | mfgdb | supplierdb | ecperfdb"
fi
