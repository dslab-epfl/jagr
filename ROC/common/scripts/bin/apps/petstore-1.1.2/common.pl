#!/usr/bin/perl -w

#
#  This script implements functions for deploying, start, and stopping
#  Petstore-1.1.2
#

use strict;

use vars qw($CONFIG);
use vars qw($CONFIGNAME);
use vars qw($CONFIGNAMEFRONTEND);
use vars qw($CONFIGNAMEBACKEND);
use vars qw($CLOUDSCAPE_HOME);

require 'lib/jboss.pl';

$CONFIGNAME = "petstore-1.1.2";
$CONFIGNAMEBACKEND = $CONFIGNAME . "-backend";
$CONFIGNAMEFRONTEND = $CONFIGNAME . "-frontend";

my $PPROOTDIR;
my $JBOSS_HOME;
my $PPAPPDIR;
my $CLOUDSCAPE_HOST;
my $PETSTORE112_HOME;

sub init_ps112_app {
    # make sure that environment variables are setup correctly
    check_env_var('JAVA_HOME');

    # check for required application specific configuration flags

    $PPROOTDIR=getoptordie('pprootdir','root of ROC/PP source tree');

    $JBOSS_HOME = getoptordie('jbossdir','JBOSS HOME directory');

    $PPAPPDIR = "$PPROOTDIR/apps";
    $CLOUDSCAPE_HOST = get_dbmachine();
    $PETSTORE112_HOME = "$PPAPPDIR/petstore-1.1.2";
    $CLOUDSCAPE_HOME = "$PPAPPDIR/cloudscape";
}


sub deploy_db {
    system( "rm -rf $CLOUDSCAPE_HOME/cloudscape/EstoreDB" );
    system( "tar -zxC $CLOUDSCAPE_HOME/cloudscape -f $PETSTORE112_HOME/deploy/cloudscape-petstore-1.1.2.tar.gz" );
}

sub deploy_backend {

    my $config = $_[0];

    deploy_commonend($config);

    system( "cp $PETSTORE112_HOME/deploy/petstore.ear $JBOSS_HOME/server/$config/deploy" );


}

sub deploy_frontend {

    my $config = $_[0];

    deploy_commonend($config);

    system( "cp $PETSTORE112_HOME/deploy/petstore-web.ear $JBOSS_HOME/server/$config/deploy" );


}

sub deploy_commonend {

    my($config) = $_[0];

#    my $BASE = "$JBOSS_HOME/server/all";
    my $BASE = "$JBOSS_HOME/server/default";
    my $NEW = "$JBOSS_HOME/server/$config";

    my $DB_HOST = get_dbmachine();

    # 1. remove any existing configuration with this name
    system( "rm -rf $NEW" );

    # 2. copy the base configuration
    system( "cp -R $BASE $NEW" );

    # 3. copy cloudscape jars
    system( "cp $CLOUDSCAPE_HOME/lib/system/cloudscape.jar $NEW/lib");
    system( "cp $CLOUDSCAPE_HOME/lib/system/cloudutil.jar $NEW/lib" );
    system( "cp $CLOUDSCAPE_HOME/lib/cloudscape/cloudclient.jar $NEW/lib" );
    system( "cp $CLOUDSCAPE_HOME/lib/cloudscape/RmiJdbc.jar $NEW/lib" );

    # 4. copy configuration modifications
    system( "cp $PETSTORE112_HOME/deploy/login-config.xml-jboss-3.2.1 $NEW/conf/login-config.xml" );
    system( "cp $PETSTORE112_HOME/deploy/jboss-minimal.xml-jboss-3.2.1 $NEW/conf/jboss-minimal.xml" );
    system( "cp $PETSTORE112_HOME/deploy/jboss-service.xml-jboss-3.2.1 $NEW/conf/jboss-service.xml" );
    system( "cat $PETSTORE112_HOME/deploy/cloudscape-ds.xml.in | sed 's/" . 
        '\\$CLOUDSCAPEHOST\\$' . "/'" . $DB_HOST . "/ > $NEW/deploy/cloudscape-ds.xml" );

    # 5. copy petstore deployment files
    system( "cp $PETSTORE112_HOME/deploy/emk-util.jar $NEW/lib" );
}

sub get_app_loadgen_checksum() {
    my $rocdir=getoptordie('rocrootdir','root of ROC source tree');    
    my $tracesdir=$rocdir . "/common/scripts/traces/petstore-1.1.2/";
    return $tracesdir."checksum";
}

sub get_app_loadgen_traces() {

    my $rocdir=getoptordie('rocrootdir','root of ROC source tree');    
    my $tracesdir=$rocdir . "/common/scripts/traces/petstore-1.1.2/";


    return ($tracesdir."browse-signin-buy-trace",
	    $tracesdir."browser-trace",
	    $tracesdir."signin-browse-buy-trace",
	    $tracesdir."signin-browse-signout-trace");
}

return 1; # DO NOT REMOVE THIS LINE!




