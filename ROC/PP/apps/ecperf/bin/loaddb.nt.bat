:: This script loads all data into the database based on the given scale factor.
:: Pass the orders_injection_rate as an argument. If not given, defaults to 1

:: directory /tmp is necessary in root directory of drive

@echo off

set SCALE=1
if %1=="" GOTO :ENV

set SCALE=%1
GOTO :ENV

:ENV
::This shouldn't need to be modified
set LOAD_PKG=com.sun.ecperf.load

::The following should be modified to match your environment
set ECPERF_HOME=C:\ecperf
set CONFIG_DIR=%ECPERF_HOME%\config
set APPSSERVER=%CONFIG_DIR%\sssw
set ENVFILE=%APPSSERVER%.env

echo Appsserver is %APPSSERVER%
echo ENVFILE is %ENVFILE%
echo SCALE is %SCALE%
pause

set JAVA_HOME=C:\jdk1.2.2
set JDBC_CLASSPATH=C:\oracle\ora81\jdbc\lib\classes12.zip

set CLASSPATH=%JDBC_CLASSPATH%;%ECPERF_HOME%\jars\load.jar

echo Loading Corp Database...
%JAVA_HOME%\jre\bin\java -Decperf.home=%ECPERF_HOME% %LOAD_PKG%.LoadCorp %SCALE% 

echo Loading Orders Database...
%JAVA_HOME%\jre\bin\java -Decperf.home=%ECPERF_HOME% %LOAD_PKG%.LoadOrds %SCALE%

echo Loading Manufacturing Database...
%JAVA_HOME%\jre\bin\java -Decperf.home=%ECPERF_HOME% %LOAD_PKG%.LoadMfg %SCALE%

echo Loading Supplier Database...
%JAVA_HOME%\jre\bin\java -Decperf.home=%ECPERF_HOME% %LOAD_PKG%.LoadSupp %SCALE%

echo Loading Discount Rules...
%JAVA_HOME%\jre\bin\java -Decperf.home=%ECPERF_HOME% %LOAD_PKG%.LoadRules \
discount %ECPERF_HOME%\schema\discount.rules

