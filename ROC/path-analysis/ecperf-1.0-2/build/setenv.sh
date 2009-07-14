#!/bin/sh
export CLASSPATH=.

##
## you should already have JAVA_HOME, JBOSS_HOME and ECPERF_HOME set.
##export JAVA_HOME=...
##export JBOSS_HOME=...
##export ECPERF_HOME=...

## ---------------------
## JAVA
## ---------------------
##export PATH=$JAVA_HOME/bin:$PATH

## ---------------------
## ant
## ---------------------
export ANT_HOME=$ECPERF_HOME/ant1.4.1
export PATH=$ANT_HOME/bin:$PATH

## ---------------------
## JBOSS
## ---------------------
##export CLASSPATH=$CLASSPATH:$JBOSS_HOME/lib/ext/jboss.jar
##export CLASSPATH=$CLASSPATH:$JBOSS_HOME/lib/ext/jboss-j2ee.jar
##export CLASSPATH=$CLASSPATH:$JBOSS_HOME/lib/ext/xerces.jar
##export CLASSPATH=$CLASSPATH:$JBOSS_HOME/lib/jboss-jdbc_ext.jar

for i in $(ls $JBOSS_HOME/lib); do \
  CLASSPATH=$CLASSPATH:$JBOSS_HOME/lib/$i; \
done

for i in $(ls $JBOSS_HOME/server/all/lib); do \
  CLASSPATH=$CLASSPATH:$JBOSS_HOME/server/all/lib/$i; \
done


export J2EE_HOME=$JBOSS_HOME


## ---------------------
## JDBC
## ---------------------
##CLASSPATH=$CLASSPATH:D:\app\JBoss\JBoss-2.4.3_Tomcat-3.2.3\jboss\lib\ext\classes12.zip


export CLASSPATH




