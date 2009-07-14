#!/usr/bin/perl -w

use strict;
use File::List;

sub get_simple_fault_types() {
    return ("error","exception");
}

sub get_extra_fault_types() {
    return ("deadlock","nullmap","unbind","infinite-loop");
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

	my $searchcrit = "faultload-.*-" . $_ . ".xml\$";

	print( "\tsearchcrit=" . $searchcrit . "\n" );

	my @morefiles = @{ $search->find($searchcrit) };
	
	print( "\tresults=", @morefiles, "\n" );

	@files = (@files,@morefiles);
    }

    print( "getmatchingtriggers: results=", @files, "\n" );

    return @files;
}

sub start_rrfaultcampaign(\$\$) {

    my $targetmachine=$_[0];
    my $faultload=$_[1];

    my $expttoolsdir = getoptordie('rocrootdir','ROC root directory') . "/jagr/expttools/bin";
    spawn_process( $expttoolsdir . "/inject-fault.sh $targetmachine $faultload",$targetmachine );
}

return 1;
