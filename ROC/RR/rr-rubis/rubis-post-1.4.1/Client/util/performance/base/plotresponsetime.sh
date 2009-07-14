#!/bin/sh
#
# $Id: plotresponsetime.sh,v 1.3 2004/05/13 09:47:04 skawamo Exp $
#
# Plotresponsetime: It produces a file "responsetime.png". 
#  responsetime.png indicates the response time of each client request
#
# usage: responsetime.sh  trace_client.html
#
INSTALLDIR=installdir

if [ $# -eq 1 ] 
then 
    if [ ! -d "data" ]; then
	mkdir data
    fi

    grep "<font" $1 | ${INSTALLDIR}/responsetime.awk > data/responsetime.data

    GP=data/response.gp
    echo set xlabel \"Time [sec]\" > ${GP}
    echo set ylabel \"Response Time [sec]\" >> ${GP}
    echo set yrange [0:] >> ${GP}
    echo set terminal png >> ${GP}
    echo set output \"response.png\" >> ${GP}
    echo plot \"data/responsetime.data\" using 1:2 title \"response time of succeeded request\" with i 3, \"data/responsetime.data\" using 1:3 title \"response time of failed request\" with i 1 >> ${GP}
    
    gnuplot < ${GP}

else

    echo "usage: plotresponsetime.sh trace_client0.html"

fi
