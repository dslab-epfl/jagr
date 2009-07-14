#!/usr/bin/perl -w

use strict;

require 'expts/time-trial/base.pl';
require 'lib/faulttriggerhelper.pl';

my $exptname = "AllSrcBug";

sub init_expt {
    print "Initializing $exptname experiment script\n";
}

sub get_exptname {
    return $exptname;
}

my $currbuggysrcdir;

sub run_expt {

    print( "all-srcbug-faults RUN_EXPT\n" );

    my @srcbuggydirs=get_app_buggy_src_dirs();
    my $appdir=get_app_root_buggy_src_dir();

    foreach(@srcbuggydirs) {
	print( "Running current source buggy ver: " . $_ . "\n" );
	$currbuggysrcdir=$_;
	$_ = $currbuggysrcdir;
	s/$appdir/app/;
	s#/#_#g;
	my $currname = $_;
	print( "\tcurrname is :" . $currname . "\n" );
	clear_jboss_env();
	clear_java_opt();
	base_run_timed_expt(300,$currname);
    }
}

sub expt_post_deploy($) {

    my $config=$_[0];

    print( "deploying buggy src dir : " . $config . "\n" );

    deploy_buggy_src_dir($config,$currbuggysrcdir);
}

return 1;
