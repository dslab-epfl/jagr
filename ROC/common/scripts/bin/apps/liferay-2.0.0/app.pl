#!/usr/bin/perl -w

#
#  This script implements functions for deploying, starting, and stopping 
#  Liferay 2.0
#

use strict;

require 'lib/jboss.pl';

$appname = "Liferay-2.0";

$CONFIGNAME = "notdeployed";

sub init_app {
    print "Initializing $appname application script\n";

    # make sure that environment variables are setup correctly
    check_env_var('JAVA_HOME');

    # check for required application specific configuration flags

    $PPROOTDIR=getoptordie('pprootdir','root of ROC/PP source tree');

    $JBOSS_HOME = getoptordie('jbossdir','JBOSS HOME directory');
    $PPAPPDIR = "$PPROOTDIR/apps";
    $POSTGRES_HOST = get_dbmachine();
    $LIFERAY_HOME = "$PPAPPDIR/liferay-2.0";
    $POSTGRES_HOME = "$PPAPPDIR/postgresql";
}

sub get_appname {
    return $appname;
}


sub deploy_app {

    my $BASE = "$JBOSS_HOME/server/default";
    $CONFIGNAME = "liferay-2.0-1";
    my $NEW = "$JBOSS_HOME/server/$CONFIGNAME";

    # 1. remove any existing configuration with this name
    system( "rm -rf $NEW" );

    # 2. copy the base configuration
    system( "cp -R $BASE $NEW" );
    
    # 3. copy postgresql and other third-party support libraries
    system( "cp $LIFERAY_HOME/server/default/lib/*.jar $NEW/lib" );

    # 4. copy configuration modifications
    system( "cp $LIFERAY_HOME/server/default/conf/login-config.xml $NEW/conf" );
    # 5. copy liferay deployment files

    system( "cat $LIFERAY_HOME/server/default/deploy/postgres-ds.xml.in | sed 's/\\\$POSTGRESHOST\\\$/'$POSTGRES_HOST/ > $NEW/deploy/postgres-ds.xml" );

    system( "cp $LIFERAY_HOME/server/default/deploy/liferay-ep-2.0.0-rc2.ear $NEW/deploy" );
    system( "cp $LIFERAY_HOME/server/default/deploy/mail-service.xml $NEW/deploy" );
    system( "cp $LIFERAY_HOME/server/default/deploy/liferay-jms-service.xml $NEW/deploy" );

    # 7. reset database
    system( "rm -rf $LIFERAY_HOME/db" );
    system( "tar -zxC $LIFERAY_HOME -f $LIFERAY_HOME/liferay-db.tar.gz" );

    # return the name of the config file
    return ($CONFIGNAME);
}

sub run_app {

    print ">>> Starting PostgreSQL database...\n";
    spawn_background_process( "pg_ctl start -D $LIFERAY_HOME/db &", get_dbmachine() );

    my @frontendmachines = get_frontendmachines();

    start_jboss_cluster(\$CONFIGNAME,\@frontendmachines);
}

sub kill_app {
    my @frontendmachines = get_frontendmachines();
    kill_jboss_cluster(\$CONFIGNAME,\@frontendmachines);
    spawn_process( "pg_ctl stop -D $LIFERAY_HOME/db", get_dbmachine() );
}

sub app_save_logs($) {
    my $logdir = $_[0];

    my @frontendmachines = get_frontendmachines();
    jboss_save_logs(\$logdir,\$CONFIGNAMEFRONTEND,\@frontendmachines );
}

return 1; # DO NOT REMOVE THIS LINE!
