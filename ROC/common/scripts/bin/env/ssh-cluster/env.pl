#!/usr/bin/perl -w

use File::chdir;
use strict;

#
#  This script provides helper functions for spawning processes and
#  killing processes on the x-cluster
#


my $envname = "ssh-cluster";

sub init_env {
    print "Initializing $envname environment script\n";
}

sub get_envname {
    return $envname;
}

sub spawn_process {
    $| = 1;
    my($cmd, $machine) = @_;

    system( "ssh -A $machine \'cd $CWD;" . $cmd . "\'" ); 
    
}

sub spawn_background_process {
    $| = 1;
    my($cmd, $machine) = @_;

    system( "ssh -f -A $machine \'cd $CWD;" . $cmd . "\' &" ); 
    
}

return 1;
