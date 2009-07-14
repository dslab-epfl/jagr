#!/bin/sh

# Remotely reboot a JBoss instance (assumes JBoss is the only thing
# running on the remote node).
#
# @version $Revision: 1.3 $
#
# @author  George Candea

# Check arguments
if [ $# -ne 1 ]; then
   echo 1>&2 Usage: $0 hostname
   exit 127
fi

# Kill the server
ssh $1 "$JBOSS_HOME/bin/kill-server.sh > /dev/null"

usleep 2000000

# Start JBoss up in the background
ssh -f $1 "$JBOSS_HOME/bin/run.sh > /dev/null"

usleep 5000000

# Wait for startup to complete
ssh $1 "$JBOSS_HOME/bin/wait-for-server.sh"
exit 0
