@setlocal
@echo off

if .%2. == .. ( echo "Usage: blddb.bat <database_name> <database dir>"
       goto exit )

set DB=%1
set DB_DIR=%2

db2 -v db2start

rem Create the DB
call createdb.bat %DB% %DB_DIR%

rem Create the tablespaces and tables
call schema_C.bat %DB% %DB_DIR%
call schema_M.bat %DB% %DB_DIR%
call schema_O.bat %DB% %DB_DIR%
call schema_S.bat %DB% %DB_DIR%

rem Create the (U??) tablespace and table
call schema_U.bat %DB% %DB_DIR%

:exit
endlocal

