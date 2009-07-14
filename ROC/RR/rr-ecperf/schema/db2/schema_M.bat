@setlocal
@echo off

if .%2. == .. ( echo "Usage: schema_M.bat <database_name> <database dir>"
       goto exit )

set DB=%1
set DB_DIR=%2

rem ######  datafiles #####
set PART=%DB_DIR%\M_parts
set BOM=%DB_DIR%\M_bom
set WO=%DB_DIR%\M_wrkorder
set LO=%DB_DIR%\M_lrgorder
set INV=%DB_DIR%\M_inv

rem ##############################################
rem #  clean up datafiles from  earlier attempts
rem ##############################################
echo "Cleaning up old tables spaces"
db2 -v connect to %DB%  user ecperf using ecperf
db2 -v DROP TABLESPACE M_parts_space
db2 -v DROP TABLESPACE M_bom_space
db2 -v DROP TABLESPACE M_wo_space
db2 -v DROP TABLESPACE M_lo_space
db2 -v DROP TABLESPACE M_inv_space

rem echo "Removing Datafiles"
del %BOM% /s/q > NUL 2>&1
del %WO% /s/q > NUL 2>&1
del %LO% /s/q > NUL 2>&1
del %INV% /s/q > NUL 2>&1
del %PART% /s/q > NUL 2>&1

rem ##############################################
rem # Create the required tablespaces
rem ##############################################
echo "Creating M table spaces and tables..."
rem Using SMS for now (until more precise numbers are known)

set STR=CREATE TABLESPACE M_parts_space
set STR=%STR% managed by system using
set STR=%STR% ('%PART%')
set STR=%STR% extentsize 1M prefetchsize 100K
db2 -v "%STR%"

set STR=CREATE TABLESPACE M_bom_space
set STR=%STR% managed by system using
set STR=%STR% ('%BOM%')
set STR=%STR% extentsize 1M prefetchsize 100K
db2 -v "%STR%"

set STR=CREATE TABLESPACE M_wo_space
set STR=%STR% managed by system using
set STR=%STR% ('%WO%')
set STR=%STR% extentsize 1M prefetchsize 1M
db2 -v "%STR%"

set STR=CREATE TABLESPACE M_lo_space
set STR=%STR% managed by system using
set STR=%STR% ('%LO%')
set STR=%STR% extentsize 1M prefetchsize 100K
db2 -v "%STR%"

set STR=CREATE TABLESPACE M_inv_space
set STR=%STR% managed by system using
set STR=%STR% ('%INV%')
set STR=%STR% extentsize 1M prefetchsize 100K
db2 -v "%STR%"
db2 -v connect reset


rem ##############################################
rem # Running scripts to create schema and indexes
rem ##############################################
db2 -v connect to %DB%  user ecperf using ecperf
db2 -tvf ../sql/schema_M.sql
db2 -v connect reset

:exit
endlocal
