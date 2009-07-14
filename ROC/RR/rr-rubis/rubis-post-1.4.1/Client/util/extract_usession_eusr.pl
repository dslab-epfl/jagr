#!/usr/bin/perl

# CVS
# $Id: extract_usession_eusr.pl,v 1.2 2004/03/19 01:47:01 fjk Exp $
#

#check arguments
if($#ARGV != 1){
    print "usage : ./extract_usession_euser.pl file_name config_file_name\n";
    exit;
}

if(! -r $ARGV[0]){
    print "usage : ./extract_usession_euser.pl file_name config_file_name\n";
    exit;
}

if(! -r $ARGV[1]){
    print "usage : ./extract_usession_euser.pl file_name config_file_name\n";
    exit;
}

#extract config_variables from config_file( ex.rubis_sess_state.properties)
$session_num;
open(IN, $ARGV[1]);
while($line_conf = <IN>){
    if($line_conf =~ m/workload_number_of_clients_per_node/){
        if($' =~ m/=(\s)*/ && $' =~ m/\n/){
	    $session_num = $`;
        }
    }
}
close(IN);

#check trace-client0.html 
#and extracts lines that match "UserSessionXX"
for($I=0; $I<$session_num; $I++){
	$session = "UserSession".$I;
	
        # open input/output file
	open(IN, $ARGV[0]);
        if($ARGV[0] =~ m/\/[^\/]*$/ ){
		if(! -d "$`/$session"){
		    mkdir("$`/$session",0755);
		}
		$out = $`."/".$session."/";
	        $original_file = "$&";
		$original_file =~ s/\///;
		$out = $out.$original_file;
	} else {
		if(! -d "$session"){
		    mkdir("$session",0755);
		}
		$out = $session."/".$ARGV[0];
	}
	open(OUT, "> $out");

        #read input file and if line matches UserSessionXX,
        #then writes to the output file.
	$counter = 0;
	while ($line = <IN>) {
		#print "$line";
		#print "$session\n";
		if ($line =~ m/$session /) {
			print(OUT $line);
			if($line =~ m/HTML reply was:/){
			    print(OUT "</font><br>");
			}
			$counter++;
		}
	}

	#close input/output file.
        #if there were no data to write, then delete
        #output file.
	close(IN);
	close(OUT);
	if($counter == 0){
		unlink $out;
	}
	#print success message
	print "Successfully generated file : ";
	print "$out\n";
}


