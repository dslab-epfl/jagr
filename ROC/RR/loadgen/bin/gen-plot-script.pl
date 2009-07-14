#!/usr/bin/perl

open( GPSCRIPT, ">plot-response-time.gp" ) || die "Couldn't create file\n" ;
print "Using: ";

print GPSCRIPT "set term postscript eps enhanced color solid 10\n";
print GPSCRIPT "set size {0.5,0.2}\n\n";

#
# AVERAGE RESPONSE TIME
#

print GPSCRIPT "# AVERAGE RESPONSE TIME\n\n";
print GPSCRIPT "set output 'average_response_time.eps'\n";
print GPSCRIPT "set title 'Average Response Time'\n";
print GPSCRIPT "set xlabel 'Time [seconds]'\n";
print GPSCRIPT "set ylabel 'Response Time [msec]'\n";
print GPSCRIPT "set xrange [0:]\n";
print GPSCRIPT "set yrange [0:]\n";

print GPSCRIPT "plot \\\n";

foreach $argnum (0 .. $#ARGV-1) 
{
    $file = $ARGV[ $argnum ];
    print $file." ";
    print GPSCRIPT "   \'$file/response_time_buckets.tab' using 1:2 title \'\' with linespoints linewidth 1 pointsize 0.5, \\\n";
}

$file = $ARGV[ $#ARGV ];
print $file."\n";
print GPSCRIPT "   \'$file/response_time_buckets.tab' using 1:2 title \'\' with linespoints linewidth 1 pointsize 0.5\n\n";

#
# MAXIMUM RESPONSE TIME
#

print GPSCRIPT "# MAXIMUM RESPONSE TIME\n\n";
print GPSCRIPT "set output 'maximum_response_time.eps'\n";
print GPSCRIPT "set title 'Maximum Response Time'\n";

print GPSCRIPT "plot \\\n";

foreach $argnum (0 .. $#ARGV-1) 
{
    $file = $ARGV[ $argnum ];
    print GPSCRIPT "   \'$file/response_time_buckets.tab' using 1:4 title \'\' with linespoints linewidth 1 pointsize 0.5, \\\n";
}

$file = $ARGV[ $#ARGV ];
print GPSCRIPT "   \'$file/response_time_buckets.tab' using 1:4 title \'\' with linespoints linewidth 1 pointsize 0.5\n\n";

close( GPSCRIPT );
