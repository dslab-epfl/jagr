#!/bin/gawk -f 
#
# extract-times.awk: extract start and end time of experiment 
#  from a log file of client emulator
#  
#  usage: extract-times.awk trace_client0.html
#
#  Apr/08/2004 S.Kawamoto
#

BEGIN { head = 1 }

/UserSession/ && $1 == "<font" {
  times = $4;
  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  time = hour*3600+min*60+sec+msec/1000.0;
  if ( head == 1 ) {
    printf("%s %0.3f\n","starttime: ", time);      
    head = 0;
  }

}

END {
  printf("%s %0.3f\n","endtime: ", time);      
}
