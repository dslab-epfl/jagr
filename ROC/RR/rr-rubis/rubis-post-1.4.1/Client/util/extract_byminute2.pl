#!/usr/bin/perl

# CVS
# $Id: extract_byminute2.pl,v 1.2 2004/03/31 02:01:52 fjk Exp $
#

#check argument
if ($#ARGV != 0){
    print "usage : ./extract_ussession.pl file_name\n";
    exit;
}

if (! -r $ARGV[0]){
    print "usage : ./extract_ussession.pl file_name\n";
    exit;
}

# open input file
open(IN, $ARGV[0]);

# open default file
if( $ARGV[0] =~ m/\/[^\/]*$/){
    $original_file = substr($&,1);
    $directory = "$`/byminute";
} else {
    $original_file = $ARGV[0];
    $directory = "byminute";
}
if(! -d $directory){
    mkdir("$directory",0755);
}
$out = $directory."/header_part";
open(OUT, ">> $out");

$date = "date";
while($line = <IN>){
    # the format has been changed.
    # if ($line =~ m/\d\d:\d\d:\d\d\:\d\d\d/){
    if ($line =~ m/\d\d:\d\d:\d\d\,\d\d\d/){
	$current_time = substr($&,0,5);
	$clock = substr($current_time,0.2);
	$min = substr($current_time,3,2);
	$min = $clock*60+$min;
	if($current_time ne $time){
            $time = $current_time;
            close(OUT);
            $out = $directory."/".$time."_".$original_file;
            open(OUT, ">> $out");
	    if($min > $max_min){
            	print "generated file : ";
            	print "$out\n";
	        $max_min = $min;
	    }
	}
    }
    print(OUT $line);
}
    

#close input/output file
close(IN);
close(OUT);

#print end message.
print "successfully ended.\n";

