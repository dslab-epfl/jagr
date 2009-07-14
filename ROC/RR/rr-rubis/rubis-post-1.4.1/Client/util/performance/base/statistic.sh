#!/bin/sh 
#
# $Id: statistic.sh,v 1.1 2004/05/08 05:21:45 skawamo Exp $
#
# Calcurate min, max, average from a series of datum 
#
# usage: statistic.sh prefix_pattern suffix_pattern filename
#
INSTALLDIR=installdir

if [ $# -eq 3 ] 
then 
    awk "/$1[0-9.]+$2/{sub(\"$1\",\"\"); sub(\"$2\",\"\"); print \$0}" $3 | ${INSTALLDIR}/statistic.awk 
else    
    echo "usage: statistic.sh prefix_pattern suffix_pattern filename"
fi