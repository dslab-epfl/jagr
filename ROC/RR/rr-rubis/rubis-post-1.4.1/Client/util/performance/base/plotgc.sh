#!/bin/sh
#
# $Id: plotgc.sh,v 1.7 2004/04/30 16:25:04 skawamo Exp $
#
# Plotservlet: It produces two files "gc.ps" and "heap.ps". 
#  gc.ps indicates the behavior of gc.
#  heap.ps indicates the usage of heap memory.
#
# usage: plotgc.sh  trace_client0.html  jboss-log gc-log
#
INSTALLDIR=installdir

if [ $# -eq 3 ] 
then 

  if [ ! -d "memory" ]; then     
      mkdir memory
  fi
  
  if [ ! -f "memory/times-for-gc" ]; then
      ${INSTALLDIR}/extract-duration.awk $1 > memory/times-for-gc
      ${INSTALLDIR}/extract-offset.awk $2 >> memory/times-for-gc
  fi

  cat memory/times-for-gc $3 | ${INSTALLDIR}/gc.awk > memory/gctime.data

  #
  # Full GC information
  #
  echo set xlabel \"Time [sec]\" > memory/gc.gp
  echo set ylabel \"Full GC Time [sec]\" >> memory/gc.gp
  
  echo set terminal png >> memory/gc.gp
  echo set output \"gc.png\" >> memory/gc.gp
  echo plot \"memory/gctime.data\" title \"Full GC\" with i >> memory/gc.gp
  
  gnuplot < memory/gc.gp

  #
  # cummulative GC time
  #
  echo set xlabel \"Time [sec]\" > memory/cummulativegc.gp
  echo set ylabel \"Commulative GC Times [sec]\" >> memory/cummulativegc.gp
  
  echo set terminal png >> memory/cummulativegc.gp
  echo set output \"cummulativegc.png\" >> memory/cummulativegc.gp
  echo plot \"memory/gctime.data\" using 1:3 title \"Cummulative GC Times\" with linespoints >> memory/cummulativegc.gp
  
  gnuplot < memory/cummulativegc.gp

else

  echo "usage: plotgc.sh trace_client0.html JBoss-log"

fi
