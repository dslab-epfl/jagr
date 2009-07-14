#!/bin/sh
#
# $Id: plotresponsetime2.sh,v 1.2 2004/05/19 00:03:58 skawamo Exp $
#
# Plotresponsetime2: It produces a file "responsetime.png". 
#  responsetime.png indicates the response time of each client request
#
# usage: responsetime2.sh  user_ops.dat
#
INSTALLDIR=installdir

if [ $# -eq 1 ] 
then 
    if [ ! -d "data" ]; then
	mkdir data
    fi

    ${INSTALLDIR}/responsetime2.awk $1 > data/responsetime.data

    GP=data/response.gp
    echo set xlabel \"Time [sec]\" > ${GP}
    echo set ylabel \"Response Time [sec]\" >> ${GP}
    echo set yrange [0:] >> ${GP}
    echo set xrange [0:] >> ${GP}
    echo set terminal png >> ${GP}
    echo set output \"response.png\" >> ${GP}
    echo plot \"data/responsetime.data\" using 1:2 title \"response time of succeeded request\" with i 3, \"data/responsetime.data\" using 1:3 title \"response time of failed request\" with i 1 >> ${GP}
    
    gnuplot < ${GP}

else

    echo "usage: plotresponsetime2.sh user_ops.dat"

fi
