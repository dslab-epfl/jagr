#!/bin/sh -v

javac -classpath .:../../ssm.jar:$JBOSS_HOME/server/default/lib/javax.servlet.jar -d build/WEB-INF/classes src/SessionDemo.java

cd build
jar cvf ../webtest.war .

