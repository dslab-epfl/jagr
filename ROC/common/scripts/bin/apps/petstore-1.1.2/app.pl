#!/usr/bin/perl -w

#
#  This script implements functions for deploying, start, and stopping
#  Petstore-1.1.2
#

use strict;

use vars qw($CONFIG);
use vars qw($CONFIGNAMEFRONTEND);
use vars qw($CONFIGNAMEBACKEND);
use vars qw($CLOUDSCAPE_HOME);


require 'lib/jboss.pl';
require 'apps/petstore-1.1.2/common.pl';

my $appname = "Petstore-1.1.2";

sub init_app {
    init_ps112_app();
}

sub get_appname {
    return $appname;
}

sub deploy_app {
    deploy_frontend( $CONFIGNAMEFRONTEND );
    deploy_backend( $CONFIGNAMEBACKEND );    

    deploy_db();

    return ($CONFIGNAMEFRONTEND,$CONFIGNAMEBACKEND);
}

sub run_app {

    # start cloudscape
    print ">>> Starting Cloudscape database...\n";
    spawn_background_process( "$CLOUDSCAPE_HOME/bin/cloudscape -start", get_dbmachine() );

    my @frontendmachines = get_frontendmachines();
    my @backendmachines = get_backendmachines();

    start_jboss_cluster(\$CONFIGNAMEBACKEND,\@backendmachines );
    start_jboss_cluster(\$CONFIGNAMEFRONTEND,\@frontendmachines );
}

sub kill_app {

    my @frontendmachines = get_frontendmachines();
    my @backendmachines = get_backendmachines();

    print( "killing app...\n" );
    print( "    configfrontend is: ", $CONFIGNAMEFRONTEND, "\n" );
    print( "    configbackend is: ", $CONFIGNAMEBACKEND, "\n" );
    print( "    frontend machines are: ", @frontendmachines, "\n" );
    print( "    backend machines are: ", @backendmachines, "\n" );

    kill_jboss_cluster(\$CONFIGNAMEFRONTEND,\@frontendmachines );
    kill_jboss_cluster(\$CONFIGNAMEBACKEND,\@backendmachines );
    spawn_process( "killall -9 java", get_dbmachine() );
}

sub app_save_logs($) {
    my $logdir = $_[0];

    my @frontendmachines = get_frontendmachines();
    my @backendmachines = get_backendmachines();

    jboss_save_logs(\$logdir,\$main::CONFIGNAMEFRONTEND,\@frontendmachines );
    jboss_save_logs(\$logdir,\$main::CONFIGNAMEBACKEND,\@backendmachines );
}



sub get_app_fault_trigger_dir() {
    my $PPROOTDIR=getoptordie('pprootdir','root of ROC/PP source tree');
    return $PPROOTDIR . "/expts/faults/petstore-1.1.2-beans/";
}

sub get_app_buggy_src_dirs() {
    return ();
}


return 1; # DO NOT REMOVE THIS LINE!




