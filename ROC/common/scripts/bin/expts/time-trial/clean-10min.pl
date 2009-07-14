#!/usr/bin/perl -w

use strict;

require 'expts/time-trial/base.pl';

my $exptname = "Clean10Min";

sub init_expt {
    print "Initializing $exptname experiment script\n";
}

sub get_exptname {
    return $exptname;
}

sub run_expt {

    base_run_timed_expt(600,"clean-5min");

}

return 1;
