set term postscript eps enhanced color solid 10
set size {0.5,0.2}

set xlabel 'Time [seconds]'        
set ylabel 'Response Time [msec]' 

###########################################################################

set output 'response_time_average.eps'
set title 'Average Response Time' 
set xrange [0:]
set yrange [0:3000]

plot 'normal_6_nodes_6000_clients/response_time_buckets.tab' using 1:2 \
            title 'normal (6)' with linespoints \
            linetype 47 linewidth 1 pointtype 2 pointsize 0.5, \
     'failover_jboss_6_nodes_6000_clients/response_time_buckets.tab'  using 1:2 \
            title 'JBoss failover (6) / 2 nodes' with linespoints \
            linetype 12 linewidth 1 pointtype 5 pointsize 0.5, \
     'failover_ejb_6_nodes_6000_clients/response_time_buckets.tab' using 1:2 \
            title 'EJB failover (6)' with linespoints \
            linetype 4 linewidth 1 pointtype 10 pointsize 0.5

###########################################################################     

set output 'response_time_max.eps'
set title 'Maximum Response Time' 
set xrange [0:]
set yrange [0:20000]

plot 'normal_6_nodes_6000_clients/response_time_buckets.tab' using 1:4 \
            title 'normal (6)' with linespoints \
            linetype 47 linewidth 1 pointtype 2 pointsize 0.5, \
     'failover_jboss_6_nodes_6000_clients/response_time_buckets.tab' using 1:4 \
            title 'JBoss failover (6)' with linespoints \
            linetype 12 linewidth 1 pointtype 5 pointsize 0.5, \
     'failover_ejb_6_nodes_6000_clients/response_time_buckets.tab' using 1:4 \
            title 'EJB failover (6)' with linespoints \
            linetype 4 linewidth 1 pointtype 10 pointsize 0.5


