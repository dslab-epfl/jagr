#!/bin/gawk -f 
#
# $Id: extract-duration.awk,v 1.1 2004/04/28 03:36:55 skawamo Exp $
#
# extract-duration.awk: extract duration of experiment from trace file 
#  of client emulator
#  
#  usage: extract-duration.awk trace_client0.html
#
#  Apr/27/2004 S.Kawamoto
#

BEGIN { begin = 1 ; }

/<font color=/{
  times = $4;
  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  if ( begin == 1 ) {
    begin = 0;
    starttime = hour*3600+min*60+sec+msec/1000.0;
  } else {
    stoptime = hour*3600+min*60+sec+msec/1000.0;
  }
} 


END {
  print "duration:", stoptime - starttime;
}
