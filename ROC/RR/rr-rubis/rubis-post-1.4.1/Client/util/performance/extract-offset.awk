#!/bin/gawk -f 
#
# extract-offset.awk: extract offset from jboss log 
#  
#  usage: extract-offset.awk jboss-log
#
#  Apr/27/2004 S.Kawamoto
#

/Starting JBoss/ { 
  times = $1;
  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  starttime = hour*3600+min*60+sec+msec/1000.0;
} 

/Started a new listener thread for host/{
  times = $1;
  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  expstarttime = hour*3600+min*60+sec+msec/1000.0;

  print "offset:", expstarttime - starttime;
  exit 0;
}
