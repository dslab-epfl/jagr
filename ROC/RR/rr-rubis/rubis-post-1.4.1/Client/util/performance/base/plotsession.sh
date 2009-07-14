#!/bin/sh
#
# $Id: plotsession.sh,v 1.1 2004/04/23 23:14:27 skawamo Exp $
#
# Plotsession: It produces a file "session.ps" and its pdf version.
#  session.ps indicates the lifetime of each session.
#
# usage: plotsession.sh  trace_client.html 
#
INSTALLDIR=installdir

if [ $# -eq 1 ]
then 
    if [ ! -d "session" ]; then
	mkdir session
    fi

    if [ ! -f "times" ]; then
	${INSTALLDIR}/extract-times.awk $1 > times
    fi

    cat times $1 | ${INSTALLDIR}/session.awk > session/session.data
    GPFILE=session/session.gp

    echo set xlabel \"Time [sec]\" > ${GPFILE}
    echo set ylabel \"Life time of each session [sec]\" >> ${GPFILE}
    echo set yrange [0:] >> ${GPFILE}
    echo set xrange [0:] >> ${GPFILE}
    echo set terminal postscript >> ${GPFILE}
    echo set output \"session.ps\" >> ${GPFILE}
    echo plot \"session/session.data\" using 1:2 title \"Life time of each session\" with i >> ${GPFILE}
    
    gnuplot < ${GPFILE}
    ps2pdf session.ps

else

    echo "usage: plotsession.sh trace_client0.html"

fi
