#!/bin/awk -f
#
# $Id: throughput.awk,v 1.1 2004/05/18 23:27:46 skawamo Exp $
#
# throughput.awk: culucurate throughput of both succeeded and failed requests.
#
#  usage: throughput.awk trace_client0.html
#

BEGIN { head = 1; bucket = 2;}

/UserSession/ && (head == 1) {
  times = $4;
  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  starttime = hour*3600+min*60+sec+msec/1000.0;
  head = 0;
}

/UserSession/ && ( /Error in HTML returned/ || /Error: Network error accessing/ || /Error: Got bad HTTP response/ ){
  times = $4;
  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  time = hour*3600+min*60+sec+msec/1000.0;

  i = int( (time - starttime)/bucket );

  bad[i]++;
}

/UserSession/ && /GOOD.*HTML reply was/ {
  times = $4;
  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  time = hour*3600+min*60+sec+msec/1000.0;

  i = int((time - starttime)/bucket );
  good[i]++;
}


END {
  for(k=0;k<i;k++){
    printf("%d %d %d\n",k*bucket,good[k]/bucket,bad[k]/bucket);
  }
}
