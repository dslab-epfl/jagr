#!/usr/bin/perl -w

use strict;

sub init_logdir();

###############
#### Setup ####
###############

$| = 1;                            # make i/o unbuffered
$SIG{'INT'} = 'my_sigint_catcher'; # special-case signal handling
my($execing) = 0;

no strict 'refs'; # so we can use vars as function pointers

# for getting the current time
use POSIX qw(strftime);

# to parse command line arguments
use Getopt::Long;
# turn on pass-through (unrecognized arguments go in @ARGV)
Getopt::Long::Configure ("pass_through");

###############################
#### General Infrastucture ####
###############################

# parse special "blessed" command line args and store in a hash
my %blessedopts = ();
GetOptions (\%blessedopts,
            'interactive', # prompt for environment vars if not defined
            'app=s',       # filename of application.pl to run
	    'env=s',       # filename of env.pl to run
	    'config=s',    # read options from config file?
            'tag=s'        # put this string in the ID file in logs directory
           );

# if a config file is specified, read in the options
my %configopts = ();
my $config = $blessedopts{'config'};
if (defined($config)) {
  %configopts = read_config_file($config);
}

print_opts(%configopts);

# set a global interactive flag (used in check_env_var)
my $interactive = getopt('interactive');

#################
# figure out which environment we're running in -- this is a required option
#
my $env = getopt('env') || die usage();
print "Initializing environment script: $env\n";
require $env;
my $envname = get_envname();
print "Loaded environment info for $envname\n";

#################
# figure out which expt to run -- this is a required option
#
my $expt = getopt('expt') || die usage();
print "Initializing experiment script: $expt\n";
require "$expt";
my $exptname = get_exptname();
print "Loaded experiment info for $exptname\n";

#################
# figure out which app to run -- this is a required option
#
my $app = getopt('app') || die usage();
print "Initializing application script: $app\n";
require "$app";
my $appname = get_appname();
print "Loaded application info for $appname\n";


#################
# setup logfiles
#

init_logdir();

###############
# initialize everything
#

# initialize the machine configuration
init_machineconfig();

# initialize the application script
init_env();   # initialize the app script (calls init_env())

# initialize the experiment script
init_expt();   # initialize the expt script (calls init_expt())

# initialize the application script
init_app();   # initialize the app script (calls init_app())



##############
# run the expt
#

run_expt();


# DONE


#################################
#### Log directory functions ####
#################################

my $logdir;

sub get_baselogdir() {
    my $baselogdir = getopt('logdir');
    if( !defined($baselogdir)) {
	$baselogdir= "../logs";
    }

    if (!(-d $baselogdir)) {
	mkdir( $baselogdir, 0776) || die "cannot create directory $baselogdir\n";
    }

    return $baselogdir;
}

sub init_logdir() {

# create a directory called "logs" if it doesn't already exists
    my $baselogdir = get_baselogdir();

# create a subdirectory in "logs" based on app and current time
    $logdir = strftime("$baselogdir/log-$appname-$exptname-%y%m%d-%H%M%S", localtime);
    mkdir($logdir, 0776) || die "cannot create directory $logdir\n";
    
# if requested, create an ID file to go in log directory
    my $tag= getopt('tag');
    if (defined($tag)) {
	# write the tag to a file called ID
	system("echo $tag > $logdir/ID");
    }
    
# redirect stdout and stderr to the log files
    open(STDOUT, "| tee -i $logdir/out.log") || die "cannot redirect STDOUT\n";
    open(STDERR, "| tee -i $logdir/err.log") || die "cannot redirect STDERR\n";
}

sub get_logdir() {
    return $logdir;
}



##################################
#### Cluster Config Functions ####
##################################

my %machineconfig;
my @cluster_clientmachines;
my @cluster_frontendmachines;
my @cluster_backendmachines;
my $cluster_dbmachine;

sub init_machineconfig {
    my $machineconfigfile=getopt('machineconfig') || die usage();
    %machineconfig=read_config_file($machineconfigfile);

    @cluster_clientmachines= split(' ',$machineconfig{'client'});
    @cluster_frontendmachines= split(' ',$machineconfig{'frontend'});
    @cluster_backendmachines= split(' ',$machineconfig{'backend'});
    $cluster_dbmachine= $machineconfig{'dbmachine'};

    print( "cluster client machines are: (", scalar(@cluster_clientmachines), ") ", @cluster_clientmachines,"\n" );
    print( "cluster frontend machines are: (", scalar(@cluster_frontendmachines), ") ", @cluster_frontendmachines,"\n" );
    print( "cluster backend machines are: (", scalar(@cluster_backendmachines), ") ", @cluster_backendmachines,"\n" );
    print( "cluster db machine is: ", $cluster_dbmachine,"\n" );

}

sub get_clientmachines {
    return @cluster_clientmachines;
}

sub get_frontendmachines {
    return @cluster_frontendmachines;
}

sub get_backendmachines {
    return @cluster_backendmachines;
}

sub get_dbmachine {
    return $cluster_dbmachine;
}

sub get_namedmachine($) {
    return $machineconfig{$_[0]};
}

sub print_machineconfig {
    # TODO
}

###########################
#### Utility Functions ####
###########################

sub prepare_app {
    # kill any old instances of the app first
    kill_app();

    # deploy the application
    my @nodeconfigs = deploy_app();
    
    if (! @nodeconfigs ) {
	die "deploy must return the configuration name!\n";
    }

    return @nodeconfigs;
}


# print out an associative array of name/value pairs (for testing)
sub print_opts {
  my %opts = @_;

  my $name;
  my $value;

  while (($name, $value) = each(%opts)) {
    print("$name = $value\n");
  }
}

# print out the usage
# TODO maybe make this more dynamic?
sub usage {
    return "Usage: run.pl --env=environment.pl --expt=experiment.pl --app=application.pl [--interactive] [--config=configfile] [--machineconfig=machineconfigfile] [--tag=tagname] [--logdir=logdir]\n";
}

# utility function to read name/value pairs from config file
# (stored one per line, name=value, or just name (if no value))
# whitespace allowed on right hand side of =
sub read_config_file  {
  my ($config) = @_;
  my %configopts = ();

  my $line;

  if (open(IN, $config)) {
    while ($line = <IN>) {
      chomp($line);
      
      if ($line =~ /^([^\=\s\#]+)(=(.*))?/) {
	  my $name = $1;
	  my $value = $3;
	  
        if (!defined($value)) {$value = "";}

        chomp($name);
        chomp($value);

        if (!defined($configopts{$name})) {
          $configopts{$name} = $value;
        }
      }
    }
  }

  return %configopts;
}

# utility function to extract a name/value pair
# from command-line or config file
#
# parameters:
# 1) name of environment variable
#
# returns undef if option doesn't exist, "" if option doesn't have
# a corresponding value, and the value otherwise
sub getopt {

  my($name) = @_;

  # default type to string
  my($type) = 's';

  # look at command-line first
  my($val);

  $val = $blessedopts{"$name"};
  if (defined($val)) {return $val;}

  GetOptions("$name:$type", \$val);

  if (defined($val)) {
      $blessedopts{"$name"} = $val;
      return $val;
  }

  # then look at config file
  return $configopts{"$name"};
}

sub getoptordie($$) {
    
  my($name) = $_[0];
  my($descr) = $_[1];

  my $retconfig = getopt($name);

  if (!defined($retconfig)) {
      die "need to set --" . $name . "=[" . $descr . "]\n";
  }

  return $retconfig;
}

# make sure that an environment variable is already defined
# if it isn't and we're running in interactive mode, give the
# user a chance to input the value
sub check_env_var {
  my($var) = @_;
  my($value);
  
  if (!($ENV{$var})) {

    if ($interactive) {
      print "Please enter a value for \$$var: " ;

      $value = <STDIN>;
      $ENV{$var} = chomp($value);

    } else {
      die "Error: environment variable $var not defined.\n";
    }
  }
}

# Called when perl can't find a subroutine -- depends on a consistent
# naming convention.  TODO: Should probably add namespaces...
#sub AUTOLOAD
#{
#  use vars '$AUTOLOAD';
#
#  # autoload application-specific subroutines to init, deploy, run, or kill
#  if ( $AUTOLOAD =~ /^main::(init|deploy|run|kill)_(\w+)$/ )
#  {
#     my $file = $2;
#     # require "$file.pl"; # try to import the plugin file
#     # make sure that function is defined
#     if (defined &$AUTOLOAD) {goto &$AUTOLOAD;}
#  }
#  die "Error: couldn't find subroutine <$AUTOLOAD>\n";
#}

# This subroutine execs a command and suppresses all output except
# for that which matches an optional regex parameter or the word
# "xception" (e is missing b/c I need to review my regexes)
#
# parameters:
# 1) string containing command to run (required)
# 2) string containing regex to match stdout to (optional)
#
# This could also be extended to pass in a function pointer to call on
# a match, but that might be overkill for now.
sub exec_and_match
{
  $| = 1;
  my($cmd, $match) = @_;

  if (!defined($match)) {
    $match = "xception";
  } else {
    $match = "($match)|(xception)";
  }

  $execing = 1;

  my $line;

  open(CMDPROC, "$cmd|"); 
  while ($line = <CMDPROC>) {
    if ($line =~ $match) {
      print $line;
    }
  }
  close(CMDPROC);

  $execing = 0;
}

# executes $cmd, detaching from process when output matches $match
sub exec_in_back
{
  my($cmd, $match) = @_;
  if (!defined($match)) {$match = ".";}

  print "waiting for $match on $cmd\n";

  my $tempfilename = ".run" . $$;
  unlink $tempfilename;

  my $kidpid;

  if (!defined($kidpid = fork())) {
    # fork returned undef, so failed
    die "Cannot fork: $!";
  } elsif ($kidpid == 0) {

    # fork returned 0, so this branch is child
    local *CMD;
    
    open(CMD, "$cmd|") || die "couldn't exec $cmd: $!\n"; 

    my $found = 0;

    my $line;

    while ($line = <CMD>) {
      print $line;
      if ($line =~ $match && !$found) {
        system("touch $tempfilename");
        $found = 1;
      }
    }
    close(CMD);
    exit;

  } else {
    # fork returned 0 nor undef
    # so this branch is parent
    while (!(-e $tempfilename)) {
      sleep 1;
    }
    unlink $tempfilename;
    print "done waiting!\n";
  }
}

# ignore SIGINT once if we're execing a process
# this allows us to clean up by calling kill_[app] 
sub my_sigint_catcher {
  if ($execing == 0) {
    print "caught SIGINT -- exiting...\n";
    exit;
  } else {
    $execing = 0;
  }
}
