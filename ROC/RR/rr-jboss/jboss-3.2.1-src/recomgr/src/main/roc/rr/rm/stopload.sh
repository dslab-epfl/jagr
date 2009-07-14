#!/bin/sh
kill -9 `ps -aef | grep 'java -classpath ./Client edu.rice.rubis.client.ClientEmulator' | awk '{print $2}'`
