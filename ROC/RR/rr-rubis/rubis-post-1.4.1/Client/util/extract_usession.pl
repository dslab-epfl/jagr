#!/usr/bin/perl

# CVS
# $Id: extract_usession.pl,v 1.2 2004/03/19 01:47:01 fjk Exp $
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

# open input/output file
open(IN, $ARGV[0]);
$out;
if ( $ARGV[0] =~ m/\/[^\/]*$/){
    $original_file = $&;
    $out = $`."/extracted_";
    $original_file =~ s/\///;
    $out = $out.$original_file;
}
else{
    $out = "extracted_".$ARGV[0];
}
open(OUT, "> $out");

# extracts all lines that match "UserSession" and write
# out to the output file.
while ($line = <IN>) {
	if ($line =~ m/UserSession/) {
	    print (OUT $line);
	    if($line =~ m/HTML reply was:/){
		print(OUT "</font><br>");
	    }
	}
}

#close input/output file
close(IN);
close(OUT);

#print end message.
print "Successfully generated file : ";
print "$out\n";
