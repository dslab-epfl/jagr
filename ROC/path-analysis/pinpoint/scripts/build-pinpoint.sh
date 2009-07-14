#!/bin/sh

source config.env

ORIGDIR=$(pwd)
PINPOINT_JAR=$PINPOINT_HOME/output/pinpoint.jar

cd $PINPOINT_HOME/lib

for i in *.jar; do CLASSPATH=$CLASSPATH:$(pwd)/$i; done

cd $PINPOINT_HOME/src/main

echo "Compiling Pinpoint Java files..."
#echo "[ Using CLASSPATH=$CLASSPATH ]"

##
#JAVAC="javac -classpath $CLASSPATH"
JAVAC="jikes -classpath /usr/java/j2sdk1.4.1/jre/lib/rt.jar:$CLASSPATH"

if find . -name "*.java" | xargs $JAVAC;
then
  echo "Creating Jar files"
  find swig roc -name "*.class" | xargs jar cf $PINPOINT_JAR
  echo "Done";
else
  echo "Error compiling Pinpoint!";
fi

cd $ORIGDIR
