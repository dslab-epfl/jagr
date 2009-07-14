#!/usr/bin/perl -w

#
#
#
#

use strict;
use File::List;

sub get_simple_fault_types() {
    return ("runtime_exception","expected_exception","null_call");
}

sub get_extra_fault_types() {
    return ("infinite_loop","halt");
}

sub get_performance_fault_types() {
    return ( "constantPerformance","cumulativePerformance",
	     "gcPerformance","stutterPerformance" );
}

sub get_matching_triggers(\$\@) {

    my $faulttriggerdirref=$_[0];
    my $faulttriggerdir=$$faulttriggerdirref;
    my $faulttypesref = $_[1];
    my @faulttypes = @$faulttypesref;

    print("getmatchingtriggers: faulttriggerdir=" . $faulttriggerdir . "\n" );

    my $search = new File::List( $faulttriggerdir);
    $search->show_empty_dirs();
    
    my @files = ();

    foreach( @faulttypes ) {
	print("getmatchingtriggers_loop\n" );

	my $searchcrit = "faultconfig-name_.*" . $_ . "\$";

#	print( "\tsearchcrit=" . $searchcrit . "\n" );

	my @morefiles = @{ $search->find($searchcrit) };
	
#	print( "\tresults=", @morefiles, "\n" );

	@files = (@files,@morefiles);
    }

    print( "getmatchingtriggers: results=", @files, "\n" );

    return @files;
}

#
# this function searches through the sets of logs in the logbasedir
# for other experiments with the same --tag as the current experiment
# For each matching experiment it finds, it filters out its already
# completed fault runs and removes them from this list
#
sub filter_faults_already_run(@) {

    my $tag = getopt('tag');
    if( !defined($tag)) {
	return @_;
    }

    my $baselogdir= get_baselogdir();

    ## MAJOR TODO

    return @_;
}


sub deploy_buggy_src_dir($$) {
    my $targetconfigdir=$_[0];
    my $buggysrcdir=$_[1];

    print( "faulttriggerhelper: injecting source code bug: ", $buggysrcdir );
    system( "cp $buggysrcdir/* $targetconfigdir/deploy/" );
}


return 1;
