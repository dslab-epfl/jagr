@echo off

start java -jar /home/mdelgado/ROC/RR/rr-jboss/naming/output/lib/jnpserver.jar

echo Press any key when the server is started to continue
pause
java -jar /home/mdelgado/ROC/RR/rr-jboss/naming/output/lib/jnptest.jar

pause
