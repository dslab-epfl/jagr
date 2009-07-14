#!/bin/sh
#
# $Id: plotmemory.sh,v 1.6 2004/05/19 00:03:58 skawamo Exp $
#
# Plotmemory: It produces a file "memory.ps". 
#  memory.ps indicates the behavior of available and total memory.
#
# usage: plotmemory.sh  trace_client.html jboss-log
#
INSTALLDIR=installdir

if [ $# -eq 2 ] 
then 
    if [ ! -d "memory" ]; then
	mkdir memory
    fi

    if [ ! -f "times" ]; then
	${INSTALLDIR}/extract-times.awk $1 > times
    fi

    cat times $2 | ${INSTALLDIR}/memory.awk > memory/memory.data

    echo set xlabel \"Time [sec]\" > memory/memory.gp
    echo set ylabel \"Used Memory [MB]\" >> memory/memory.gp
    echo set yrange [0:] >> memory/memory.gp
    echo set xrange [0:] >> memory/memory.gp
    echo set terminal png >> memory/memory.gp
    echo set output \"memory.png\" >> memory/memory.gp
    echo plot \"memory/memory.data\" using 1:4 title \"Used\" with linespoints, \"memory/memory.data\" using 1:3 title \"Total\" with linespoints >> memory/memory.gp
    
    gnuplot < memory/memory.gp

else

    echo "usage: plotmemory.sh trace_client0.html JBoss-log"

fi
