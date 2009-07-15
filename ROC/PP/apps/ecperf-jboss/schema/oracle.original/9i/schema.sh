#!/bin/ksh

#sh createdb.sh ecperf1 /export/home/oracle/oradata

sh schema_C.sh test1 /disk1/oracle/test1
sh schema_M.sh test1 /disk1/oracle/test1
sh schema_O.sh test1 /disk1/oracle/test1
sh schema_S.sh test1 /disk1/oracle/test1
sh schema_U.sh test1 /disk1/oracle/test1
