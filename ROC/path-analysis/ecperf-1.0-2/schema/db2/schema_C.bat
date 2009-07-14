@setlocal
@echo off

if .%2. == .. ( echo "Usage: schema_C.bat <database_name> <database dir>"
       goto exit )

set DB=%1
set DB_DIR=%2

rem ######  datafiles #####

set CUST=%DB_DIR%\C_cust
set SUPP=%DB_DIR%\C_supp
set SITE=%DB_DIR%\C_site
set PARTS=%DB_DIR%\C_parts


rem ##############################################
rem #  clean up tablespaces and
rem #  datafiles from  earlier attempts
rem ##############################################
echo "Cleaning up old tables spaces"
db2 -v connect to %DB% user ecperf using ecperf
db2 -v DROP TABLESPACE C_cust_space
db2 -v DROP TABLESPACE C_supp_space
db2 -v DROP TABLESPACE C_site_space
db2 -v DROP TABLESPACE C_parts_space
db2 -v connect reset

rem echo "Removing Datafiles"
del %CUST% /s/q > NUL 2>&1
del %SUPP% > /s/q NUL 2>&1
del %SITE% > /s/q NUL 2>&1
del %PARTS% > /s/q NUL 2>&1


rem ##############################################
rem # Create the required tablespaces
rem ##############################################
echo "Creating C table spaces and tables..."
rem Using SMS for now (until more precise numbers are known)

db2 -v connect to %DB%  user ecperf using ecperf
set STR=CREATE TABLESPACE C_cust_space
set STR=%STR% managed by system using
set STR=%STR% ('%CUST%')
set STR=%STR% extentsize 500K prefetchsize 1M
db2 -v "%STR%"

set STR=CREATE TABLESPACE C_supp_space
set STR=%STR% managed by system using
set STR=%STR% ('%SUPP%')
set STR=%STR% extentsize 100K prefetchsize 100K
db2 -v "%STR%"

set STR=CREATE TABLESPACE C_site_space
set STR=%STR% managed by system using
set STR=%STR% ('%SITE%')
set STR=%STR% extentsize 100K prefetchsize 100K
db2 -v "%STR%"

set STR=CREATE TABLESPACE C_parts_space
set STR=%STR% managed by system using
set STR=%STR% ('%PARTS%')
set STR=%STR% extentsize 500K prefetchsize 100K
db2 -v "%STR%"
db2 -v connect reset


rem ##############################################
rem # Running scripts to create schema and indexes
rem ##############################################
db2 -v connect to %DB%  user ecperf using ecperf
db2 -tvf ../sql/schema_C.sql
db2 -v connect reset

:exit
endlocal
