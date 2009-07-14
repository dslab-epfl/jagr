#!/usr/bin/perl -w

#
#  This script implements functions for deploying, starting, and stopping 
#  RR-Rubis
#

use strict;
use File::List;

require 'lib/jboss.pl';

my $appname = "RR-RUBiS";

my $CONFIGNAME = "notdeployed";

my $PPROOTDIR;
my $JBOSS_HOME;
my $PPAPPDIR;
my $RRRUBISDIR;
my $MYSQLDIR;
my $SSMDIR;
my $ROCROOTDIR;

sub init_app {
    print "Initializing $appname application script\n";

    # make sure that environment variables are setup correctly
    check_env_var('JAVA_HOME');

    # check for required application specific configuration flags

    $PPROOTDIR=getoptordie('pprootdir','root of ROC/PP source tree');
    $JBOSS_HOME = getoptordie('jbossdir','JBOSS HOME directory');
    $PPAPPDIR = "$PPROOTDIR/apps";
    $RRRUBISDIR = getoptordie('rubisdir','RR-RUBiS directory');
    $ROCROOTDIR = getoptordie('rocrootdir','RR-RUBiS directory');
    
    my $dontstartmysql = getopt('nomysql');
    if( ! $dontstartmysql ) {
	$MYSQLDIR = getoptordie('mysqldir','MySQL directory');
    }

    my $dontstartssm = getopt('nossm');
    if( ! $dontstartssm ) {
	$SSMDIR = getoptordie('ssmdir','SSM directory');
    }
}

sub get_appname {
    return $appname;
}


sub deploy_app {

    my $BASE = "$JBOSS_HOME/server/default";
    $CONFIGNAME = "rr-rubis";
    my $NEW = "$JBOSS_HOME/server/$CONFIGNAME";

    # 1. remove any existing configuration with this name
    system( "rm -rf $NEW" );

    # 2. copy the base configuration
    system( "cp -R $BASE $NEW" );

    # return the name of the config file
    return ($CONFIGNAME);
}

sub run_app {

    my $dontstartmysql = getopt('nomysql');
    if( ! $dontstartmysql ) {
	local $CWD=$MYSQLDIR;
#	spawn_background_process("cd $CWD; ./bin/safe_mysqld --datadir=$CWD/data --socket=$CWD/data/mysql/mysql.sock --err-log=$CWD/data/mysql/mysql.log --pid-file=$CWD/data/mysql/mysqld.pid",get_dbmachine() );
	spawn_background_process("cd $CWD; ./bin/safe_mysqld",get_dbmachine() );
    }

    ## start SSM
    my $dontstartssm = getopt('nossm');
    if( ! $dontstartssm ) {
	local $CWD=$SSMDIR;
	my @ssmmachines=split(' ',get_namedmachine("ssm"));
	my $count=1;
	foreach (@ssmmachines) {
	    print( "starting SSM brick $count on $_ \n" );
	    spawn_background_process("cd $CWD; cd bin; ./brick.sh $count", $_);
	    $count=$count+1;
	}
    }


    my @frontendmachines = get_frontendmachines();

    start_jboss_cluster(\$CONFIGNAME,\@frontendmachines );
}

sub kill_app {

    my @frontendmachines = get_frontendmachines();

    kill_jboss_cluster(\$CONFIGNAME,\@frontendmachines );

    my $dontstartmysql = getopt('nomysql');
    if( ! $dontstartmysql ) {
	local $CWD=$MYSQLDIR;
#	spawn_process("cd $CWD; ./bin/mysqladmin -S /var/lib/mysql/mysql.sock -u root shutdown", get_dbmachine() );
	spawn_process("cd $CWD; ./bin/mysqladmin shutdown -u root", get_dbmachine() );
    }

    ## kill SSM
    my $dontstartssm = getopt('nossm');
    if( ! $dontstartssm ) {
	local $CWD=$SSMDIR;
	my @ssmmachines=split(' ',get_namedmachine("ssm"));
	my $count=1;
	foreach (@ssmmachines) {
	    print( "killing SSM brick $count on $_ \n" );
	    spawn_process("killall java", $_);
	    $count=$count+1;
	}
    }


}

sub app_save_logs($) {
    my $logdir = $_[0];

    my @frontendmachines = get_frontendmachines();

    jboss_save_logs(\$logdir,\$CONFIGNAME,\@frontendmachines );

    # TODO: save rubis's Client logs
}

sub OLD_app_run_loadgen($$$) {
    
    my $logdir=$_[0];
    my $loadconfig=$_[1];     # ignored
    my $loadmagnitude=$_[2];  # ignored

    my @frontendmachines=get_frontendmachines();
    my $hostnamelist=convert_array_to_csv(@frontendmachines);
    
    my @loadgenmachines=get_clientmachines();

    my $abslogdir=$CWD . "/" . $logdir;


    foreach( @loadgenmachines ) {

	my $lm = $_;

	## TODO: this doesn't correctly handle when hostnamelist contains more than one host...
#	spawn_process( "cat $RRRUBISDIR/Client/config_files/rubis.properties.in | sed 's#\\\$RUBISDIR#'$RRRUBISDIR# | sed 's/\\\$TARGETHOST/'$hostnamelist/ > $RRRUBISDIR/Client/config_files/rubis.properties.$lm",$lm );

	my $stdout=$abslogdir . "/stdout." . $lm;
	my $stderr=$abslogdir . "/stderr." . $lm;
	{
	    local $CWD=$RRRUBISDIR . "/Client";
	    spawn_background_process( "./runclient config_files/rubis.properties.$lm", $lm  );
	}
    }
 
}

sub app_run_loadgen($$$) {
    
    my $logdir=$_[0];
    my $loadconfig=$_[1];     # ignored
    my $loadmagnitude=$_[2];  # ignored

    my @frontendmachines=get_frontendmachines();
    my $hostnamelist=convert_array_to_csv(@frontendmachines);
    
    my @loadgenmachines=get_clientmachines();

    my $abslogdir=$CWD . "/" . $logdir;


    foreach( @loadgenmachines ) {

	my $lm = $_;

	my $stdout=$abslogdir . "/stdout." . $lm;
	my $stderr=$abslogdir . "/stderr." . $lm;
	{
	    local $CWD=$ROCROOTDIR . "/jagr/loadgen/bin";
	    spawn_background_process( "./rubis-loadgen.sh", $lm  );
	}
    }
 
}

## TODO: sub app_kill_loadgen

sub get_app_fault_trigger_dir() {
    return getoptordie('rocrootdir','root of ROC source tree') . "/jagr/jagr-rubis/faultloads";

}

return 1; # DO NOT REMOVE THIS LINE!


