#!/bin/sh

if [ -z $JBOSS_TOP ]; then
  echo Please define JBOSS_TOP
  exit 1;
fi

if [ -z $1 ]; then

  echo Compiling all the loadgen code
  javac -classpath ${CLASSPATH}:${JBOSS_TOP}/../jargs/lib Proxy2.java Proxy.java LoadGen.java LoadGen2.java
  echo
  echo Make sure to run 'ant jar' in ../jargs
  echo
  javac -classpath ../jargs/lib/jargs.jar:$JBOSS_TOP/server/output/classes LoadGen3.java
  echo Done...

else 
  if [ $1 == 'clean' ]; then
    echo Removing *.class files;
    rm *.class;
  else
    echo Unrecognized build command $1;
  fi
fi
