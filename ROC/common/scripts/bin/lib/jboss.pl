#!/bin/perl -w

use strict;

require 'lib/util.pl';

my @jboss_env;
my @java_opt;

sub clear_jboss_env() {
    @jboss_env=();
}

sub add_jboss_env(\$) {
    @jboss_env=(@jboss_env,$_);
}

sub get_jboss_env_export() {
    my $export = "";
    foreach( @jboss_env ) {
	$export = $export . "export " . $_ . "; ";
    }
    return $export;
}

sub clear_java_opt() {
    @java_opt=();
}

sub add_java_opt($) {
    my $j=$_[0];
    @java_opt=(@java_opt,$j);
}

sub get_java_opt_export() {
    my $export = "export JAVA_OPTS=\"";
    $export = $export . "-server -Xmx640m -Xss2m";
    foreach( @java_opt ) {
	$export = $export . " " . $_;
    }
    $export = $export ."\";";
    return $export;
}

# start up jboss using the given config directory
sub start_jboss_cluster (\$\@){

    my $configref=$_[0];
    my $machinesref=$_[1];
    
    my $config=$$configref;
    my @machines=@$machinesref;

    my $jbosshome= getoptordie('jbossdir','JBOSS HOME directory');

    print( "Starting jboss ($config)... on machines ",  @machines, "\n");
    
    foreach (@machines) {
	# copy $config to $config-[machinename]
	my $configcopy= $config . $_;
	system( "rm -rf $jbosshome/server/$configcopy" );
	system( "cp -r $jbosshome/server/$config $jbosshome/server/$configcopy" );
	
	my $JAVA_OPTS=get_java_opt_export();
	my $jboss_export = get_jboss_env_export();

	spawn_background_process( $jboss_export . $JAVA_OPTS . "$jbosshome/bin/run.sh --config=$configcopy 1>/tmp/jboss.stdout 2>/tmp/jboss.stderr", $_ );
	
	# or, to reduce output: -- BUT, THIS IS BROKEN BECAUSE THERE'S NO SPAWN!!
	# exec_and_match("$jbosshome/bin/run.sh --config=$configcopy", "Started in");
    }
    
}

sub kill_jboss_cluster(\$\@) {

    my $configref=$_[0];
    my $machinesref=$_[1];
    
    my $config=$$configref;
    my @machines=@$machinesref;
    
    print( "killing jboss ...\n" );
    print( "    config is: ", $config, "\n" );
    print( "    machines are: ", @machines, "\n" );
    
    
    foreach (@machines) {
	# my $configcopy= $config . $_;
	spawn_process( "killall -9 java", $_ );
    }
    
}

sub jboss_save_logs(\$\$\@) {

    my $logdestref=$_[0];
    my $configref=$_[1];
    my $machinesref=$_[2];
    
    my $logdest=$$logdestref;
    my $config=$$configref;
    my @machines=@$machinesref;

    my $jbosshome=getoptordie('jbossdir','JBOSS HOME directory');

    foreach (@machines) {
	my $configcopy = $config . $_;
	my $jbosslogdir = $logdest . "/" . $configcopy;
	ensure_dir_exists($jbosslogdir);
	system( "cp $jbosshome/server/" . $configcopy . "/log/* $jbosslogdir/" );
    }
    
}

return 1;
