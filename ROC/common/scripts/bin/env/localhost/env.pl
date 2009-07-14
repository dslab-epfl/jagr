#!/usr/bin/perl -w

use strict;

#
#  This script provides helper functions for spawning processes and
#  killing processes on the localhost
#


my $envname = "localhost";

sub init_env {
    print "Initializing $envname environment script\n";
}

sub get_envname {
    return $envname;
}

sub spawn_process {
    $| = 1;
    my($cmd, $machine) = @_;

    if( $machine eq 'localhost' ) {
	my $backgroundcmd = $cmd;
	system( $backgroundcmd );
    }
    else {
	print "ERROR: trying to spawn process on non-local machine: $machine";
    }
}

sub spawn_background_process {
    $| = 1;
    my($cmd, $machine) = @_;

    if( $machine eq 'localhost' ) {
	my $backgroundcmd = $cmd . "&";
	system( $backgroundcmd );
    }
    else {
	print "ERROR: trying to spawn process on non-local machine: $machine";
    }
}

return 1;
