@setlocal
@echo off

rem
rem  Script to create DB2 database
rem  #############################

rem  ##################
rem  # check Parameters
rem  ##################

if .%2. == .. ( echo "Usage: createdb.bat <database_name> <database dir>"
       goto exit )

rem  ####################
rem  # Set the database and database directory vars
rem  #####################
set DB=%1
set DB_DIR=%2


rem  ####################################
rem  # Environment vars an params look ok
rem  # so go build the database
rem  ####################################

set SYS=%DB_DIR%\sys_%DB%
set LOG1=%DB_DIR%\log1_%DB%
set LOG2=%DB_DIR%\log2_%DB%
set ROLL=%DB_DIR%\roll_%DB%
set TEMP=%DB_DIR%\temp_%DB%

echo Deleting existing %SYS%...
del %SYS% /s/q > NUL 2>&1
echo Deleting existing %LOG1%...
del %LOG1% /s/q > NUL 2>&1
echo Deleting existing %LOG2%...
del %LOG2% /s/q > NUL 2>&1
echo Deleting existing %ROLL%...
del %ROLL% /s/q > NUL 2>&1
echo Deleting existing %TEMP%...
del %TEMP% /s/q > NUL 2>&1


rem  ################
rem  # Create database
rem  ################
rem Using SMS for now (until more precise numbers are known)

db2 -v drop database %DB%
set STR=create database %DB% on %~d2
set STR=%STR% TEMPORARY TABLESPACE
set STR=%STR% managed by system using
set STR=%STR% ('%TEMP%' )
db2 -v "%STR%"



rem  #############################
rem  # Create the ROLL space
rem  #############################
rem Using SMS for now (until firm numbers are available)

db2 -v connect to %DB%
set STR=CREATE TABLESPACE roll_space
set STR=%STR% managed by system using
set STR=%STR% ('%ROLL%')
set STR=%STR% extentsize 200 prefetchsize 100K
db2 -v "%STR%"
db2 -v connect reset

rem  ########################
rem  # END OF DATABASE BUILD
rem  ########################

:exit
endlocal
