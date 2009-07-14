#!/bin/sh



ps -ef | grep `whoami` | grep java | awk '{print $2}' | xargs kill -9
sleep 1.5
~/ROC/SS/manualsb.sh $1 

