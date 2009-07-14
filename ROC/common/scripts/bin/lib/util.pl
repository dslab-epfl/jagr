#!/bin/perl -w

use strict;


sub ensure_dir_exists($) {
    my $logdir=$_[0];
    if(!(-d $logdir)) {
	mkdir( $logdir, 0776 ) || die "cannot create directory $logdir\n";
    }
}

sub convert_array_to_csv {
    my $s = "";
    my $i = 0;

    for ($i = 0; $i < @_; $i++) {
	$s = $s . $_[$i];
	$s = $s . "," if (($i+1) < @_);
    }

    return $s;
}

return 1;
