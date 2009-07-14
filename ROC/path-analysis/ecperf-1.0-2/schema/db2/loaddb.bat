@echo off
setlocal
set ECPERF_HOME=c:/ecperf/ecperf
set SCALE=1
set CONFIG_DIR=%ECPERF_HOME%/config
set LOAD_PKG=com.sun.ecperf.load
set APPSSERVE=myserver
set ENVFILE=%CONFIG_DIR%/myserver.env
set JAVA_HOME=c:/jdk1.3
set CLASSPATH=c:/sqllib/java/db2java.zip;%ECPERF_HOME%/jars/load.jar;

set  
  
echo "Loading Corp Database..."
%JAVA_HOME%/bin/java -Decperf.home=%ECPERF_HOME% %LOAD_PKG%.LoadCorp %SCALE%
echo "Loading Orders Database..."
%JAVA_HOME%/bin/java -Decperf.home=%ECPERF_HOME% %LOAD_PKG%.LoadOrds %SCALE%
echo "Loading Manufacturing Database..."
%JAVA_HOME%/bin/java -Decperf.home=%ECPERF_HOME% %LOAD_PKG%.LoadMfg %SCALE%
echo "Loading Supplier Database..."
%JAVA_HOME%/bin/java -Decperf.home=%ECPERF_HOME% %LOAD_PKG%.LoadSupp %SCALE%
echo "Loading Discount Rules..."
%JAVA_HOME%/bin/java -Decperf.home=%ECPERF_HOME% %LOAD_PKG%.LoadRules discount %ECPERF_HOME%\schema\discount.rules
@endlocal

