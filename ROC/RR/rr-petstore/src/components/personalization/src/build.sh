#! /bin/sh

# $Id: build.sh,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $

if [ -z "$JAVA_HOME" ]
then
JAVACMD=`which java`
if [ -z "$JAVACMD" ]
then
echo "Cannot find JAVA. Please set your PATH."
exit 1
fi
JAVA_BINDIR=`dirname $JAVACMD`
JAVA_HOME=$JAVA_BINDIR/..
fi

if [ -z "$J2EE_HOME" ]
then
echo "Please set J2EE_HOME."
exit 1
fi

JAVACMD=$JAVA_HOME/bin/java

ANT_HOME=../../../lib/ant
ANT_CLASSPATH=$JAVA_HOME/lib/tools.jar
ANT_CLASSPATH=$ANT_HOME/lib/ant.jar:$ANT_HOME/lib/xml.jar:$ANT_CLASSPATH
ANT_CLASSPATH=$J2EE_HOME/lib/j2ee.jar:$ANT_CLASSPATH
$JAVACMD -classpath $ANT_CLASSPATH -Dant.home=$ANT_HOME -Dj2ee.home=$J2EE_HOME org.apache.tools.ant.Main "$@"
