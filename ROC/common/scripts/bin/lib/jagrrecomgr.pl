#!/bin/perl -w

###############################################
### JAGR Recovery Manager-Related Functions ###
###############################################

use File::chdir;
use strict;

sub run_recomgr($) {
    my $logdir = $_[0];

    ensure_dir_exists($logdir);

    my($JAGRROOTDIR) = getoptordie('jagrrootdir','Root of ROC/JAGR source tree');
    # todo: add a jagr conf file option, if our jagr recomgr ever gets a config file parser

    my($RECOMGR_HOME) = "$JAGRROOTDIR/jagr-recomgr";

    my($RECOMGR_MACHINE) = get_namedmachine('recomgr');

    print( "running jagr recovery manager on machine: ", $RECOMGR_MACHINE, "\n" );

    
    my $stdout=$logdir . "/stdout";
    my $stderr=$logdir . "/stderr";

    spawn_background_process("$RECOMGR_HOME/bin/run-recomgr.sh", $RECOMGR_MACHINE );
}

sub kill_recomgr {
    my $RECOMGR_MACHINE = get_namedmachine('recomgr');
    print( "killing jagr recomgr on machine: ", $RECOMGR_MACHINE, "\n" );
    spawn_process( "killall -9 java", $RECOMGR_MACHINE );
}

return 1;
