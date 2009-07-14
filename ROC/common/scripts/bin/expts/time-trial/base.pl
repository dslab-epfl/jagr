#!/usr/bin/perl -w

#
#  this script provides base functionality for time-trial expts.
#  (e.g., run for N minutes, while injecting a fault, observe with
#  pinpoint, then stop).
#
use strict;

require 'lib/pinpoint.pl';
require 'lib/loadgen.pl';
require 'lib/jagrrecomgr.pl';

my $sublogdir;
my $runrecoverymanager;

sub base_init_expt($) {
    my $subexptname = $_[0];

    my $logdir = get_logdir();
    $sublogdir = $logdir . "/" . $subexptname;

    $runrecoverymanager = getopt('runrecomgr');
    
    ensure_dir_exists( $sublogdir );

    init_pinpoint();
}

sub base_start_running_expt {
    
    # kill old instances, and deploy app
    kill_app();
    kill_pinpoint();
    my @confignodes = prepare_app();

    my $jbosshome=getoptordie('jbossdir','JBOSS HOME directory');

    foreach (@confignodes) {
	deploy_pinpoint( $jbosshome . "/server/" .$_ );
	if( defined(&expt_post_deploy)) {
	    expt_post_deploy( $jbosshome . "/server/" . $_);
	}
    }

    print "Beginning experiment...\n";
    run_pinpoint( $sublogdir . "/pinpoint" );
    if( $runrecoverymanager ) {
	run_recomgr( $sublogdir . "/recomgr" );
    }
    run_app();
}

sub base_stop_running_expt() {
    print "Ending experiment...\n";

    kill_app();
    if( $runrecoverymanager ) {
	kill_recomgr();
    }
    kill_pinpoint();
    app_save_logs($sublogdir);
}


sub base_run_timed_expt($$) {

    my $timetorun = $_[0];
    my $exptname= $_[1];

    kill_loadgen();

    base_init_expt($exptname);

    base_start_running_expt();

    print "Sleeping for 150 seconds... while servers start up...\n";
    sleep 150;

    print "Starting Load Generator...";

    my $loadconf = "OSDILoad.conf";

    my $makechecksums=getopt('createchecksums');
    if( $makechecksums ) {
	$loadconf = "OSDILoad-generatechecksums.conf";
    }

    if( defined(&expt_setup_faults)) {
	expt_setup_faults();
    }


    run_loadgen($sublogdir . "/loadgen", $loadconf, 4 );

    print "Sleeping for " . $timetorun . " seconds... while experiment runs.\n";
    sleep $timetorun;

    print "Stopping Experiment...";

    kill_loadgen();

    base_stop_running_expt();
}

return 1;
