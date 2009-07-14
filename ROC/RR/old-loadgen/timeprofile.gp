# $Id: timeprofile.gp,v 1.3 2003/03/20 14:36:10 candea Exp $
#
# set term postscript eps enhanced color
set term png small color
# set output "/tmp/preliminary.eps"
set output "/tmp/preliminary.png"
set size 0.7,0.8
set title "Uptime Profile of JBoss running PetStore"
set xlabel "Time [minutes]"
set ylabel "Svc Up/Down"
set yrange [0:1.2]
set ytics (0)
plot 't2.trc' using ($1/60000):($3+0.005) title "JBoss (whole app restart)" with steps lw 2, 't1.trc' using ($1/60000):($3+0.01) title "JBoss with micro-reboots" with steps lw 2
