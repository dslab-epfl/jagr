#!/usr/bin/perl -w

#
#  This script implements functions for deploying, starting, and stopping 
#  Petstore-1.3.2
#


use strict;

require 'lib/jboss.pl';

my $appname = "Petstore-1.3.2";

my $CONFIGNAME = "notdeployed";

my $PPROOTDIR;
my $JBOSS_HOME;
my $PPAPPDIR;
my $PETSTORE_HOME;
my $CLOUDSCAPE_HOME;
my $CLOUDSCAPE_HOST;


sub init_app {
    print "Initializing $appname application script\n";

    # make sure that environment variables are setup correctly
    check_env_var('JAVA_HOME');

    # check for required application specific configuration flags

    $PPROOTDIR=getoptordie('pprootdir','root of ROC/PP source tree');

    $JBOSS_HOME = getoptordie('jbossdir','JBOSS HOME directory');
    $PPAPPDIR = "$PPROOTDIR/apps";
    $CLOUDSCAPE_HOST = "localhost";
    $PETSTORE_HOME = "$PPAPPDIR/petstore-1.3.2";
    $CLOUDSCAPE_HOME = "$PPAPPDIR/cloudscape";

}

sub get_appname {
    return $appname;
}


sub deploy_app {

    my $BASE = "$JBOSS_HOME/server/default";
    $CONFIGNAME = "petstore-1.3.2-1";
    my $NEW = "$JBOSS_HOME/server/$CONFIGNAME";

    # 1. remove any existing configuration with this name
    system( "rm -rf $NEW" );

    # 2. copy the base configuration
    system( "cp -R $BASE $NEW" );
    
    # 3. copy cloudscape jars
    system( "cp $CLOUDSCAPE_HOME/lib/system/cloudscape.jar $NEW/lib" );
    system( "cp $CLOUDSCAPE_HOME/lib/system/cloudutil.jar $NEW/lib" );
    system( "cp $CLOUDSCAPE_HOME/lib/cloudscape/cloudclient.jar $NEW/lib" );
    system( "cp $CLOUDSCAPE_HOME/lib/cloudscape/RmiJdbc.jar $NEW/lib" );

    # 4. copy configuration modifications
    system( "cat $PETSTORE_HOME/jboss/cloudscape-ds.xml.in | sed 's/\\\$CLOUDSCAPEHOST\\\$/'$CLOUDSCAPE_HOST/ > $NEW/deploy/cloudscape-ds.xml" );

    system( "cp $PETSTORE_HOME/jboss/login-config.xml $NEW/conf" );
    system( "cp $PETSTORE_HOME/jboss/petstoremq-destinations-service.xml $NEW/deploy" );
    system( "cp $PETSTORE_HOME/jboss/mail-service.xml $NEW/deploy" );
    
    # 5. copy petstore deployment files
    system( "cp $PETSTORE_HOME/src/apps/petstore/build/petstore.ear $NEW/deploy" );
    system( "cp $PETSTORE_HOME/src/apps/opc/build/opc.ear $NEW/deploy" );
    system( "cp $PETSTORE_HOME/src/apps/admin/build/petstoreadmin.ear $NEW/deploy" );
    system( "cp $PETSTORE_HOME/src/apps/supplier/build/supplier.ear $NEW/deploy" );

    # 6. copy pkgen.jar library
    system( "cp $PETSTORE_HOME/src/components/pkgen/build/pkgen.jar $NEW/lib" );

    # 7. reset database
    system( "rm -rf $CLOUDSCAPE_HOME/cloudscape/EstoreDB" );
    system( "tar -zxC $CLOUDSCAPE_HOME/cloudscape -f $PETSTORE_HOME/jboss/cloudscape-fresh-EstoreDB-petstore-1.3.1_02.tar.gz" );

    # return the name of the config file
    return ($CONFIGNAME);
}

sub run_app {

    # start cloudscape
    print ">>> Starting Cloudscape database...";
    spawn_background_process( "$CLOUDSCAPE_HOME/bin/cloudscape -start &", get_dbmachine() );

    my @frontendmachines = get_frontendmachines();

    start_jboss_cluster(\$CONFIGNAME,\@frontendmachines);
}

sub kill_app {
    my @frontendmachines = get_frontendmachines();
    kill_jboss_cluster(\$CONFIGNAME,\@frontendmachines);
    spawn_process( "killall -9 java", get_dbmachine() );
}

sub app_save_logs($) {
    my $logdir = $_[0];

    my @frontendmachines = get_frontendmachines();

    jboss_save_logs(\$logdir,\$CONFIGNAME,\@frontendmachines );
}

sub get_app_loadgen_traces() {

    my $rocdir=getoptordie('rocrootdir','root of ROC source tree');    
    my $tracesdir=$rocdir . "/common/scripts/traces/petstore-1.3.2/";


    return ($tracesdir."browse-signin-buy-trace",
	    $tracesdir."browser-trace",
	    $tracesdir."signin-browse-buy-trace",
	    $tracesdir."signin-browse-signout-trace");
}

sub get_app_loadgen_checksum() {
    my $rocdir=getoptordie('rocrootdir','root of ROC source tree');    
    my $tracesdir=$rocdir . "/common/scripts/traces/petstore-1.3.2/";
    return $tracesdir."checksum";
}

sub get_app_fault_trigger_dir() {
    # we can reuse the petstore-1.3.1 directory for 1.3.2
    return $PPROOT . "/expts/faults/petstore-1.3.1/";
}

sub get_app_buggy_src_dirs() {
    # we don't have any buggy source for petstore 1.3.2
    return ();
}

return 1; # DO NOT REMOVE THIS LINE!
