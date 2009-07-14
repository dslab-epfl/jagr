@echo off
@if "%OS%"=="Windows_NT"  setlocal

REM ----------------------------------------------------------------
REM 
REM    Batch file for running invocation log tool.
REM
REM    Author:    Juha Lindfors
REM    Revision:  $Id: SystemLog.bat,v 1.1.1.1 2002/10/03 21:06:52 candea Exp $
REM
REM ----------------------------------------------------------------


REM ----------------------------------------------------------------
REM
REM   Set the classpath
REM
REM ----------------------------------------------------------------
:SetClasspath
set CP=.;..\classes

set CP=%CP%;..\lib\jboss-j2ee.jar
set CP=%CP%;..\lib\jta-spec1_0_1.jar
set CP=%CP%;..\lib\jnp-client.jar
set CP=%CP%;..\lib\jbossmq-client.jar
set CP=%CP%;..\lib\jboss-util.jar
set CP=%CP%;..\lib\jboss-client.jar
set CP=%CP%;..\lib\jpl-util-0_5_1.jar
set CP=%CP%;..\lib\jpl-pattern-0_3.jar

set CP=%CP%;%CLASSPATH%

goto Run

REM ----------------------------------------------------------------
REM 
REM   Execute
REM
REM ----------------------------------------------------------------
:Run
java -cp "%CP%" org.jboss.admin.systemlog.Main %1 %2 %3 %4 %5 %6 %7
