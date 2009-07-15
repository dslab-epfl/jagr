use Carp;

use Crypt::SSLeay;
use LWP::Parallel::UserAgent;
use LWP::Parallel::Protocol::https;

# -------------------------------------------------------------------

use strict;

use Deluge::User;
use Deluge::Stopwatch;

package Deluge::Mcp;
use vars qw();

# singleton: count of child processes
my $children = 0;

# -------------------------------------------------------------------

sub dump_state
{
    my ($self) = @_;
    my ($user, $i);

    print STDERR "---------STATE DUMP----------\n";
    $i = $self->user_count;
    print STDERR "Num standard users: $i\n";
    
    foreach $user (@{$self->{_users}}) {
	$user->dump_state();
    }

    if ($self->proxy_user) {
	print STDERR "Proxy user exists:\n";
	$self->proxy_user->dump_state;
    }

    print STDERR "-----------------------------\n";
}

# -------------------------------------------------------------------

sub shut_down
{
    my $self = shift;

    $self->{_agent} = undef;
    $self->{_agent_queue_timer} = undef;

    my $user;
    while ($user = shift (@{$self->{_users}})) { $user->shut_down }

    $self->{log_fh} = undef;
    $self->{timer} = undef;
} # end shut_down

# -------------------------------------------------------------------

sub add_req_to_agent
{
    my ($self, $prereq) = @_;
    my ($req, $res);

    (! @{$self->{_agent_queue}}) && ($self->{_agent_queue_timer}->reset);
    push (@{$self->{_agent_queue}}, $prereq);
    $self->{_agent_queue_count} ++;
}

# -------------------------------------------------------------------

sub main_attack_loop
{
    my ($self) = @_;

    my ($dead_count, $active_count, $min_sleep, $res);

  MAIN_LOOP: while (1) {
      $dead_count = $active_count = 0;
      $min_sleep = $self->{_agent_queue_timer}->time + 999999;  #hack
      
      print STDERR "------------------------------------\n"
	    if $self->debug_level();
    
    SUB_LOOP: for my $user ( @{$self->{_users}}  ) {
	
	return if $self->endstate_check();

	($self->debug_level >= 2) &&
	    (print STDERR "Processing user: " . $user->id . "\n");

	# Run the user, check its state
	$res = $user->execute_state();

	($self->debug_level >= 2) &&
	    (print STDERR "STATE: $res\n");

	($res == -1) && ($dead_count ++);
	($res == 1) && ($active_count ++);

	($self->debug_level >= 5) &&
	    (print STDERR "TIME: " .
	     $self->{_agent_queue_timer}->elapsed . "\n");

	# check max_rate and sleep if we exceed it
	# right now, each child process is responsible for only one user_def
	# so we can assume that the 0 entry is _it_
	if (defined $self->{_user_defs}->[0]->{max_rate}
	    and  $self->{_user_defs}->[0]->{max_rate} > 0
	    and $self->{operation_counter} > 0
	    and $self->timer->elapsed() > 0 ) {
	    print STDERR "checking max_rate throttle\n" if $self->debug_level() > 7;
	    # calculate how long we should have spent so far, minus elapsed
	    my $sleep = ( $self->{operation_counter} /
			  $self->{_user_defs}->[0]->{max_rate} ) - 
			      $self->timer->elapsed();
	    # skip it unless we're at least 100 ms off
	    while ( $sleep > 0.1 ) {
		print STDERR "throttling to max_rate: sleep=$sleep\n"
		    if $self->debug_level() > 3;
		$self->timer->sleep($sleep);
		$sleep = ( $self->{operation_counter} /
			   $self->{_user_defs}->[0]->{max_rate} ) - 
			       $self->timer->elapsed();
	    }
	} # end if max_rate

	# Completely empty the queue if it's full or timed out.
	if (($self->{_agent_queue_count} >= $self->threads_per_proc) ||
	    ($self->{_agent_queue_timer}->elapsed >=
	     $self->queue_max_delay)) {
	    while ($self->{_agent_queue_count}) {
		$self->start_agent();
	    }
	}
    } # end foreach users

($self->debug_level > 1) &&
    (print STDERR "LOOP:  Users: " . $self->user_count .
     ", Dead: $dead_count\n");

# All the users are dead.  Quit.
($dead_count >= $self->user_count) && (return 0);

# Longest sleep we can do is...
for my $user (@{$self->{_users}}) {
    my $foo = $user->sleeping();
    # print STDERR "SLEEP " . $user->id . " has $foo\n";
    $min_sleep = Deluge::Etc::min($min_sleep, $user->sleeping);
}

if ($active_count) {
    if ($self->{_agent_queue_count}) {
	($self->debug_level >= 1) && (print STDERR "TEST: 1 1\n");
	$self->{_agent_queue_timer}->
	    sleep($self->queue_max_delay -
		  $self->{_agent_queue_timer}->elapsed);
    } else {
	($self->debug_level >= 1) && (print STDERR "TEST: 1 0\n");
    }
} else {
    if ($self->{_agent_queue_count}) {
	($self->debug_level >= 1) && (print STDERR "TEST: 0 1\n");
	$self->start_agent();
    } else {
	($self->debug_level >= 1) && (print STDERR "TEST: 0 0\n");
	$self->{_agent_queue_timer}->
	    sleep($min_sleep - $self->timer->time);
    }
} # end if active_count

} # end while 1

} # end main_attack_loop

# -------------------------------------------------------------------

sub register
{
    my ($self, $prereq) = @_;

    $prereq->user->reset_cookie_jar()
	if $prereq->delete_cookies_when_registered();

    $prereq->user->clear_visited_list()
	if $prereq->clear_visited_list_when_registered();
    
    $prereq->user->page_timer->reset()
	if $prereq->primary();

    $prereq->req_wait->reset();
    
    # mike@blakeley.com: add_content doesn't handle GET query strings...
    # so if we want ext_content to affect GET (and I do),
    # we have to re-write the URL a bit before we create the request
    my $uri = URI->new($prereq->dest_url);
    if  ($prereq->method() ne 'POST'
	 and @{$prereq->{ext_content}} ) {
	# XXX handle PUT and friends? if so, make a new method elsewhere
	# meanwhile assume GET-style query string...
	# to make '&&' less likely, I pre-pend the new data
	# NB: this doesn't support the new-style ';' delimiter
	my $query = $uri->query();
	$query .= ($query ? '&' : '')
	    . join('&', @{$prereq->{ext_content}});
	$uri->query($query);
	$prereq->dest_url($uri->as_string());
	# since we've used up the ext_content, make sure it's gone
	$prereq->{ext_content} = [];
    }

    my $request = HTTP::Request->new($prereq->method, $prereq->dest_url);
    $request->push_header($Deluge::Etc::HeaderTag => $prereq->id);

    # add any ext_content (POST variables)
    for (@{$prereq->{ext_content}}) {
	$request->add_content($_);
    }

    if (! $request) {
	$prereq->Log->new_tag($Deluge::Log::TAG_BREQ, "Invalid request");
	$prereq->failed(1);
	return 0;
    }

    while (1) {
	my ($car, $cdr) = $prereq->shift_ext_header();
	($car) || last;
	$request->push_header($car => "$cdr");
    }

    $prereq->Log->new_tag($Deluge::Log::TAG_TIME, $self->timer->time);

    my $ack = $uri->scheme;
    
    if ($ack =~ m|^https|) {
	$prereq->user->do_secure_serial($prereq, $request);
	return 0;
    }

    ($self->debug_level >= 2) &&
	(print STDERR "\tRegistering: (".$prereq->user->id.") " .
	 "$prereq->{dest_url} ...\n");

    my $callback = sub { $prereq->agent_callback(@_) };
    my $entry = $self->{_agent}->register($request, $callback);
    
    if (ref $entry eq 'HTTP::Response') {
	### register returned an HTTP::Response object, so it's an error.
	($self->debug_level >= 2) && (print STDERR "failed 1.\n");
	$prereq->Log->new_tag($Deluge::Log::TAG_BREQ,
			      "Registration failed");
	$self->failed(1);
	return 0;
    } elsif (ref $entry eq "LWP::Parallel::UserAgent::Entry") {
	### register returned an LWP::Parallel::UserAgent::Entry
	### object, so the register succeeded.
	
	($self->debug_level >= 2) && (print STDERR "succeeded.\n");
	$request->remove_header(qw(User-Agent));
	($prereq->user->playback_mode) ? 
	    ($entry->agent($prereq->user_agent_code)) :
		($entry->agent($Deluge::Etc::AgentCode));
	
	($prereq->user->cookie_jar) &&
	    ($entry->cookie_jar($prereq->user->cookie_jar));
	return 1;
    } else {
	### we should never get here, if register worked correctly.
	($self->debug_level >= 2) && (print STDERR "failed 2.\n");
	$prereq->Log->new_tag($Deluge::Log::TAG_BREQ,
			      "Registration returned unknown object");
	$prereq->failed(1);
	return 0;
    }
} # end register

# -------------------------------------------------------------------

sub start_agent
{
    my ($self) = @_;
    my ($entries, $reqcount, $prereq, $entry);
    my (@temp_queue) = ();

    $reqcount = 0;
    while (1) {
	($reqcount >= $self->threads_per_proc) && last;
	($prereq = shift(@{$self->{_agent_queue}})) || last;
	$self->{_agent_queue_count}--;
	push (@temp_queue, $prereq);
	$reqcount += $self->register($prereq);
    }

    print STDERR "Running queue of size $reqcount... "
	if $self->debug_level();
    $entries = $self->{_agent}->wait($self->timeout);
    print STDERR "done.\n" if $self->debug_level();

  ENTRY_LOOP:
    foreach $entry (keys %$entries) {
	my ($item);
	my ($response) = $entries->{$entry}->response;
	my ($pqid) = $response->request->header($Deluge::Etc::HeaderTag);

      PREREQ_LOOP:
	foreach $prereq (@temp_queue) {
	    if ($prereq->id eq $pqid) {
		$prereq->preprocess($response);
		next ENTRY_LOOP;
	    }
	}
    }

  PROCESS_LOOP:
    foreach $prereq (@temp_queue) {
	$prereq->process_response();
	($prereq->primary) && ($prereq->user->images_to_agent);
    }
    
    $self->{_agent_queue_timer}->reset;
    $self->{_agent}->initialize;
}

# -------------------------------------------------------------------

sub endstate_check
{
    my ($self) = @_;

    ($self->attack_time_length) &&
	($self->timer->elapsed >= $self->attack_time_length) &&
	    (return 1);

    return 0;
}

# -------------------------------------------------------------------

sub user_count
{
    # XXX might be better to omit scalar, and let context determine
    scalar @{ (shift)->{_users} };
}

# -------------------------------------------------------------------

sub start_proxy
{
    my ($self) = @_;

    print STDERR "Proxy server running on host " . $self->hostname .
	" on port " . $self->proxy_http_port . "\n";

    $self->proxy_user->start_proxy;
}

# -------------------------------------------------------------------


# from perldoc perlipc
sub REAPER {
    my $wpid = wait;
    #print STDERR "reaper: $wpid\n";
    $children--;

    # loathe sysV: it makes us not only reinstate
    # the handler, but place it after the wait
    $SIG{CLD} = $SIG{CHLD} = \&REAPER;
}

sub prep_for_attack
{
    my ($self) = @_;
    my ($i, $pid, $floor);

    # open logfile before forking, so the fd is inherited
    $self->open_log_file("w");

    # remember the parent PID
    $self->parent($$);
    $SIG{CLD} = $SIG{CHLD} = \&REAPER;

    # basis for __ID increments
    $floor = 0;

    # fork one process per _user_def for now
    # XXX allow split of instances within a user_def across processes

  DEF: for my $def (@{$self->{_user_defs}}) {
      # fork as many children as the user_def asks for,
      # each with the same number of instances
      # always at least one process per def
      $def->{processes} ||= 1;
      for (1..$def->{processes}) {
	  # by default we number all the instances in succession
	  # however, if there's an id_offset, it trumps $floor
	  # use with caution... avoid negative numbers
	  if ($def->{id_offset}) {
	      $floor += $def->{id_offset};
	  }
	  print STDERR "using floor = $floor + " . $def->{id_offset} 
	  if $self->debug_level() > 3;

	  if ($pid = fork) {
	      # parent: increment floor, children and move on
	      $floor += $def->instances();
	      $children++;
	      next;
	  } else {
	      die "fork failed: $!" unless defined $pid;

	      # child
	      print STDERR $$ . ": " . $def->instances() . " " 
		  . $def->defname() . " users\n";

	      # to avoid confusion down the road, make it clear
	      # that all the users in this process have the same def
	      $self->{_user_defs_} = [ $def ];

	      for ($i=0; $i<$def->instances; $i++) {
		  # user_count is just @users in scalar context
		  push(@{$self->{_users}},
		       #$def->make_user_from_def($self->user_count));
		       # this gives us a 'global' id
		       $def->make_user_from_def($floor + $self->user_count));
		  print STDERR ($i < 100 or $i % 100) ? "." : $i;
	      }
	      print STDERR "\n";
	      
	      # set up LWP once per process
	      $self->{_agent} = LWP::Parallel::UserAgent->new();
	      
	      # LWP::Agent standard stuff
	      $self->{_agent}->env_proxy();
	      $self->{_agent}->timeout($self->timeout);
	      $self->{_agent}->from($self->owner_email);
	      
	      # LWP::Parallel::Agent specific stuff
	      $self->{_agent}->in_order(0);
	      $self->{_agent}->duplicates(0);
	      # $self->{_agent}->redirect(1);
	      $self->{_agent}->redirect(0);
	      $self->{_agent}->max_req($self->threads_per_proc);

	      $self->{_agent_queue_timer}->reset;

	      # exit the loop - this child is done
	      last DEF;
	  } # end if fork
      } # end for processes
  } # end for defs
} # end prep_for_attack

# -------------------------------------------------------------------

sub prep_for_record
{
    my $self = shift;

    $self->{proxy_defname} or 
	die "Mcp::prep_for_record called without proxy_defname";

    for my $def (@{$self->{_user_defs}}) {
	next unless $self->{proxy_defname} eq $def->defname;
	$self->proxy_user($def->make_user_from_def($self->user_count, $self));
	$self->proxy_user->prep_for_record($self);
	# we found our man: go no farther
	return;
    }

  main::usage("No such user [$self->{proxy_defname}] in config file");
}

# -------------------------------------------------------------------

sub open_log_file
{
    my ($self, $mode) = @_;

    if ($mode eq "w") {
	($self->{log_fh} = new FileHandle $self->log_filename, "w") ||
	  main::usage("Can't open [$self->{log_filename}] for writing");
	return;
    }
}

# -------------------------------------------------------------------

sub _check_config_info
{
    my ($self) = @_;
    
    ($self->owner_email) ||
      main::usage("Missing mandatory [owner_email] assignment");
    
    ($self->log_filename) ||
      main::usage("Missing mandatory [log_filename] assignment");

    ($self->{timeout} == -1) &&
      main::usage("Missing mandatory [timeout] assignment");

    ($self->{timeout} > 0) ||
      main::usage("Value for [timeout] must be > 0");

    ($self->record_mode) &&
	($self->proxy_http_port == -1) &&
	  main::usage("Missing mandatory [proxy_http_port] assignment");
    
    ($self->threads_per_proc == -1) &&
      main::usage("Missing mandatory [threads_per_proc] assignment");

    ($self->threads_per_proc > 0) ||
      main::usage("Value for [threads_per_proc] must be > 0");

    ($self->hostname) || ($self->hostname(main::hostname()));
}

# -------------------------------------------------------------------

sub _read_config_file
{
    my ($self, $cfg) = @_;
    my ($line, $tag);

  LINE:
    while ($line = $cfg->get_next_line) {
	my ($car, $cdr) = $cfg->get_pair($line);

	# Switch values
	foreach $tag (qw(dump_responses
			 eval_per_url
			 allow_secure
			 verbose_logs)) {
	    if ($car eq $tag) {
		$self->{$tag} = $cfg->get_switch($car, $cdr);
		next LINE;
	    }
	}

	# Numerical values
	foreach $tag (qw(attack_time_length
			 debug_level
			 domain_match
			 eval_hist_time_buckets
			 eval_hist_value_buckets
			 proxy_http_port
			 queue_max_delay
			 threads_per_proc
			 timeout
			 user_ramp_time)) {
	    if ($car eq $tag) {
		$self->{$tag} = $cfg->get_number($car, $cdr);
		next LINE;
	    }
	}

	# String values
	foreach $tag (qw(hostname
			 log_filename
			 owner_email)) {
	    if ($car eq $tag) {
		$self->{$tag} = $cdr;
		next LINE;
	    }
	}

	# Lists
	foreach $tag (qw(ignore_url_regexps
			 require_url_regexps)) {
	    if ($car eq $tag) {
		$self->{$tag} = [ $cfg->get_list($car) ];
		next LINE;
	    }
	}

	# Hashes
	foreach $tag (qw(pos_vis_regexps
			 neg_vis_regexps
			 pos_invis_regexps
			 neg_invis_regexps)) {
	    if ($car eq $tag) {
		$self->{$tag} = { $cfg->get_hash($car) };
		next LINE;
	    }
	}

	# User Objects 
	if ($car eq "user_def") {
		# in proxy mode, only create the user we need
		if ($self->{record_mode} and $self->{proxy_defname} ne $cdr) {
		    # not interested in this user
		  Deluge::User::ignore_user($cfg);
		} else {
		    my ($user) = Deluge::User->new($self, $cfg, $cdr);
		    push(@{$self->{_user_defs}}, $user);
		}
	    next LINE;
	}

	$cfg->error("Unknown variable [$car]");
    }
}

# -------------------------------------------------------------------

sub DESTROY
{
    my ($self) = @;
}

# -------------------------------------------------------------------

sub _initialize
{
    # defname will be null unless we are running in proxy mode
    my ($self, $defname) = @_;

    # -- Private --
    $self->{_user_defs} = [];
    $self->{_users} = [];

    $self->{_agent} = 0;
    $self->{_agent_queue} = [];
    $self->{_agent_queue_count} = 0;
    $self->{_agent_queue_timer} = Deluge::Stopwatch->new;

    # -- Public: config file --
    # Switches
    $self->{dump_responses} = 0;
    $self->{eval_per_url} = 0;
    $self->{allow_secure} = 0;
    $self->{verbose_logs} = 0;
    
    # Numerical
    $self->{attack_time_length} = 0;
    $self->{debug_level} = 0;
    $self->{domain_match} = 0;
    $self->{eval_hist_time_buckets} = 15;
    $self->{eval_hist_value_buckets} = 15;
    $self->{proxy_http_port} = -1;
    $self->{queue_max_delay} = 0;
    $self->{threads_per_proc} = -1;
    $self->{timeout} = -1;
    $self->{user_ramp_time} = 0;
    $self->{operation_counter} = 0;

    # Strings
    $self->{log_filename} = "";
    $self->{owner_email} = "";
    $self->{hostname} = "";

    # Lists
    $self->{ignore_url_regexps} = [];
    $self->{require_url_regexps} = [];

    # Hashes
    $self->{pos_vis_regexps} = {};
    $self->{neg_vis_regexps} = {};
    $self->{pos_invis_regexps} = {};
    $self->{neg_invis_regexps} = {};

    # -- Public --
    $self->{last_active_user} = 0;
    $self->{log_fh} = 0;

    $self->{timer} = Deluge::Stopwatch->new();

    $self->{record_mode} = 0;

    # -- Proxy stuff --
    $self->{proxy_user} = 0;

    if ($defname) {
    	$self->{record_mode} = 1;
	$self->{proxy_defname} = $defname;
    }
} # end _initialize

# ---------- get/set convenience functions --------------------------
sub allow_secure { my $s = shift; @_ ? ($s->{allow_secure} = shift) : $s->{allow_secure} }
sub attack_time_length { my $s = shift; @_ ? ($s->{attack_time_length} = shift) : $s->{attack_time_length} }
sub children { my $s = shift; @_ ? ($children = shift) : $children }
sub debug_level { my $s = shift; @_ ? ($s->{debug_level} = shift) : $s->{debug_level} }
sub domain_match { my $s = shift; @_ ? ($s->{domain_match} = shift) : $s->{domain_match} }
sub dump_responses { my $s = shift; @_ ? ($s->{dump_responses} = shift) : $s->{dump_responses} }
sub eval_hist_time_buckets { my $s = shift; @_ ? ($s->{eval_hist_time_buckets} = shift) : $s->{eval_hist_time_buckets} }
sub eval_hist_value_buckets { my $s = shift; @_ ? ($s->{eval_hist_value_buckets} = shift) : $s->{eval_hist_value_buckets} }
sub eval_per_url { my $s = shift; @_ ? ($s->{eval_per_url} = shift) : $s->{eval_per_url} }
sub hostname { my $s = shift; @_ ? ($s->{hostname} = shift) : $s->{hostname} }
#sub id { my $s = shift; @_ ? ($s->{id} = shift) : $s->{id} }
sub ignore_url_regexps { my $s = shift; @_ ? ($s->{ignore_url_regexps} = shift) : $s->{ignore_url_regexps} }
sub log_filename { my $s = shift; @_ ? ($s->{log_filename} = shift) : $s->{log_filename} }
sub log_fh { my $s = shift; @_ ? ($s->{log_fh} = shift) : $s->{log_fh} }
sub neg_invis_regexps { my $s = shift; @_ ? ($s->{neg_invis_regexps} = shift) : $s->{neg_invis_regexps} }
sub neg_vis_regexps { my $s = shift; @_ ? ($s->{neg_vis_regexps} = shift) : $s->{neg_vis_regexps} }
sub owner_email { my $s = shift; @_ ? ($s->{owner_email} = shift) : $s->{owner_email} }
sub parent { my $s = shift; @_ ? ($s->{parent} = shift) : $s->{parent} }
sub pos_invis_regexps { my $s = shift; @_ ? ($s->{pos_invis_regexps} = shift) : $s->{pos_invis_regexps} }
sub pos_vis_regexps { my $s = shift; @_ ? ($s->{pos_vis_regexps} = shift) : $s->{pos_vis_regexps} }
sub proxy_http_port { my $s = shift; @_ ? ($s->{proxy_http_port} = shift) : $s->{proxy_http_port} }
sub proxy_user { my $s = shift; @_ ? ($s->{proxy_user} = shift) : $s->{proxy_user} }
sub queue_max_delay { my $s = shift; @_ ? ($s->{queue_max_delay} = shift) : $s->{queue_max_delay} }
sub record_mode { my $s = shift; @_ ? ($s->{record_mode} = shift) : $s->{record_mode} }
sub require_url_regexps { my $s = shift; @_ ? ($s->{require_url_regexps} = shift) : $s->{require_url_regexps} }
sub threads_per_proc { my $s = shift; @_ ? ($s->{threads_per_proc} = shift) : $s->{threads_per_proc} }
sub timeout { my $s = shift; @_ ? ($s->{timeout} = shift) : $s->{timeout} }
sub timer { my $s = shift; @_ ? ($s->{timer} = shift) : $s->{timer} }
sub user_ramp_time { my $s = shift; @_ ? ($s->{user_ramp_time} = shift) : $s->{user_ramp_time} }
sub verbose_logs { my $s = shift; @_ ? ($s->{verbose_logs} = shift) : $s->{verbose_logs} }

=pod

The increment_counter() function adds 1 to the global operation counter.
This is used to determine the current operation rate, for max_rate
throttling and concurrency control.

=cut

sub increment_counter {
    (shift)->{operation_counter}++;
} # end increment_counter

# -------------------------------------------------------------------

sub new
{
    # defname will be null unless we're in proxy mode
    my ($above, $cfg, $defname) = @_;
    my ($class) = ref($above) || $above;
    my ($self) = {};
    my ($fname) = shift;
    
    bless ($self, $class);

    $self->_initialize($defname);
    $self->_read_config_file($cfg);
    $self->_check_config_info;

    if ($defname) {
	$self->prep_for_record($defname);
    }

    return $self;
}

1;

# end Mcp.pm
