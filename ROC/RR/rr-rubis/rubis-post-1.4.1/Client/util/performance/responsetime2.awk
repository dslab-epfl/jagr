#!/bin/awk -f
#
# $Id: responsetime2.awk,v 1.1 2004/05/18 23:27:46 skawamo Exp $
#
# responsetime2.awk: culucurate response time of each client request
#
#  usage: responsetime2.awk  [user_ops.dat]
#

BEGIN { head = 1; }

NF == 5 {
  if (head == 1){
    starttime = $2;
    head = 0;
  }
}

/OK/ {
  printf("%.3f %.3f %.3f\n",($2-starttime)/1000, ($3-$2)/1000,-1);
}

/BAD/ {
  printf("%.3f %.3f %.3f\n",($2-starttime)/1000, -1,($3-$2)/1000);
}
