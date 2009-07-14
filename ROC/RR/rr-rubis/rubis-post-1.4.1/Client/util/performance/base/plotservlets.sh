#!/bin/sh
#
# plotservlet: produce postscript figure of a specified servlet 
#
# usage: plotservlets.sh  trace_file
#
INSTALLDIR=installdir
SERVLETS="AboutMe Browse BrowseCategories BrowseRegions BuyNow Home \
Login Logout PutBid PutComment RegisterItem RegisterUser \
SearchItemsByCategory SearchItemsByRegion SellItemForm StoreBid \
StoreBuyNow StoreComment ViewBidHistory ViewItem ViewUserInfo"

if [ $# -eq 1 ] 
then 

    mkdir servlets
    mkdir servlets/data
    ${INSTALLDIR}/time.awk $1 > servlets/exectimes

  for servlet in ${SERVLETS}
  do

    DATA=servlets/data/${servlet}
    GPFILE=${DATA}.gp
    grep ${servlet} servlets/exectimes > ${DATA}

    echo set xlabel \"Time [sec]\" > ${GPFILE}
    echo set ylabel \"Execution time [msec]\" >> ${GPFILE}
    echo set yrange [0:] >> ${GPFILE}
    echo set terminal postscript >> ${GPFILE} 
    echo set output \"servlets/${servlet}.ps\" >> ${GPFILE}
    echo plot \"${DATA}\" title \"${servlet}\" with linespoints >> ${GPFILE}

    gnuplot < ${GPFILE}
    
    echo ${servlet} "done"

  done

else

  echo "usage: plotservlets.sh TraceFile"

fi
