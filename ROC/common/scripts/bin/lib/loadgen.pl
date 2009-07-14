#!/bin/perl -w

###################################
#### Loadgen-related functions ####
###################################

use strict;
use File::chdir;


require "lib/util.pl";

sub run_loadgen($$$) {

    if( defined(&app_run_loadgen) ) {
	print "loadgen.pl: using application-specific load generator";
	app_run_loadgen(@_);
	return;
    }

    my $logdir=$_[0];
    my $loadconfig=$_[1];
    my $loadmagnitude=$_[2];

    my $ROCROOTDIR = getoptordie('rocrootdir','Root of ROC/PP source tree');
    my $loadgendir = "$ROCROOTDIR/common/loadgen4/";

    my @frontendmachines = get_frontendmachines();
    my $hostnamelist= convert_array_to_csv(@frontendmachines);
    
    my @tracefiles = get_app_loadgen_traces();
    my $tracelist= convert_array_to_csv(@tracefiles);

    my @loadgenmachines=get_clientmachines();

    my $abslogdir = $CWD . "/" . $logdir;

    ensure_dir_exists($abslogdir);

    my $checksumfile = get_app_loadgen_checksum();

    foreach (@loadgenmachines) {
	
	my $lm = $_;

	my $stdout= $abslogdir . "/stdout." . $lm;
	my $stderr= $abslogdir . "/stderr." . $lm;
	{
	    local $CWD=$loadgendir;
	    spawn_background_process("java -cp dist/lib/loadgen4.jar:../swig-util/dist/lib/swigutil.jar:../swig-util/lib/xercesImpl.jar:../swig-util/lib/xmlParserAPIs.jar roc.loadgen.Engine conf/$loadconfig tracefile=$tracelist hostname=$hostnamelist port=8080 checksums=$checksumfile loadmag=$loadmagnitude log=$abslogdir 1>$stdout 2>$stderr",$lm);
	}
    }

}

sub kill_loadgen() {

    if( defined(&app_kill_loadgen) ) {
	print "loadgen.pl: using application-specific load generator killer";
	app_kill_loadgen(@_);
	return;
    }

    my @loadgenmachines=get_clientmachines();

    print( "killing loadgen on machines: ", @loadgenmachines, "\n" );

    foreach (@loadgenmachines) {
	spawn_process("killall -9 java", $_);
    }

}

return 1;
