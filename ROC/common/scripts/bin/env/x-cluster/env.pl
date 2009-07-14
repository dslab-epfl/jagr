#!/usr/bin/perl -w

use strict;

#
#  This script provides helper functions for spawning processes and
#  killing processes on the x-cluster
#


my $envname = "x-cluster";

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
	system( $cmd );
    }
    else {
	system( "export GEXEC_SVRS=$machine; gexec -v -n 1 /work/emrek/ROC/common/scripts/bin/lib/run.sh \'" . $cmd . "\'" ); 
    }
}

sub spawn_background_process {
    $| = 1;
    my($cmd, $machine) = @_;

    if( $machine eq 'localhost' ) {
	system( $cmd );
    }
    else {
	system( "export GEXEC_SVRS=$machine; gexec -v -n 1 /work/emrek/ROC/common/scripts/bin/lib/run.sh \'" . $cmd . "\' &" ); 
    }
}

return 1;
