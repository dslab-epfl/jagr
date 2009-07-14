#!/usr/bin/perl -w

#
# This script provides helper functions for running un
#
#

use strict;

require 'lib/pinpoint.pl';

my $exptname = "Kill";

sub init_expt {
    print "Initializing $exptname experiment script\n";
}

sub get_exptname {
    return $exptname;
}

sub run_expt {

    # kill old instances, and deploy app

    print "Cleaning up lingering experiments...\n";

    kill_app();
}

return 1;
