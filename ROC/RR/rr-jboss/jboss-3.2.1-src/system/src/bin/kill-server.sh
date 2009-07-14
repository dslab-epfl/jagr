#!/bin/sh

# Kill a JBoss instance (assumes JBoss is the only Java thing running
# on the remote node).
#
# @version $Revision: 1.2 $
#
# @author  George Candea

# Kill all Java processes
killall -9 java

# Wait for all Java processes to disappear
while ps ax|grep -v grep|grep java >/dev/null; do
    usleep 10000
done

# Move server.log out of the way
cd $JBOSS_HOME/server/default/log
mv server.log server.log.`date +%F_%T`
exit 0
