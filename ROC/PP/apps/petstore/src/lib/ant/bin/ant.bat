@echo off

REM
REM Copyright 2002 Sun Microsystems, Inc. All rights reserved.
REM 
REM Redistribution and use in source and binary forms, with or without
REM modification, are permitted provided that the following conditions
REM are met:
REM 
REM - Redistributions of source code must retain the above copyright
REM   notice, this list of conditions and the following disclaimer.
REM 
REM - Redistribution in binary form must reproduce the above copyright
REM   notice, this list of conditions and the following disclaimer in
REM   the documentation and/or other materials provided with the
REM   distribution.
REM 
REM Neither the name of Sun Microsystems, Inc. or the names of
REM contributors may be used to endorse or promote products derived
REM from this software without specific prior written permission.
REM 
REM This software is provided "AS IS," without a warranty of any
REM kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
REM WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
REM FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
REM EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
REM SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
REM DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
REM OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
REM FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
REM PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
REM LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
REM EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
REM 
REM You acknowledge that Software is not designed, licensed or intended
REM for use in the design, construction, operation or maintenance of
REM any nuclear facility.
REM


if exist "%HOME%\antrc_pre.bat" call "%HOME%\antrc_pre.bat"

if not "%OS%"=="Windows_NT" goto win9xStart
:winNTStart
@setlocal

rem %~dp0 is name of current script under NT
set DEFAULT_ANT_HOME=%~dp0

rem : operator works similar to make : operator
set DEFAULT_ANT_HOME=%DEFAULT_ANT_HOME:\bin\=%

if %ANT_HOME%a==a set ANT_HOME=%DEFAULT_ANT_HOME%
set DEFAULT_ANT_HOME=

rem On NT/2K grab all arguments at once
set ANT_CMD_LINE_ARGS=%*
goto doneStart

:win9xStart
rem Slurp the command line arguments.  This loop allows for an unlimited number of 
rem agruments (up to the command line limit, anyway).

set ANT_CMD_LINE_ARGS=

:setupArgs
if %1a==a goto doneStart
set ANT_CMD_LINE_ARGS=%ANT_CMD_LINE_ARGS% %1
shift
goto setupArgs

:doneStart
rem This label provides a place for the argument list loop to break out 
rem and for NT handling to skip to.

rem find ANT_HOME
if not "%ANT_HOME%"=="" goto checkJava

rem check for ant in Program Files on system drive
if not exist "%SystemDrive%\Program Files\ant" goto checkSystemDrive
set ANT_HOME=%SystemDrive%\Program Files\ant
goto checkJava

:checkSystemDrive
rem check for ant in root directory of system drive
if not exist "%SystemDrive%\ant" goto noAntHome
set ANT_HOME=%SystemDrive%\ant
goto checkJava

:noAntHome
echo ANT_HOME is not set and ant could not be located. Please set ANT_HOME.
goto end

:checkJava
set _JAVACMD=%JAVACMD%
set LOCALCLASSPATH=%CLASSPATH%
for %%i in ("%ANT_HOME%\lib\*.jar") do call "%ANT_HOME%\bin\lcp.bat" "%%i"

if "%JAVA_HOME%" == "" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java
if exist "%JAVA_HOME%\lib\tools.jar" call "%ANT_HOME%\bin\lcp.bat" "%JAVA_HOME%\lib\tools.jar"
if exist "%JAVA_HOME%\lib\classes.zip" call "%ANT_HOME%\bin\lcp.bat" "%JAVA_HOME%\lib\classes.zip"
goto checkJikes

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo   If build fails because sun.* classes could not be found
echo   you will need to set the JAVA_HOME environment variable
echo   to the installation directory of java.
echo.

:checkJikes
if not "%JIKESPATH%" == "" goto runAntWithJikes

:runAnt
%_JAVACMD% -classpath %LOCALCLASSPATH% -Dant.home="%ANT_HOME%" %ANT_OPTS% org.apache.tools.ant.Main %ANT_CMD_LINE_ARGS%
goto end

:runAntWithJikes
%_JAVACMD% -classpath %LOCALCLASSPATH% -Dant.home="%ANT_HOME%" -Djikes.class.path=%JIKESPATH% %ANT_OPTS% org.apache.tools.ant.Main %ANT_CMD_LINE_ARGS%

:end
set LOCALCLASSPATH=
set _JAVACMD=
set ANT_CMD_LINE_ARGS=

if not "%OS%"=="Windows_NT" goto mainEnd
:winNTend
@endlocal

:mainEnd
if exist "%HOME%\antrc_post.bat" call "%HOME%\antrc_post.bat"

