#!/bin/sh

# Wait for default JBoss to complete startup.  This assumes JBoss is
# in the process of starting up, so the log file is accurately
# reflecting its progress (as opposed to indicating that an old,
# already-dead instance has started up in the past).
#
# @version $Revision: 1.1 $
#
# @author  George Candea

while (1>0)
do
   grep "\[org.jboss.system.server.Server\] JBoss (MX MicroKernel)" $JBOSS_HOME/server/default/log/server.log >/dev/null
   if [ "$?" -eq "0" ]
   then
      exit 0
   fi
   usleep 100000
done
