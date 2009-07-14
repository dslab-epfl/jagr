#!/bin/sh

# $Id: startrubis.sh,v 1.2 2003/04/12 04:37:14 steveyz Exp $
#
# Run this script to startup all auxiliary RR-JBoss services
# (should be run before starting JBoss)

if [ -z $JBOSS_HOME ]; then
  echo Please define JBOSS_HOME
  exit 1
fi

if [ -z $JBOSS_TOP ]; then
  echo Please define JBOSS_TOP
  exit 1
fi

if [ -z $J2EE_HOME ]; then
  echo Please define J2EE_HOME
  exit 1
fi

# Setup the JVM
if [ "x$JAVA_HOME" != "x" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA="java"
fi

export CLASSPATH="${CLASSPATH}:${JBOSS_TOP}/server/output/classes:${JBOSS_HOME}/lib/GFCall1.1.2.zip"

RUNDIR="/tmp/RR-JBoss"
rm -rf $RUNDIR
mkdir $RUNDIR

echo ">>> Deploying Rubis..."
exec $JBOSS_HOME/bin/deploy-rubis.sh >& $RUNDIR/PetStore.log &

echo ">>> Starting DelayProxy..."
exec xterm -e $JAVA org.jboss.RR.DelayProxy >& $RUNDIR/DelayProxy.log &
echo $! > $RUNDIR/DelayProxy.pid
echo "    Log file is in $RUNDIR/DelayProxy.log"

if [ "$1" == "vanilla" ] ; then
    echo ">>> Starting TheBrain... (use full app RBs)"
    exec xterm -e $JAVA org.jboss.RR.TheBrain -o $JBOSS_TOP/server/src/main/org/jboss/RR/f-map.xml -vanilla $JBOSS_HOME/server/default/deploy/rubis.jar/ $JBOSS_HOME/server/default/deploy/ejb_rubis_web.war >& $RUNDIR/TheBrain.log &
else
#use full app restarts
    echo ">>> Starting TheBrain... (use uRBs)"
    exec xterm -e $JAVA org.jboss.RR.TheBrain -o $JBOSS_TOP/server/src/main/org/jboss/RR/f-map.xml $JBOSS_HOME/server/default/deploy/rubis.jar/ $JBOSS_HOME/server/default/deploy/ejb_rubis_web.war >& $RUNDIR/TheBrain.log &
fi

echo $! > $RUNDIR/TheBrain.pid
echo "    Log file is in $RUNDIR/TheBrain.log"

# For the time being, JNP is outside JBoss...
#
# echo ">>> Starting external JNP..."
# exec $JBOSS_TOP/naming/run.sh >& $RUNDIR/JNP.log &
# echo $! > $RUNDIR/JNP.pid
# echo "    Log file is in $RUNDIR/JNP.log"

# echo ">>> Starting Cloudscape database..."
# exec $J2EE_HOME/bin/cloudscape -stop >& /dev/null &
# exec $J2EE_HOME/bin/cloudscape -start >& $RUNDIR/Cloudscape.log &
# echo $! > $RUNDIR/Cloudscape.pid
# echo "    Log file is $RUNDIR/Cloudscape.log"
