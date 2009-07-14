#!/bin/sh
#
# plotservlet: produce postscript figure of a specified servlet 
#
# usage: plotservlet.sh  trace_file  Servlet_name
#
INSTALLDIR=installdir

if [ $# -eq 2 ] 
then 

  ${INSTALLDIR}/time.awk $1 | grep $2 > exectime

  echo set xlabel \"Time [sec]\" > exectime.gp
  echo set ylabel \"Execution time [msec]\" >> exectime.gp
  echo set yrange [0:] >> exectime.gp
  echo set terminal postscript >> exectime.gp
  echo set output \"$2.ps\" >> exectime.gp
  echo plot \"exectime\" title \"$2\" with linespoints >> exectime.gp

  gnuplot < exectime.gp

else

  echo "usage: plotservlet.sh TraceFile ServletName"

fi
