#!/bin/awk -f
#
#  extract error pages from the trace file
#
#   usage:  extract_error.awk  trace_file
#
#   Mar/26/2004 S.Kawamoto

# for CVS
# $Id: extract_error.awk,v 1.1 2004/03/27 02:47:23 fjk Exp $

BEGIN{ error = 0; start = 0 }

/computeURL/ { start = 1; error = 0; i = 0; }

/Error returned from access to/{ 
  error = 1;
  for(j=0;j<i;j++){
    print line[j];
  }
}

/Resetting session because HTML response contained an error/{ start = 0; }

{
  if ( start == 1 && error == 0 ){
    line[i++] = $0;
  } else if ( error == 1 ) {
    print $0;
  } 
}
    

