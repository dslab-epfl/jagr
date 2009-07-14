#!/bin/perl -w

####################################
#### Pinpoint-related Functions ####
####################################

use File::chdir;
use strict;


sub run_pinpoint($) {
    my $logdir = $_[0];

    ensure_dir_exists($logdir);

    my($PPROOTDIR) = getoptordie('pprootdir','Root of ROC/PP source tree');
    my($ROCROOTDIR) = getoptordie('rocrootdir','Root of ROC source tree');
    my($PPCONF) = getopt('ppconf','config filename to pass to pinpoint');
    if( !defined($PPCONF)) {
	$PPCONF = "../conf/main/save-observations-to-disk.conf";
    }
    else {
#	$PPCONF = $CWD . "/" . $PPCONF;
        $PPCONF = $PPCONF;
        # HACK 
        $PPCONF = $PPCONF . " historicaldir=$ROCROOTDIR/jagr/jagr-rubis/good-profile/hgcb/";
    }

    my($PINPOINT_HOME) = "$PPROOTDIR/pinpoint";

    my($PINPOINT_MACHINE) = get_namedmachine('pinpoint');

    print( "running pinpoint on machine: ", $PINPOINT_MACHINE, "\n" );

    my $obsfile= $CWD . "/" . $logdir . "/pinpoint.observations";
    my $stdout=$logdir . "/stdout";
    my $stderr=$logdir . "/stderr";

    spawn_background_process("cd $CWD; mkdir -p $logdir; $PINPOINT_HOME/bin/run-pinpoint.sh " . $PPCONF 
			     . " output=$obsfile 1>$stdout 2>$stderr",
			     $PINPOINT_MACHINE);
}

sub kill_pinpoint {

    my $tmp="pinpoint";
    my($PINPOINT_MACHINE) = get_namedmachine($tmp);

    print( "killing pinpoint on machine: ", $PINPOINT_MACHINE, "\n" );

    spawn_process("killall -9 java", $PINPOINT_MACHINE);
}


sub init_pinpoint {

    add_java_opt("-Droc.pinpoint.tracing.Publisher=roc.pinpoint.tracing.java.TCPFastObservationPublisher");

    my $obs_host=get_namedmachine('pinpoint');
    add_java_opt("-Droc.pinpoint.publishto.hostname=$obs_host");
    
    add_java_opt("-Droc.pinpoint.publishto.maxqueue=100000");
}

# copy pinpoint files to new JBoss configuration
sub deploy_pinpoint {

    my($config) = @_;
    
    print( "deploying pinpoint to ", $config, "\n" );

    my($PPROOTDIR) = getoptordie('pprootdir','Root of ROC/PP source tree');
    my($JBOSSDIR) = getoptordie('jbossdir','Root of JBoss tree');
    my($PINPOINT_HOME) = "$PPROOTDIR/pinpoint";
    
    system("cp $PINPOINT_HOME/dist/lib/pinpoint.jar $config/lib");
#    system("cp $PINPOINT_HOME/dist/lib/pinpoint.jar $JBOSSDIR/lib");
    system("cp $PINPOINT_HOME/lib/swigutil.jar $config/lib");
    system("cp $PINPOINT_HOME/lib/xercesImpl.jar $config/lib");
    system("cp $PINPOINT_HOME/lib/xmlParserAPIs.jar $config/lib");

    my $pinpointdontcopyconfig=getopt('pinpointdontcopyconfig');
    if( ! $pinpointdontcopyconfig ) {
	system("cp $PINPOINT_HOME/src/jboss-ext/conf/standardjboss.xml-jboss-3.2.1 $config/conf/standardjboss.xml" );
    }

}

return 1;
