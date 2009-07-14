#!/bin/awk -f
#
# $Id: gc.awk,v 1.1 2004/04/28 03:36:55 skawamo Exp $
#
# gc.awk: extract data from gc log of JVM
#
# usage: gc.awk gc-log
#

/offset:/{ offset = $2 }
/duration:/{ duration = $2 }


/Full GC/{ 
  time = substr($1,1,match($1,":")-1) - offset;
  comgc += $5;
  
  if ( time > 0  && (duration - time > 0) ) {
    printf("%0.3f %0.3f %0.3f\n",time-starttime,$5,comgc);
  }
}
