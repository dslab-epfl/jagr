#!/usr/bin/perl -w

#
#  This script implements functions for deploying, start, and stopping
#  Petstore-1.1.2
#

use strict;

require 'lib/jboss.pl';
require 'apps/petstore-1.1.2/common.pl';

use vars qw($CONFIG);
use vars qw($CONFIGNAMEFRONTEND);
use vars qw($CONFIGNAMEBACKEND);
use vars qw($CLOUDSCAPE_HOME);

my $appname = "Petstore-1.1.2-singlenode";

sub init_app {
    print "Initializing $appname application script\n";
    init_ps112_app();
}

sub get_appname {
    return $appname;
}

sub deploy_app {
    deploy_backend( $main::CONFIGNAMEBACKEND );    

    deploy_db();

    return ($main::CONFIGNAMEBACKEND);
}

sub run_app {

    # start cloudscape
    print ">>> Starting Cloudscape database...\n";
    spawn_background_process( "$CLOUDSCAPE_HOME/bin/cloudscape -start", get_dbmachine() );

    my @frontendmachines = get_frontendmachines();

    start_jboss_cluster(\$CONFIGNAMEBACKEND,\@frontendmachines );
}

sub kill_app {

    my @frontendmachines = get_frontendmachines();

    print( "killing app...\n" );
    print( "    configfrontend is: ", $CONFIGNAMEFRONTEND, "\n" );
    print( "    frontend machines are: ", @frontendmachines, "\n" );

    kill_jboss_cluster(\$CONFIGNAMEBACKEND,\@frontendmachines );
    spawn_process( "killall -9 java", get_dbmachine() );
}

sub app_save_logs($) {
    my $logdir = $_[0];

    my @frontendmachines = get_frontendmachines();
    my @backendmachines = get_backendmachines();

    jboss_save_logs(\$logdir,\$main::CONFIGNAMEFRONTEND,\@frontendmachines );
    jboss_save_logs(\$logdir,\$main::CONFIGNAMEBACKEND,\@backendmachines );
}



return 1; # DO NOT REMOVE THIS LINE!




