#!/usr/bin/perl -w

#
# This script provides helper functions for running un
#
#

use strict;

require 'lib/pinpoint.pl';
require 'lib/jagrrecomgr.pl';

my $exptname = "Open-ended";

my $sublogdir;

sub init_expt {
    print "Initializing $exptname experiment script\n";

    my $logdir = get_logdir();
    $sublogdir = $logdir . "/" . "openended";

    ensure_dir_exists( $sublogdir );

    init_pinpoint();
}

sub get_exptname {
    return $exptname;
}

sub run_expt {

    # kill old instances, and deploy app
    my @config = prepare_app();

    my $jbosshome=getoptordie('jbossdir','JBOSS HOME directory');

    foreach (@config) {
	deploy_pinpoint( $jbosshome . "/server/" . $_ );
    }

    run_pinpoint( $sublogdir . "/pinpoint" );
    if( getopt('runrecomgr') ) {
	run_recomgr( $sublogdir . "/recomgr" );
    }
    run_app();

    # TODO: run forever (until ctl-break?)
    print "Press enter to end experiment\n";
    my $value = <STDIN>;

    print "Ending experiment...\n";

    kill_app();
    if( getopt('runrecomgr') ) {
	kill_recomgr();
    }
    kill_pinpoint();
    app_save_logs($sublogdir);
}

return 1;
