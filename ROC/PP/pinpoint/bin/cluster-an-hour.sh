#!/bin/sh


export d=1000
export t=0

./run-pinpoint.sh ../conf/main/unsupported/inspect-query-log-cb.conf \
   inputfile=amzn-fault2-log/$d/$(($t+0)).gz,\
amzn-fault2-log/$d/$(($t+1)).gz,\
amzn-fault2-log/$d/$(($t+2)).gz,\
amzn-fault2-log/$d/$(($t+3)).gz,\
amzn-fault2-log/$d/$(($t+4)).gz,\
amzn-fault2-log/$d/$(($t+5)).gz,\
amzn-fault2-log/$d/$(($t+6)).gz,\
amzn-fault2-log/$d/$(($t+7)).gz,\
amzn-fault2-log/$d/$(($t+8)).gz,\
amzn-fault2-log/$d/$(($t+9)).gz,\
amzn-fault2-log/$d/$(($t+10)).gz,\
amzn-fault2-log/$d/$(($t+11)).gz,\
amzn-fault2-log/$d/$(($t+12)).gz,\
amzn-fault2-log/$d/$(($t+13)).gz,\
amzn-fault2-log/$d/$(($t+14)).gz,\
amzn-fault2-log/$d/$(($t+15)).gz,\
amzn-fault2-log/$d/$(($t+16)).gz,\
amzn-fault2-log/$d/$(($t+17)).gz,\
amzn-fault2-log/$d/$(($t+18)).gz,\
amzn-fault2-log/$d/$(($t+19)).gz,\
amzn-fault2-log/$d/$(($t+20)).gz,\
amzn-fault2-log/$d/$(($t+21)).gz,\
amzn-fault2-log/$d/$(($t+22)).gz,\
amzn-fault2-log/$d/$(($t+23)).gz,\
amzn-fault2-log/$d/$(($t+24)).gz,\
amzn-fault2-log/$d/$(($t+25)).gz,\
amzn-fault2-log/$d/$(($t+26)).gz,\
amzn-fault2-log/$d/$(($t+27)).gz,\
amzn-fault2-log/$d/$(($t+28)).gz,\
amzn-fault2-log/$d/$(($t+29)).gz,\
amzn-fault2-log/$d/$(($t+30)).gz,\
amzn-fault2-log/$d/$(($t+31)).gz,\
amzn-fault2-log/$d/$(($t+32)).gz,\
amzn-fault2-log/$d/$(($t+33)).gz,\
amzn-fault2-log/$d/$(($t+34)).gz,\
amzn-fault2-log/$d/$(($t+35)).gz,\
amzn-fault2-log/$d/$(($t+36)).gz,\
amzn-fault2-log/$d/$(($t+37)).gz,\
amzn-fault2-log/$d/$(($t+38)).gz,\
amzn-fault2-log/$d/$(($t+39)).gz,\
amzn-fault2-log/$d/$(($t+40)).gz,\
amzn-fault2-log/$d/$(($t+41)).gz,\
amzn-fault2-log/$d/$(($t+42)).gz,\
amzn-fault2-log/$d/$(($t+43)).gz,\
amzn-fault2-log/$d/$(($t+44)).gz,\
amzn-fault2-log/$d/$(($t+45)).gz,\
amzn-fault2-log/$d/$(($t+46)).gz,\
amzn-fault2-log/$d/$(($t+47)).gz,\
amzn-fault2-log/$d/$(($t+48)).gz,\
amzn-fault2-log/$d/$(($t+49)).gz,\
amzn-fault2-log/$d/$(($t+50)).gz,\
amzn-fault2-log/$d/$(($t+51)).gz,\
amzn-fault2-log/$d/$(($t+52)).gz,\
amzn-fault2-log/$d/$(($t+53)).gz,\
amzn-fault2-log/$d/$(($t+54)).gz,\
amzn-fault2-log/$d/$(($t+55)).gz,\
amzn-fault2-log/$d/$(($t+56)).gz,\
amzn-fault2-log/$d/$(($t+57)).gz,\
amzn-fault2-log/$d/$(($t+58)).gz,\
amzn-fault2-log/$d/$(($t+59)).gz \
                                              \
  outputfile=amzn-fault2-log/$d/5min-cluster-chi-2/hour.cluster
