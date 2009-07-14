#!/bin/awk -f
#
# usage: time-average tracefile.html
#

BEGIN { 
  i=0; 
  servlet[0] = "AboutMe";
  servlet[1] = "Browse"
  servlet[2] = "BrowseCategories";
  servlet[3] = "BrowseRegions";
  servlet[4] = "BuyNow";
  servlet[5] = "Home";
  servlet[6] = "Login";
  servlet[7] = "Logout";
  servlet[8] = "PutBid";
  servlet[9] = "PutComment";
  servlet[10] = "RegisterItem";
  servlet[11] = "RegisterUser";
  servlet[12] = "SearchItemsByCategory";
  servlet[13] = "SearchItemsByRegion";
  servlet[14] = "SellItemForm";
  servlet[15] = "StoreBid";
  servlet[16] = "StoreBuyNow";
  servlet[17] = "StoreComment";
  servlet[18] = "ViewBidHistory";
  servlet[19] = "ViewItem";
  servlet[20] = "ViewUserInfo";
}

/millisecond/{ 
  name[i]=$7;
  if ( NF == 15 ){
    time[i] = $9*60000+$11*1000+$13;
  } else if ( NF == 13 ){
    time[i] = $9*1000+$11;
  } else {
    time[i] = $9;
  }
  i++;
}

END {
  for(k=0;k<21;k++){
    min[k] = 100000000;
    max[k] = 0;
  }

  for(j=0;j<i;j++){
    for(k=0;k<21;k++){
      if(name[j] == servlet[k]){
	total[k] += time[j];
	call[k]++;
	if ( min[k] > time[j] )
	  min[k] = time[j];
	if ( max[k] < time[j] )
	  max[k] = time[j];
	break;
      }
    }
  }
  
  printf("               servlet\t     #\tmin[ms]\tmax[ms]\tave[ms]\n");
  for(l=0;l<21;l++){
    if ( call[l] == 0 ) {
      printf("%22s\t%7d\t%7d\t%7d\t%7d\n",servlet[l],0,0,0,0);
    } else {
      printf("%22s\t%7d\t%7d\t%7d\t%7d\n",servlet[l],call[l],
	     min[l],max[l],total[l]/call[l]);
    }
  }
}
