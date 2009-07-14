@setlocal
@echo off

if .%2. == .. ( echo "Usage: schema_C.bat <database_name> <database dir>"
       goto exit )

set DB=%1
set DB_DIR=%2


rem ######  datafiles #####
set COMP=%DB_DIR%\S_comp
set SC=%DB_DIR%\S_suppcomp
set SITE=%DB_DIR%\S_site
set SUPP=%DB_DIR%\S_supp
set PO=%DB_DIR%\S_po


rem ##############################################
rem #  clean up datafiles from  earlier attempts
rem ##############################################
echo "Cleaning up old tables spaces"
db2 -v connect to %DB%  user ecperf using ecperf
db2 -v DROP TABLESPACE S_comp_space
db2 -v DROP TABLESPACE S_sc_space
db2 -v DROP TABLESPACE S_po_space
db2 -v DROP TABLESPACE S_site_space
db2 -v DROP TABLESPACE S_supp_space

rem echo "Removing Datafiles"
del %COMP% /s/q > NUL 2>&1
del %SC% /s/q > NUL 2>&1
del %SITE% /s/q > NUL 2>&1
del %SUPP% /s/q > NUL 2>&1
del %PO% /s/q > NUL 2>&1
del %BIDS% /s/q > NUL 2>&1

rem ##############################################
rem # Create the required tablespaces
rem ##############################################
echo "Creating S table spaces and tables..."
rem Using SMS for now (until more precise numbers are known)

set STR=CREATE TABLESPACE S_comp_space
set STR=%STR% managed by system using
set STR=%STR% ('%COMP%')
set STR=%STR% extentsize 1M prefetchsize 100K
db2 -v "%STR%"

set STR=CREATE TABLESPACE S_sc_space
set STR=%STR% managed by system using
set STR=%STR% ('%SC%')
set STR=%STR% extentsize 1M prefetchsize 100K
db2 -v "%STR%"

set STR=CREATE TABLESPACE S_po_space
set STR=%STR% managed by system using
set STR=%STR% ('%PO%')
set STR=%STR% extentsize 1M prefetchsize 1M
db2 -v "%STR%"

set STR=CREATE TABLESPACE S_site_space
set STR=%STR% managed by system using
set STR=%STR% ('%SITE%')
set STR=%STR% extentsize 100K prefetchsize 100K
db2 -v "%STR%"

set STR=CREATE TABLESPACE S_supp_space
set STR=%STR% managed by system using
set STR=%STR% ('%SUPP%')
set STR=%STR% extentsize 100K prefetchsize 100K
db2 -v "%STR%"

db2 -v connect reset

rem ##############################################
rem # Running scripts to create schema and indexes
rem ##############################################
db2 -v connect to %DB%  user ecperf using ecperf
db2 -tvf ../sql/schema_S.sql
db2 -v connect reset

:exit
endlocal
