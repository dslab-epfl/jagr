#!/bin/awk -f
#
# $Id: responsetime.awk,v 1.4 2004/05/13 09:47:04 skawamo Exp $
#
# responsetime.awk: culucurate response time of each client request
#
#  usage: responsetime.awk  trace_client0.html
#

BEGIN { head = 1; }

/UserSession/ && (head == 1) {
  times = $4;
  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  starttime = hour*3600+min*60+sec+msec/1000.0;
  head = 0;
}

/UserSession/ && /going to/ {
  sessionId = substr($2,match($2,">")+1,14);
  if ( NF == 12 ) {
    name = substr($12,1,match($12,"<br>")-1);
  } else {
    name = $12;
  }
  servlet[sessionId] = name;

  sessionId = substr($2,match($2,">")+1,14);
  times = $4;
  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  time = hour*3600+min*60+sec+msec/1000.0;
  start[sessionId] = time;
}

/UserSession/ && /resetToInitialState/ {
  sessionId = substr($2,12,30);
  servlet[sessionId] = $11;
  sessionId = substr($2,match($2,">")+1,14);
  times = $4;
  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  time = hour*3600+min*60+sec+msec/1000.0;
  start[sessionId] = time;
}

/UserSession/ && ( /Error in HTML returned/ || /Error: Network error accessing/ || /Error: Got bad HTTP response/ ){

  sessionId = substr($2,match($2,">")+1,14);
  times = $4;
  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  time = hour*3600+min*60+sec+msec/1000.0;
  response = time - start[sessionId];

  print start[sessionId]-starttime, -1, response, servlet[sessionId];
}

/UserSession/ && /GOOD.*HTML reply was/ {
  sessionId = substr($2,match($2,">")+1,14);
  times = $4;
  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  time = hour*3600+min*60+sec+msec/1000.0;
  response = time - start[sessionId];

  print start[sessionId]-starttime, response, -1, servlet[sessionId];
}
