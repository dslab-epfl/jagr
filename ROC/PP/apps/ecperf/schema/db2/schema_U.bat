@setlocal
@echo off

if .%2. == .. ( echo "Usage: schema_U.bat <database_name> <database dir>"
       goto exit )

set DB=%1
set DB_DIR=%2

rem ######  datafiles #####

set UTABS=%DB_DIR%\U_tables

rem ##############################################
rem #  clean up tablespaces and
rem #  datafiles from  earlier attempts
rem ##############################################
echo "Cleaning up old tables spaces"
db2 -v connect to %DB%  user ecperf using ecperf
db2 -v DROP TABLESPACE U_space

rem echo "Removing Datafiles"
del %UTABS% /s/q > NUL 2>&1


rem ##############################################
rem # Create the required tablespaces
rem ##############################################
echo "Creating U table space and table..."
rem Using SMS for now (until more precise numbers are known)

set STR=CREATE TABLESPACE U_space
set STR=%STR% managed by system using
set STR=%STR% ('%UTABS%')
set STR=%STR% extentsize 10K prefetchsize 10K
db2 -v "%STR%"
db2 -v connect reset

rem ##############################################
rem # Running scripts to create schema and indexes
rem ##############################################
db2 -v connect to %DB%  user ecperf using ecperf
db2 -tvf ../sql/schema_U.sql
db2 -v connect reset

:exit
endlocal
