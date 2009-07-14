#!/bin/sh

#
# $Id: run.sh,v 1.1 2003/03/01 07:02:11 candea Exp $
#

#
# Runs the standalone JNDI server
#

PROGNAME=`basename $0`

# Setup the JVM
if [ "x$JAVA_HOME" != "x" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA="java"
fi

# Setup the classpath

if [ "x$JBOSS_TOP" == "x" ]; then
    echo "Please set JBOSS_TOP to the top dir of RR-JBoss src tree"
    exit 1
fi

JNDI_CLASSPATH="$JBOSS_TOP/naming/output/classes"
JNDI_CLASSPATH="$JNDI_CLASSPATH:$JBOSS_TOP/naming/output/classes/org/jnp/server"
JNDI_CLASSPATH="$JNDI_CLASSPATH:$JBOSS_TOP/naming/output/etc/conf"
JNDI_CLASSPATH="$JNDI_CLASSPATH:$JBOSS_TOP/system/output/classes"
JNDI_CLASSPATH="$JNDI_CLASSPATH:$JBOSS_TOP/common/output/classes"
JNDI_CLASSPATH="$JNDI_CLASSPATH:$JBOSS_TOP/messaging/output/classes"

# Display our environment
echo "================================================================================"
echo "JNDI Standalone Server"
echo ""
echo "JNDI_CLASSPATH: $JNDI_CLASSPATH"
echo "================================================================================"
echo ""

exec $JAVA -classpath "$JNDI_CLASSPATH" org/jnp/server/Main
