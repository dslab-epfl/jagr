
set term svg
set output "requestids.svg"

#set term postscript eps enhanced mono
#set output "requestids.eps"

#set term png small color
#set output "requestids.png"

set size 0.7,0.7
set title ""
set xlabel "Score"
set ylabel "Num requests"

plot [0:0.2] [0:100] \
'badrequests.trc' using 1:2 title "Failed requests" with boxes,\
'goodrequests.trc' using 1:2 title "Successful requests" with boxes



