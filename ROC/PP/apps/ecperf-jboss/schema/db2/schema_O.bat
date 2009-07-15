@setlocal
@echo off

if .%2. == .. ( echo "Usage: schema_O.bat <database_name> <database dir>"
       goto exit )

set DB=%1
set DB_DIR=%2

rem ######  datafiles #####
set CUST=%DB_DIR%\O_cust
set ORDS=%DB_DIR%\O_ords
set ITEM=%DB_DIR%\O_item
set ORDL=%DB_DIR%\O_ordl

rem ##############################################
rem #  clean up datafiles from  earlier attempts
rem ##############################################
echo "Cleaning up old tables spaces"
db2 -v connect to %DB%  user ecperf using ecperf
db2 -v DROP TABLESPACE O_cust_space
db2 -v DROP TABLESPACE O_ords_space
db2 -v DROP TABLESPACE O_ordl_space
db2 -v DROP TABLESPACE O_item_space

rem echo "Removing Datafiles"
del %CUST% /s/q > NUL 2>&1
del %ORDS% /s/q > NUL 2>&1
del %ITEM% /s/q > NUL 2>&1
del %ORDL% /s/q > NUL 2>&1

rem ##############################################
rem # Create the required tablespaces
rem ##############################################
echo "Creating O table spaces and tables..."
rem Using SMS for now (until more precise numbers are known)

db2 -v connect to %DB%  user ecperf using ecperf
set STR=CREATE TABLESPACE O_cust_space
set STR=%STR% managed by system using
set STR=%STR% ('%CUST%')
set STR=%STR% extentsize 1M prefetchsize 1M
db2 -v "%STR%"

set STR=CREATE TABLESPACE O_ords_space
set STR=%STR% managed by system using
set STR=%STR% ('%ORDS%')
set STR=%STR% extentsize 1M prefetchsize 1M
db2 -v "%STR%"

set STR=CREATE TABLESPACE O_ordl_space
set STR=%STR% managed by system using
set STR=%STR% ('%ORDL%')
set STR=%STR% extentsize 1M prefetchsize 1M
db2 -v "%STR%"

set STR=CREATE TABLESPACE O_item_space
set STR=%STR% managed by system using
set STR=%STR% ('%ITEM%')
set STR=%STR% extentsize 1M prefetchsize 100K
db2 -v "%STR%
db2 -v connect reset


rem ##############################################
rem # Running scripts to create schema and indexes
rem ##############################################
db2 -v connect to %DB%  user ecperf using ecperf
db2 -tvf ../sql/schema_O.sql
db2 -v connect reset

:exit
endlocal
