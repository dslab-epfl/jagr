#!/bin/sh

ps -ef | grep bling | grep java | awk '{print $2}' | xargs kill -9
