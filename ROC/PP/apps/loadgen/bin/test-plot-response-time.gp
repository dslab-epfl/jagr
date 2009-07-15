set term postscript eps enhanced color solid 10
set size {0.5,0.2}

set xlabel 'Time [seconds]'        
set ylabel 'Response Time [msec]' 

###########################################################################

set output 'response_time_average.eps'
set title 'Average Response Time' 

plot 'test/response_time_buckets.tab' using 1:2 title 'test' with linespoints \
            linetype 3 linewidth 1 pointtype 2 pointsize 0.5


###########################################################################     

set output 'response_time_max.eps'
set title 'Maximum Response Time' 

plot 'test/response_time_buckets.tab' using 1:4 title 'test' with linespoints \
            linetype 3 linewidth 1 pointtype 2 pointsize 0.5


