#!/bin/sh

source config.env

if [ -z $PP_ROOT ]; then
  echo Please define PP_ROOT
  exit 1;
fi;


export OBSERVATION_HOSTNAME=$OBSERVATION_MACHINE

# run observation machine as a backend
echo Killing observation machine on $OBSERVATION_HOSTNAME
export GEXEC_SVRS=$OBSERVATION_HOSTNAME
gexec -n 1 killall -9 java

echo Stopping any existing JBoss servers
killall sadc
killall -9 java
killall -9 rmiregistry


export mysqlpid_file=$PP_ROOT/apps/rubis-new/mysqlpid

if [ -s $mysqlpid_file ]; then
  export mysqlpid=`cat $mysqlpid_file`
  kill $mysqlpid
fi

  # mysql should remove pid file... wait for it to disappear
      sleep 1
      while [ -s $mysqlpid_file -a "$flags" != aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa ]
      do
        [ -z "$flags" ] && echo "Wait for mysqld to exit\c" || echo ".\c"
        flags=a$flags
        sleep 1
      done
      if [ -s $mysqlpid_file ]
         then echo " gave up waiting!"
      elif [ -n "$flags" ]
         then echo " done"
      fi


