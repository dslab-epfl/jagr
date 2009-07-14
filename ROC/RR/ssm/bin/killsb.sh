#!/bin/sh

kill -9 `ps -ef | grep "roc.rr.ssm.Start" | awk '{print $2}'` 
