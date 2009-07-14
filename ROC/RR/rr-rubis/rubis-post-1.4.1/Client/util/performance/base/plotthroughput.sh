#!/bin/sh
#
# $Id: plotthroughput.sh,v 1.1 2004/05/18 23:27:46 skawamo Exp $
#
# Plotthroughput.sh: produce a throughput graph of succedded and failed request
#
# usage: plotshroughput.sh  user_client0.html
#
INSTALLDIR=installdir

if [ $# -eq 1 ] 
then 
    if [ ! -d "data" ]; then
	mkdir data
    fi

    ${INSTALLDIR}/throughput.awk $1 > data/throughput.data

    GP=data/throughput.gp
    echo set xlabel \"Time [sec]\" > ${GP}
    echo set ylabel \"Throughput [request/sec]\" >> ${GP}
    echo set yrange [0:] >> ${GP}
    echo set xrange [0:] >> ${GP}
    echo set terminal png >> ${GP}
    echo set output \"throughput.png\" >> ${GP}
    echo plot \"data/throughput.data\" using 1:2 title \"succeeded request\" with linespoints 3, \"data/throughput.data\" using 1:3 title \"failed request\" with linespoints 1 >> ${GP}
    
    gnuplot < ${GP}

else

    echo "usage: plotthroughput.sh trace_client0.html"

fi
