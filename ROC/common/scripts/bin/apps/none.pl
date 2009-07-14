#!/usr/bin/perl -w

#
#  This script implements functions for deploying, starting, and stopping 
#  Petstore-1.3.1
#

use strict;
use File::List;

require 'lib/jboss.pl';

my $appname = "No-App";

my $CONFIGNAME = "notdeployed";

my $PPROOTDIR;
my $JBOSS_HOME;
my $PPAPPDIR;

sub init_app {
    print "Initializing $appname application script\n";

    # make sure that environment variables are setup correctly
    check_env_var('JAVA_HOME');

    # check for required application specific configuration flags

    $PPROOTDIR=getoptordie('pprootdir','root of ROC/PP source tree');
    $JBOSS_HOME = getoptordie('jbossdir','JBOSS HOME directory');
    $PPAPPDIR = "$PPROOTDIR/apps";
}

sub get_appname {
    return $appname;
}


sub deploy_app {

    my $BASE = "$JBOSS_HOME/server/default";
    $CONFIGNAME = "noapp";
    my $NEW = "$JBOSS_HOME/server/$CONFIGNAME";

    # 1. remove any existing configuration with this name
    system( "rm -rf $NEW" );

    # 2. copy the base configuration
    system( "cp -R $BASE $NEW" );

    # return the name of the config file
    return ($CONFIGNAME);
}

sub run_app {

    my @frontendmachines = get_frontendmachines();

    # petstore-1.3.1 does not separate backend and frontend machines.  
    # so we'll just use one set of machines.
    start_jboss_cluster(\$CONFIGNAME,\@frontendmachines );
}

sub kill_app {

    my @frontendmachines = get_frontendmachines();

    kill_jboss_cluster(\$CONFIGNAME,\@frontendmachines );
}

sub app_save_logs($) {
    my $logdir = $_[0];

    my @frontendmachines = get_frontendmachines();

    jboss_save_logs(\$logdir,\$CONFIGNAME,\@frontendmachines );
}

return 1; # DO NOT REMOVE THIS LINE!


