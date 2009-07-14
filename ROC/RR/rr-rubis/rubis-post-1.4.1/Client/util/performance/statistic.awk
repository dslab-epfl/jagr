#!/bin/awk -f

# $Id: statistic.awk,v 1.3 2004/05/18 23:27:46 skawamo Exp $
#
# calcurate min, max, average 
#


BEGIN { max = 0 ; min = 10000000000 } 

{
  data[i++] = $1;

  if ( $1 > max ) {
    max = $1;
  } 
  if ( $1 < min ) {
    min = $1;
  }

  sum += $1;
}

END {
  average = sum / i;

  for(k=0;k<i;k++) {
    s = (data[k]-average)^2;
    total += s;
  }
  sdev = sqrt(total/i);

  printf("%10s %10s %10s %10s\n","max  ","min  ","ave  ","sdev  ");
  printf("%10.3f %10.3f %10.3f %10.3f\n",max,min,average,sdev);
}
