#!/bin/awk -f
#
# usage: time tracefile.html
#

BEGIN { begin = 0 }


/HTML reply was/{
  hour=substr($4,1,2);
  min=substr($4,4,2);
  sec=substr($4,7,2);
  msec=substr($4,10,3);
  time=hour*3600+min*60+sec+msec/1000.000
}

/millisecond/{ 
  name[i]=$7;
  if ( NF == 15 ){
    etime = $9*60000+$11*1000+$13;
  } else if ( NF == 13 ){
    etime = $9*1000+$11;
  } else {
    etime = $9;
  }
  if ( begin == 0 ) {
    begin = time;
  }

  printf("%.3f %d %s\n",time-begin,etime,$7);
}
