#! /bin/sh

# Adapted from .bat file by Tom Vijlbrief

CP=.:../classes

CP=$CP:../lib/jboss-j2ee.jar
CP=$CP:../lib/jta-spec1_0_1.jar
CP=$CP:../lib/jnp-client.jar
CP=$CP:../lib/jbossmq-client.jar
CP=$CP:../lib/jpl-util-0_6b.jar
CP=$CP:../lib/jpl-pattern-0_3.jar
CP=$CP:../lib/jboss-util.jar
CP=$CP:$CLASSPATH

java -cp "$CP" org.jboss.admin.monitor.Main "$@"
