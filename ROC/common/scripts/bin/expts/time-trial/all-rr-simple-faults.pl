#!/usr/bin/perl -w

use strict;

require 'expts/time-trial/base.pl';
require 'lib/rrfaulthelper.pl';

my $exptname = "AllRRSimple";

my $currentfaultfile;

sub init_expt {
    print "Initializing $exptname experiment script\n";
}

sub get_exptname {
    return $exptname;
}

sub run_expt {

    print( "all-rr-simple-faults RUN_EXPT\n" );

    my $faulttriggerdir=get_app_fault_trigger_dir();
    my @simplefaults=get_simple_fault_types();

#    print( "SIMPLEFAULTS: " , @simplefaults , "\n" );

    my @faulttriggers = get_matching_triggers(\$faulttriggerdir,
					      \@simplefaults);

    # @faulttriggers = filter_faults_already_run(@faulttriggers);

    foreach(@faulttriggers) {
	clear_jboss_env();
	clear_java_opt();
#	add_java_opt( "-Droc.pinpoint.injection.FaultTriggerFile=" . $_ );

	$currentfaultfile=$_;

	s/$faulttriggerdir//;
	s#/#_#g;
	my $currname = $_;
	print( "\tcurrname is :" . $currname . "\n" );

	print( "JAVA EXPORT: " , get_java_opt_export() . "\n" );
	base_run_timed_expt(300,$currname);
    }
}

sub expt_setup_faults() {
    my @backends = get_backendmachines();
    foreach (@backends) {
	start_rrfaultcampaign($_,$currentfaultfile);
    }

    my @frontends = get_frontendmachines();
    foreach (@frontends) {
	start_rrfaultcampaign($_,$currentfaultfile);
    }
}

return 1;



