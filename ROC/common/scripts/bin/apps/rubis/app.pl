#!/usr/bin/perl -w

#
#  This script implements functions for deploying, starting, and stopping 
#  Petstore-1.3.1
#

use strict;
use File::List;
use File::chdir;

require 'lib/jboss.pl';

my $appname = "RUBiS";

my $CONFIGNAME = "notdeployed";

my $ROCROOTDIR;
my $J2EEDIR;
my $APPDIR;
my $MYSQL_HOST;
my $RUBIS_HOME;
my $JBOSS_HOME;
my $PPROOTDIR;
my $PPAPPDIR;

sub init_app {
    print "Initializing $appname application script\n";

    # make sure that environment variables are setup correctly
     check_env_var('JAVA_HOME');

    # check for required application specific configuration flags

    $ROCROOTDIR=getoptordie('rocrootdir','root of ROC/ source tree');
    $J2EEDIR=getoptordie('j2eedir','Sun\'s reference J2EE SDK directory');
    $PPROOTDIR=getoptordie('pprootdir','root of PP source tree');
    $PPAPPDIR=$PPROOTDIR."/apps";

    $JBOSS_HOME = getoptordie('jbossdir','JBOSS HOME directory');
    $APPDIR = "$ROCROOTDIR/common/scripts/bin/apps";
    $MYSQL_HOST = get_dbmachine();
    $RUBIS_HOME=$APPDIR . "/rubis";
}

sub get_appname {
    return $appname;
}


sub deploy_app {

    my $BASE = "$JBOSS_HOME/server/default";
    $CONFIGNAME = "rubis-1";
    my $NEW = "$JBOSS_HOME/server/$CONFIGNAME";

    # 1. remove any existing configuration with this name
    system( "rm -rf $NEW" );

    # 2. copy the base configuration
    system( "cp -R $BASE $NEW" );
    
    # 3. copy configuration modifications
    system( "cp $RUBIS_HOME/server/default/conf/login-config.xml $NEW/conf" );
    system( "cp $RUBIS_HOME/server/default/conf/standardjaws.xml $NEW/conf" );
    system( "cp $RUBIS_HOME/server/default/conf/standardjbosscmp-jdbc.xml $NEW/conf" );
    system( "cat $RUBIS_HOME/server/default/deploy/mysql-ds.xml.in | sed 's#\\\$MYSQLHOST\\\$#'$MYSQL_HOST# > $NEW/deploy/mysql-ds.xml" );
    system( "cp $RUBIS_HOME/server/default/lib/mysql-connector-java-3.0.8-stable-bin.jar $NEW/lib" );

    # 4. modify RUBiS Config.java and rebuild

    system( "cat $RUBIS_HOME/RUBiS/EJB_EntityBean_id_BMP/edu/rice/rubis/beans/servlets/ConfigJBoss.java.in | sed 's#\\\$ROC_TOP#'$ROCROOTDIR# > $RUBIS_HOME/RUBiS/EJB_EntityBean_id_BMP/edu/rice/rubis/beans/servlets/ConfigJBoss.java" );
    system( "cat $RUBIS_HOME/RUBiS/build.properties.in | sed 's#\\\$J2EE_DIR#'$J2EEDIR# > $RUBIS_HOME/RUBiS/build.properties" );
    {
	local $CWD="$RUBIS_HOME/RUBiS/";
	system( "./build-script.sh $JBOSS_HOME" );
    }

    # 5. deploy rubis application files
    system( "cp $RUBIS_HOME/RUBiS/EJB_EntityBean_id_BMP/rubis.ear $NEW/deploy" );
    system( "cp $RUBIS_HOME/RUBiS/EJB_EntityBean_id_BMP/ejb_rubis_web.war $NEW/deploy" );

    # 6. reset database
    system( "rm -rf $RUBIS_HOME/mysql/mysql-max-3.23.58-pc-linux-i686" );
    system( "tar -zxC $RUBIS_HOME/mysql -f $RUBIS_HOME/mysql-max-rubis-3.23.58.tgz" );

    # return the name of the config file
    return ($CONFIGNAME);
}

sub run_app {

    # start cloudscape
    print ">>> Starting MySQL database...";
    {
	local $CWD="$RUBIS_HOME/mysql/mysql-max-3.23.58-pc-linux-i686/";
	spawn_background_process( "./bin/safe_mysqld", get_dbmachine() );
    }

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

sub get_app_loadgen_checksum() {
    my $rocdir=getoptordir('rocrootdir','root of ROC source tree');
    my $tracesdir=$rocdir . "/common/scripts/traces/rubis/";
    return $tracesdir."checksum";
}

sub get_app_loadgen_traces() {

    my $rocdir=getoptordir('rocrootdir','root of ROC source tree');
    my $tracesdir=$rocdir . "/common/scripts/traces/rubis/";


    return ();

### TODO
    return ($tracesdir."browse-signin-buy-trace",
	    $tracesdir."browser-trace",
	    $tracesdir."signin-browse-buy-trace",
	    $tracesdir."signin-browse-signout-trace");
}

sub get_app_fault_trigger_dir() {
    return $PPROOTDIR . "/expts/faults/rubis-beans/";
}

sub get_app_root_buggy_src_dir() {
    return $PPAPPDIR . "/RUBiS-buggysrc/deployment-files/";
}

sub get_app_buggy_src_dirs() {
    my $BUGGY_ROOT_DIR=$PPAPPDIR . "/RUBiS-buggysrc/deployment-files/";
    
    my $search = new File::List( $BUGGY_ROOT_DIR );

    $search->show_empty_dirs();  # toggle include empty directories in output
    $search->show_only_dirs();
    my @files  = @{ $search->find("ver.*\$") };  

    return @files;
}

return 1; # DO NOT REMOVE THIS LINE!
