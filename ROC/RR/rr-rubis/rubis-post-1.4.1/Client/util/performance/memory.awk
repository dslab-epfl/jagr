#!/bin/awk -f 

BEGIN { starttime = 0; endtime = 10000000000000 }

$1 == "starttime:" {
  starttime = $2;
}

$1 == "endtime:" {
  endtime = $2;
}

/Available memory/{
  if ( NF == 9 ) {
    times = $1;
    available = $6;
    total = $9;
  } else {
    times = $2;
    available = $7;
    total = $10;
  }

  hour = substr(times,1,2);
  min  = substr(times,4,2);
  sec  = substr(times,7,2);
  msec = substr(times,10,3);
  time = hour*3600+min*60+sec+msec/1000.0;

  if ( time - starttime >= 0 ) {
    if ( time - endtime < 0 ) {
      printf("%0.3f %d %d %d\n",time-starttime,
	     available/1048576,total/1048576,(total-available)/1048576);
    } else { 
      exit;
    }
  }
}
      
