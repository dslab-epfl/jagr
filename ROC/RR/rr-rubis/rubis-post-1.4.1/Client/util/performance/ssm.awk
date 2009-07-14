#!/bin/awk -f

BEGIN { starttime = 0; endtime = 10000000000000 }

$1 == "starttime:" {
  starttime = $2;
}

$1 == "endtime:" {
  endtime = $2;
}

/\[SSM\] try to write/{
  times = $1;
  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  time = hour*3600+min*60+sec+msec/1000.0;
  userid = $8;
  start[userid] = time;
}

/\[SSM\] write/{
  times = $1;
  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  time = hour*3600+min*60+sec+msec/1000.0;

  userid = $6

  if ( start[userid] != 0 ) {
    interval = time - start[userid];
    total += interval;
    i++;
    print start[userid]-starttime, interval, substr($6,20);
  } else {
    print "Error: ", userid;
  }
}

END {
  if ( i > 0 ) {
    print "average: ", total/i;
  } else {
    print "average: ", 0;
  }
}
