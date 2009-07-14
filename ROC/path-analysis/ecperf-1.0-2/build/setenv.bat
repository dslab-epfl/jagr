@echo on
rem ---------------------
rem reset CLASSPATH
rem ---------------------
set CLASSPATH=.

rem ---------------------
rem JAVA
rem ---------------------
set JAVA_HOME=C:\app\java\jdk1.3.1
set PATH=%JAVA_HOME%\bin;%PATH%

rem ---------------------
rem ant
rem ---------------------
set ANT_HOME=d:\app\JBoss\ecperf\ant1.4.1
set PATH=%ANT_HOME%\bin;%PATH%

rem ---------------------
rem JBOSS
rem ---------------------
set JBOSS_HOME=D:\app\JBoss\JBoss-2.4.3_Tomcat-3.2.3\jboss
set PATH=%PATH%;%ANT_HOME%\bin
set CLASSPATH=%CLASSPATH%;%JBOSS_HOME%\lib\ext\jboss.jar
set CLASSPATH=%CLASSPATH%;%JBOSS_HOME%\lib\ext\jboss-j2ee.jar
set CLASSPATH=%CLASSPATH%;%JBOSS_HOME%\lib\ext\xerces.jar
set CLASSPATH=%CLASSPATH%;%JBOSS_HOME%\lib\jboss-jdbc_ext.jar

set J2EE_HOME=%JBOSS_HOME%

rem ---------------------
rem TOMCAT
rem ---------------------
set TOMCAT_HOME=D:\app\JBoss\JBoss-2.4.3_Tomcat-3.2.3\tomcat
set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\servlet.jar
set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\webserver.jar

rem ---------------------
rem JDBC
rem ---------------------
set CLASSPATH=%CLASSPATH%;D:\app\JBoss\JBoss-2.4.3_Tomcat-3.2.3\jboss\lib\ext\classes12.zip

rem ---------------------
rem ECperf
rem ---------------------
set ECPERF_HOME=D:\app\JBoss\ecperf
