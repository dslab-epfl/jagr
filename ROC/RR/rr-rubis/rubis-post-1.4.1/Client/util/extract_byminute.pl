#!/usr/bin/perl

# CVS
# $Id: extract_byminute.pl,v 1.1 2004/03/19 02:11:43 fjk Exp $
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
open(OUT, "> $out");

$date = "date";
while($line = <IN>){
    if ($line =~ m/UserSession\d\s\[\d\d\/\d\d\/\d\d\s/) {
	if ("$'" =~ m/\d\d:\d\d/ && "$&" ne $time){
	    $time = "$&";
	    close(OUT);
	    $out = $directory."/".$time."_".$original_file;
	    open(OUT, "> $out");
	    print "generated file : ";
	    print "$out\n";
	}
    }
    print(OUT $line);
}
    

#close input/output file
close(IN);
close(OUT);

#print end message.
print "successfully ended.\n";

