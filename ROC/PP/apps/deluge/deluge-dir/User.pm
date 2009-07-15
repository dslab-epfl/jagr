use Carp;
use Data::Dumper qw/Dumper/;
use FileHandle;
use POSIX;
use Crypt::SSLeay;
use URI::URL;
use HTTP::Cookies;
use HTTP::Headers;
use HTTP::Request;
use HTTP::Response;
use HTTP::Daemon;

# -------------------------------------------------------------------

use strict;

use Deluge::Agent;
use Deluge::Mcp;
use Deluge::Prereq;
use Deluge::Script;
use Deluge::Stopwatch;
use Deluge::Translator;
use Deluge::Variable;

package Deluge::User;
use vars qw();

# -------------------------------------------------------------------
# Internal Constants

my ($C_VISITED)  = 1;
my ($C_OUTSIDE)  = 2;
my ($C_IGNORED)  = 3;
my ($C_IMAGE)    = 4;
my ($C_SCHEME)   = 5;
my ($C_FILETYPE) = 6;

my ($MODE_DEPTH) = 1;
my ($MODE_BREADTH) = 2;
my ($MODE_WANDER) = 3;
my ($MODE_BOUNCE) = 4;
my ($MODE_PLAYBACK) = 5;

my ($STAT_ACTIVE) = 1;
my ($STAT_SLEEPING) = 2;
my ($STAT_FINISHED)= 3;

# -------------------------------------------------------------------

sub dump_state
{
    my $self = shift;
    my ($id) = $self->id();
    my ($prereq);

    print STDERR "User: $id\n";
    print STDERR "\tStatus: $self->{status}\n";

    if ($self->current_page) {
	print STDERR "\tCurrent Page:\n";
	$self->current_page->dump_state();
    }

    if (@{$self->{_page_queue}}) {
	print STDERR "\tPage Queue\n";
	foreach $prereq (@{$self->{_page_queue}}) {
	    $prereq->dump_state();
	}
    }

    if (@{$self->{_image_queue}}) {
	print STDERR "\tImage Queue\n";
	foreach $prereq (@{$self->{_image_queue}}) {
	    $prereq->dump_state();
	}
    }
}

# -------------------------------------------------------------------

sub _cold_reset
{
    my $self = shift;
    my ($prereq, $snooze, $item);
    
    ($self->id == -1) &&
	die "ERROR: Trying to reset a def, not an instance\n";

    $self->shut_down_kids;
    $self->instances(0);

    if (! (($self->iters == -1) || ($self->restartable))) {
	($self->mcp->debug_level >= 1) &&
	    (print STDERR "===Dead: " . $self->id . "\n");
	$self->status($STAT_FINISHED);
	$self->life_timer(0);
	$self->sub_timer(0);
	$self->cookie_jar(0);
	return;
    }

    # iters starts at -1, so this will bump a first-time user to 0
    $self->iters($self->iters + 1);

    ($self->iters == 0) ? 
	($snooze = $self->mcp->user_ramp_time * rand()) :
	    ($snooze = $self->restart_time +
	     $self->restart_spread * (2*rand() - 1));

    ($self->mcp->debug_level >= 1) &&
	(print STDERR "===Restarting: " . $self->id . " " . $self->iters .
	 " ($snooze) \n");

    $self->reset_cookie_jar();
    ($self->request_vars) && ($self->request_vars->prep);
    $self->all_trans_prepped(0);

    foreach $item (@{$self->{translators}}) {
	$item->reset();
    }

    # Prime the queue
    if (($self->attack_mode == $MODE_DEPTH) ||
	($self->attack_mode == $MODE_BREADTH) ||
	($self->attack_mode == $MODE_WANDER) ||
	($self->attack_mode == $MODE_BOUNCE)) {
	my $prereq = Deluge::Prereq->new("GET", $self->{top_url},
					   "", "a", 0, 1, $self,
					   $self->get_unique_prereq_id);
	# handle any request parameters (playback mode does this itself)
	$prereq->replace_params();
	$self->queue_page($prereq);
    } elsif ($self->attack_mode == $MODE_PLAYBACK) {
	if ($self->mcp->record_mode) {
	    $self->script(Deluge::Script->new($self,
					      $self->script_filename, "a"));
	} else {
	    $self->event_num(0);
	    $self->queue_playback_full_page();
	}
    }

    $self->life_timer->reset();
    $self->sub_timer->reset();
    
    $self->sleep_until($self->sub_timer->time()  + $snooze);
    $self->status($STAT_SLEEPING);
    
    $self->{_per_url_hit_count} = {};
    $self->{_discarded_urls} = {};
    $self->{_pages_traversed_count} = 0;
} # end _cold_reset

# -------------------------------------------------------------------

sub queue_playback_full_page
{
    my $self = shift;
    my ($got_primary) = 0;

    while (1) {
	if ($self->event_num >= $self->def->event_count) {
	    $self->{event_num}++;
	    last;
	}

	# request var and header substitution will happen inside clone()
	my $prereq = @{$self->def->{events}}[$self->event_num]->
	    clone($self, $self->get_unique_prereq_id);

	if (! $prereq) {
	    $self->{event_num}++;
	    last;
	}
	
	if ($prereq->is_image) {
	    if (! $self->get_images) {
		$self->{event_num}++;
		next;
	    }
	    $prereq->dest_url($self->
			      insert_values_into_url($prereq->dest_url));
	    $prereq->ref_url($self->insert_values_into_url($prereq->ref_url));
	    $self->queue_image($prereq);
	} else {
	    last if $got_primary;
	    $got_primary = 1;
	    $prereq->dest_url($self->
			      insert_values_into_url($prereq->dest_url));
	    $prereq->ref_url($self->insert_values_into_url($prereq->ref_url));
	    $self->queue_page($prereq);
	}

	$self->{event_num}++;
    }
} # end queue_playback_full_page

# -------------------------------------------------------------------

sub shut_down
{
    my $self = shift;

    $self->restartable(0);
    $self->_cold_reset();
}

# -------------------------------------------------------------------

sub awake_p
{
    my $self = shift;

    ($self->status == $STAT_ACTIVE) && (return 1);
    # ($self->status == $STAT_FINISHED) && (return 0);
    return 0;
}

# -------------------------------------------------------------------

sub playback_mode
{
    my $self = shift;

    return ($self->attack_mode == $MODE_PLAYBACK);
}

# -------------------------------------------------------------------

sub queue_image
{
    my ($self, $prereq) = @_;

    if (($self->_req_ok_to_queue($prereq)) ||
	($self->playback_mode)) {
	push (@{$self->{_image_queue}}, $prereq);
    } else {
	$prereq->shut_down;
    }
}

# -------------------------------------------------------------------

sub get_unique_prereq_id
{
    my $self = shift;
    my ($upid) = $self->id . "." . $self->{_prereq_count};

    $self->{_prereq_count}++;
    return ($upid);
}

# -------------------------------------------------------------------

sub ok_to_preshuffle_queue
{
    my $self = shift;

    (($self->attack_mode == $MODE_WANDER) ||
     ($self->attack_mode == $MODE_BOUNCE)) &&
	 return 1;

    return 0;
}

# -------------------------------------------------------------------

sub ok_to_presort_queue
{
    my $self = shift;

    (($self->attack_mode == $MODE_DEPTH) ||
     ($self->attack_mode == $MODE_BREADTH)) &&
	 return 1;

    return 0;
}

# -------------------------------------------------------------------

sub _req_ok_to_queue
{
    my ($self, $prereq) = @_;
    my ($uri, $item);

    # Have we discarded this before?
    (exists ($self->{_discarded_urls}->{$prereq->dest_url})) &&
	(return 0);

    # Are we in too deep?
    ($self->limit_depth) &&	($prereq->depth >= $self->limit_depth) &&
	# Don't add to _discarded_urls list.  If we get to it via
	# a shorter path, we still want to use it.
	(return 0);

    # Does it match the mandatory URL regexps?
    if (@{$self->mcp->require_url_regexps}) {
	foreach $item (@{$self->mcp->require_url_regexps}) {
	    if ($prereq->dest_url !~ m|$item|) {
		$self->{_discarded_urls}->{$prereq->dest_url} = $C_FILETYPE;
		return 0;
	    }
	}
    }

    # Does it match the illegal URL regexps?
    foreach $item (@{$self->mcp->ignore_url_regexps}) {
	if ($prereq->dest_url =~ m|$item|) {
	    $self->{_discarded_urls}->{$prereq->dest_url} = $C_FILETYPE;
	    return 0;
	}
    }

    # Is it an image tag?  Are we accepting images?
    if ((! $self->get_images) && ($prereq->is_image)) {
	$self->{_discarded_urls}->{$prereq->dest_url} = $C_OUTSIDE;
	return 0;
    }

    # Ok, simple tests out of the way.
    $uri = URI->new($prereq->dest_url);

    # Is it via a legal scheme?
    if (! Deluge::Etc::scheme_is_legal($uri->scheme,
				       $self->mcp->allow_secure)) {
	$self->{_discarded_urls}->{$prereq->dest_url} = $C_SCHEME;
	return 0;
    }

    # Is it a legal domain?
    if ($self->mcp->domain_match) {
	my ($site) = $uri->authority();

	if (($site !~ m|^$self->{domain_limit}$|) &&
	    ($site !~ m|\.$self->{domain_limit}$|)) {
	    $self->{_discarded_urls}->{$prereq->dest_url} = $C_OUTSIDE;
	    return 0;
	}
    }

    return 1;
}

# -------------------------------------------------------------------

sub note_prereq_as_queued
{
    my ($self, $prereq) = @_;
    
    $self->{_per_url_hit_count}->{$prereq->dest_url} ++;
    $prereq->in_agent(1);
}

# -------------------------------------------------------------------

sub endstate_check
{
    my $self = shift;

    if (($self->limit_pages_traversed) &&
	($self->{_pages_traversed_count} >= $self->limit_pages_traversed)) {
	$self->_cold_reset();
	return 1;
    }

    if (($self->limit_attack_time) &&
	($self->life_timer->elapsed > $self->limit_attack_time)) {
	$self->_cold_reset();
	return 1;
    }

    return 0;
}

# -------------------------------------------------------------------

sub _req_ok_to_register
{
    my ($self, $prereq) = @_;

    ($self->playback_mode) && (return 1);
    
    # Have we discarded this before?
    (exists ($self->{_discarded_urls}->{$prereq->dest_url})) &&
	(return 0);

    # Have we hit it too many times already?
    if ($self->limit_hits_per_url) {
	if (exists ($self->{_per_url_hit_count}->{$prereq->dest_url})) {
	    if ($self->{_per_url_hit_count}->{$prereq->dest_url} >=
		$self->limit_hits_per_url) {
		$self->{_discarded_urls}->{$prereq->dest_url} = $C_VISITED;
		return 0;
	    }
	} else {
	    # Haven't hit it at all.  Initialize the counter.
	    $self->{_per_url_hit_count}->{$prereq->dest_url} = 0;
	}
    }

    return 1;
}

# -------------------------------------------------------------------

sub _queue_script_events
{
    my $self = shift;
    my ($got_primary) = 0;
    
    while (1) {
	my ($prereq);
	
	if ($self->script->event_is_image) {
	    ($prereq = $self->script->event_to_prereq($self, 0)) || last;
	    ($self->get_images) || next;
	    ($self->mcp->debug_level >= 3) &&
		(print STDERR "Queueing image: " . $prereq->dest_url . "\n");
	    $self->queue_image($prereq);
	} else {
	    ($got_primary) && last;
	    $got_primary = 1;
	    ($prereq = $self->script->event_to_prereq($self, 1)) || last;
	    $self->queue_page($prereq);
	    ($self->mcp->debug_level >= 3) &&
		(print STDERR "Queueing PAGE: " . $prereq->dest_url . "\n");
	} 
    }
}

# -------------------------------------------------------------------

sub _get_next_page
{
    my $self = shift;
    my ($prereq);
    
    while (1) {
	if (! @{$self->{_page_queue}}) {
	    $self->_cold_reset();
	    return undef;
	}

	(($self->attack_mode == $MODE_BREADTH) ||
	 ($self->attack_mode == $MODE_WANDER) ||
	 ($self->attack_mode == $MODE_PLAYBACK)) &&
	     ($prereq = shift(@{$self->{_page_queue}}));

	($self->attack_mode == $MODE_DEPTH) &&
	    ($prereq = pop(@{$self->{_page_queue}}));

	($self->attack_mode == $MODE_BOUNCE) &&
	    ($prereq = splice(@{$self->{_page_queue}},
			      int(($#{@{$self->{_page_queue}}}+1)*rand()),1));
	
	($self->_req_ok_to_register($prereq)) ?
	    (return $prereq) : ($prereq->shut_down);
    }
}

# -------------------------------------------------------------------
# Return -1 if dead, 0 if sleeping, 1 if active

sub _do_stuff_active
{
    my $self = shift;
    my ($prereq);
    my ($active) = 0;

    if (! $self->current_page->executed) {
	if (($self->current_page->failed) && (! $self->playback_mode)) {
	    $self->_warm_reset;
	    ($self->status == $STAT_FINISHED) ? (return -1) : (return 0);
	}

	return 0;
    }
    
    if ($self->attack_mode == $MODE_WANDER) {
	if ($self->current_page->response and
	    $self->current_page->response->code >= 400) {
	    while ($prereq = shift(@{$self->{_page_queue}})) {
		$prereq->shut_down;
	    }
	    $prereq = 0
	    }
    }

    if (! $self->get_images) {
	$self->_warm_reset();
	return 0;
    }

    foreach $prereq (@{$self->{_image_queue}}) {
	(($prereq->ignored) || ($prereq->failed) || ($prereq->executed)) &&
	    next;

	$active = 1;
    }

    if (! $active) {
	$self->_warm_reset;
	($self->status == $STAT_FINISHED) ? (return -1) : (return 0);
    }
    
    return (1);
}

# -------------------------------------------------------------------

sub images_to_agent
{
    my $self = shift;
    my ($prereq);

    foreach $prereq (@{$self->{_image_queue}}) {
	if ($self->_req_ok_to_register($prereq)) {
	    $self->mcp->add_req_to_agent($prereq);
	    $self->note_prereq_as_queued($prereq);
	} else {
	    $prereq->shut_down;
	}
    }
}

# -------------------------------------------------------------------

sub sleeping
{
    my $self = shift;

    ($self->status == $STAT_FINISHED) && (return 999999);
    
    ($self->status == $STAT_SLEEPING) ?
	(return ($self->sleep_until)) : (return 0);
}

# -------------------------------------------------------------------
# Return -1 if dead, 0 if sleeping, 1 if active

sub execute_state
{
    my $self = shift;

    ($self->status == $STAT_FINISHED) && (return -1);
    ($self->endstate_check) && (return 0);

    if ($self->status == $STAT_SLEEPING) {
	my ($timeout) = $self->sub_timer->time() - $self->sleep_until;

	($timeout < 0) && (return 0);

	my $prereq;
	if (! ($prereq = $self->_get_next_page)) {
	    ($self->status == $STAT_FINISHED) ? (return -1) : (return 0);
	}

	$prereq->wait_for_click($timeout);
	$self->current_page($prereq);
	$prereq = 0;

	$self->mcp->add_req_to_agent($self->current_page);
	$self->note_prereq_as_queued($self->current_page);
	$self->status($STAT_ACTIVE);
	$self->sleep_until(0);
	return 1;
    }

    ($self->status == $STAT_ACTIVE) && (return ($self->_do_stuff_active));
}

# -------------------------------------------------------------------

sub queue_page
{
    my ($self, $prereq) = @_;
    ($self->_req_ok_to_queue($prereq) || $self->playback_mode) ?
	push(@{$self->{_page_queue}}, $prereq) : $prereq->shut_down();
}

# -------------------------------------------------------------------

sub shut_down_kids
{
    my $self = shift;
    my ($prereq);
    
    if ($self->current_page) {
	$self->current_page->shut_down;
	$self->current_page(0);
    }

    while ($prereq = shift(@{$self->{_image_queue}})) {
	$prereq->shut_down;
    }
    $prereq = 0;

    $self->{_image_queue} = [];

    while ($prereq = shift(@{$self->{_page_queue}})) {
	$prereq->shut_down;
    }
    $prereq = 0;

    $self->{_page_queue} = [];
}

# -------------------------------------------------------------------

sub _warm_reset
{
    my $self = shift;
    my ($prereq);
    
    ($self->id == -1) && die "ERROR: Trying to reset a def, not an instance\n";

    ($self->mcp->debug_level >= 2) &&
	(print STDERR "---Warm: " . $self->id . "\n");

    $self->current_page(undef);

    while ($prereq = shift @{$self->{_image_queue}}) {};
    $prereq = 0;

    ($self->playback_mode) && ($self->queue_playback_full_page);

    $self->sub_timer->reset;

    $self->{_pages_traversed_count} ++;
    $self->status($STAT_SLEEPING);
    $self->sleep_until($self->sub_timer->time + $self->delay_time +
		       (($self->delay_spread *2*rand())-$self->delay_spread));
}

# -------------------------------------------------------------------

sub reset_cookie_jar
{
    my $self = shift;

    ($self->accept_cookies) && ($self->cookie_jar(HTTP::Cookies->new));
}

# -------------------------------------------------------------------

sub clear_visited_list
{
    my $self = shift;

    $self->{_per_url_hit_count} = {};
    $self->{_discarded_urls} = {};
}

# -------------------------------------------------------------------

sub script_filename
{
    my $self = shift;

    return $self->script_dir . "/" . $self->script_file;
}

# -------------------------------------------------------------------

sub start_proxy
{
    my $self = shift;
    my ($connection, $request, $response, $pid);

    # reap children
    $SIG{CLD} = $SIG{CHLD} = sub { wait };

    while (1) {
	if ($connection = $self->{_proxy_daemon}->accept()) {

		$self->proxy_counter($self->proxy_counter + 1);

		# if child, exit the loop and handle the request
		($pid = fork) or last;

		# parent: close my copy of the fd and accept again
		close $connection;
		next;
   	} else {
		# no connection
		# retry if EINTER (Interrupted System Call)
		if ($! == POSIX::EINTR) {
			print STDERR "start_proxy: accept() interrupted: trying again"
			if $self->mcp->debug_level() > 3;
				next;
		}
		die "start_proxy: accept() failed $pid: $!";
	}
    } # end while

    die "start_proxy: fork failed $!" unless defined $pid;

    # child: handle the request
    $request = $connection->get_request();
    ($self->accept_cookies) || ($request->remove_header(qw(Cookie)));
    $self->script->basic_request_to_event($request);

    if (Deluge::Etc::scheme_is_legal($request->url->scheme, 0)) {
	# use the client's UserAgent header, if available
	my ($headers, $agent_header);
	if ($headers = $request->headers()) {
	    if ($agent_header = $headers->header("user-agent") ) {
		$self->{_proxy_agent}->agent($agent_header);
	    }
	}
	
	$response = $self->{_proxy_agent}->request($request);
	($self->accept_cookies) || ($response->remove_header(qw(Set-Cookie)));
	$self->script->response_to_event($request, $response, $self);
    } else {
	$response = HTTP::Response->new(403, "Forbidden");
	$response->content("bad scheme: " 
			   . ($request->url->scheme ? $request->url->scheme : 
			      "undef") . "\n");
	$self->script->bad_request_to_event($request);
    }

    $connection->send_response($response);
    close ($connection);
    exit (0);
}

# -------------------------------------------------------------------

sub compile_script_into_def
{
    my $self = shift;
    my ($counter) = 0;

    ($self->instances) || return;
    
    $self->script(Deluge::Script->new($self, $self->script_filename, "r"));

    while (1) {
	my ($prereq);
	my ($is_image) = $self->script->event_is_image;
	
	($prereq = $self->script->event_to_prereq($self,
						  (! $is_image))) || last;
	push (@{$self->{events}}, $prereq);
	
	$counter ++;
    }

    $self->script(0);
    $self->event_count($counter);
}

# -------------------------------------------------------------------

sub make_user_from_def
{
    my ($self, $id) = @_;
    my ($clone) = Deluge::User->new($self->mcp);
    my ($item);

    ($self->iters >= 0) &&
	die "ERROR: Trying to make a user from an instance, not a def\n";

    foreach $item (qw(accept_cookies
		      compare_content
		      get_images
		      restartable

		      delay_time
		      delay_spread
		      limit_depth
		      limit_hits_per_url
		      limit_pages_traversed
		      limit_attack_time
		      restart_time
		      restart_spread

		      attack_type
		      script_dir
		      script_file
		      top_url

		      domain_limit
		      attack_mode
		      defname
		      )) {
	$clone->{$item} = $self->{$item};
    }

    $clone->id($id);
    $clone->def($self);

    foreach $item (@{$self->{translators}}) {
	push(@{$clone->{translators}}, $item->clone());
    }

    if ($self->request_vars) {
	$clone->request_vars($self->request_vars->clone());
	$clone->request_vars->user($clone);
    }
    
    $clone->_cold_reset;

    return ($clone);
}

# -------------------------------------------------------------------

sub prep_for_record
{
    my $self = shift;
    my ($daemon);

    ($self->attack_mode == $MODE_PLAYBACK) ||
      main::usage("User $self->{defname} isn't using attack_type [playback]");

    $self->{_proxy_agent} = Deluge::Agent->new()
      or die "Deluge::Agent->new failed: $!";

    # env_proxy seems to have no error reporting
    $self->{_proxy_agent}->env_proxy();
    $self->{_proxy_agent}->timeout($self->mcp->timeout)
      or die "Deluge::Agent::timeout failed: $!";

    ($self->mcp->debug_level >= 5) &&
      print "prep_for_record: calling HTTP::Daemon::new with"
      . " LocalAddr=" . $self->mcp->hostname()
      . ", LocalPort=" . $self->mcp->proxy_http_port() . "\n";
    $self->{_proxy_daemon} = HTTP::Daemon->new(LocalAddr =>
					       $self->mcp->hostname,
					       LocalPort =>
					       $self->mcp->proxy_http_port())
      or die "HTTP::Daemon->new failed: $!";

    ($self->{_proxy_daemon}) ||
      main::usage("Proxy failed to start.  Port may be unavailable.");

}

# -------------------------------------------------------------------

sub do_secure_serial
{
    # This is only here until I can figure how to get the parallel user
    # agent to do secure transactions.  dammit.
    
    my ($self, $prereq, $request) = @_;
    my ($agent, $response);

    $request->remove_header(qw(User-Agent));

    $agent = Deluge::Agent->new;
    ($self->cookie_jar) && ($agent->cookie_jar($self->cookie_jar));
    $agent->timeout($self->mcp->timeout);
    $agent->from($self->mcp->owner_email);

    ($self->playback_mode) ?
	($agent->agent($prereq->user_agent_code)) :
	    ($agent->agent($Deluge::Etc::AgentCode));

    print STDERR "Running secure request outside of queue... ";
    $response = $agent->request($request);
    print STDERR "done\n";
    print STDERR "\t" . $prereq->dest_url . "\n";

    $prereq->preprocess($response);

    return (1);
}

# -------------------------------------------------------------------

sub extract_values_from_url
{
    my ($self, $url) = @_;
    my ($ok) = 1;

    foreach my $trans (@{$self->{translators}}) {
	$trans->extract_value_from_url($url);
	$trans->val() || ($ok = 0);
    }

    $self->all_trans_prepped($ok);
}

# -------------------------------------------------------------------

sub insert_values_into_url
{
    my ($self, $url) = @_;
    my ($trans);

    foreach $trans (@{$self->{translators}}) {
	$url = $trans->insert_value_into_url($url);
    }

    return ($url);
}

# -------------------------------------------------------------------

sub _check_config_info
{
    my $self = shift;
    my ($in) = "in user_def [$self->{defname}]";

    ($self->attack_type) ||
	die "ERROR: Missing mandatory [attack_type] assignment $in\n";

    if ($self->attack_type =~ m|^depth$|i) {
	$self->attack_mode($MODE_DEPTH);
    } elsif ($self->attack_type =~ m|^breadth$|i) {
	$self->attack_mode($MODE_BREADTH);
    } elsif ($self->attack_type =~ m|^wander$|i) {
	$self->attack_mode($MODE_WANDER);
    } elsif ($self->attack_type =~ m|^bounce$|i) {
	$self->attack_mode($MODE_BOUNCE);
    } elsif ($self->attack_type =~ m|^playback$|i) {
	$self->attack_mode($MODE_PLAYBACK);
    } else {
      main::usage("Unknown [attack_type] assignment $self->{attack_type} $in");
    }

    if (($self->attack_mode == $MODE_DEPTH) ||
	($self->attack_mode == $MODE_BREADTH) ||
	($self->attack_mode == $MODE_BOUNCE) ||
	($self->attack_mode == $MODE_WANDER)) {
	my ($uri, $dname, $count);
	my (@sitelist) = ();

	($self->top_url) ||
	  main::usage("Missing mandatory [top_url] assignment $in");

	# split the target into host/subdomain/domain, 
	# so we can apply the domain limit
	$uri = URI->new($self->top_url);

	@sitelist = split('\.', $uri->authority());
	$count = $self->mcp->domain_match;

	while ($count and scalar @sitelist) {
	    # pre-pend the domain hierarchy until we reach the limit
	    # why the double-backslash? if we're using this for regex, quotemeta
	    #$dname = "\\." . pop(@sitelist) . $dname;
	    $dname = pop(@sitelist) . ($dname ? ".$dname" : "");
	    $count--;
	}
	$self->domain_limit($dname);
    }

    if (! $self->mcp->record_mode) {
	($self->{instances} == -1) &&
	  main::usage("Missing mandatory [instances] assignment $in");

	($self->{delay_time} == -1) &&
	  main::usage("Missing mandatory [delay_time] assignment $in");
	
	($self->{delay_spread} == -1) &&
	  main::usage("Missing mandatory [delay_spread] assignment $in");
	
	($self->{restart_time} == -1) &&
	  main::usage("Missing mandatory [delay_time] assignment $in");
	
	($self->{restart_spread} == -1) &&
	  main::usage("Missing mandatory [delay_spread] assignment $in");
    }
    
    if ($self->attack_mode == $MODE_PLAYBACK) {
	($self->script_dir) ||
	  main::usage("Missing [script_dir] assignment $in");

	if (-e $self->script_dir) {
	    unless (-d $self->script_dir) {
	      main::usage("Value for [script_dir] \"$self->{script_dir}\" ".
			  "is not a directory $in\n");
	    }
	} else {
	    mkdir($self->script_dir) or
		die "mkdir failed for script_dir \"$self->{script_dir}\": $!";
	}

	($self->script_file) ||
	  main::usage("Missing [script_file] assignment $in");


	if ($self->mcp->record_mode) {
	    (-f $self->script_filename) &&
	      main::usage("[script_dir] is not empty: " . $self->script_filename 
				. " $in");
	} else {
	    (-f $self->script_filename) ||
	      main::usage("Value for [script_file] \"$self->{script_file}\"".
			  " is not a file in directory $self->{script_dir} ".
			  "from assignment $in\n");
	    
	    (-r $self->script_filename) ||
	      main::usage("Script file \"$self->{script_file}\" ".
			  "is not readable from assignment $in\n");

	    $self->compile_script_into_def();
	}
    }
}

# -------------------------------------------------------------------
#
# skip past a user in the config file
# NB: not an object method
#

sub ignore_user
{
    my ($cfg, $tln) = @_;
    my ($line, $tag);

  LINE:
    while ($line = $cfg->get_next_line) {
	($line eq "END") && (return);

	my ($car, $cdr) = $cfg->get_pair($line);

	# handle any sub-objects
	if ($car eq "request_vars") {
	    $cfg->get_list();
	    next LINE;
	}

	if ($car eq "translator") {
	    $cfg->get_list();
	    next LINE;
	}

	# ignore anything else

    } # end while get_next_line    

  main::usage("No END tag found for user_def starting at line $tln");

} # end ignore_user

# -------------------------------------------------------------------

sub _read_config_file
{
    my ($self, $cfg, $tln) = @_;
    my ($line, $tag);

  LINE:
    while ($line = $cfg->get_next_line) {
	($line eq "END") && (return);
	
	my ($car, $cdr) = $cfg->get_pair($line);

	# Switch values
	foreach $tag (qw(accept_cookies
			 compare_content
			 get_images
			 playback_errors
			 restartable)) {
	    if ($car eq $tag) {
		$self->{$tag} = $cfg->get_switch($car, $cdr);
		next LINE;
	    }
	}
	
	# Numerical values
	foreach $tag (qw(attack_time
			 id_offset
			 instances
			 processes
			 delay_time
			 delay_spread
			 limit_depth
			 limit_hits_per_url
			 limit_pages_traversed
			 limit_attack_time
			 max_rate
			 restart_time
			 restart_spread)) {
	    if ($car eq $tag) {
		$self->{$tag} = $cfg->get_number($car, $cdr);
		next LINE;
	    }
	}

	# String values
	foreach $tag (qw(attack_type
			 script_dir
			 script_file
			 top_url)) {
	    if ($car eq $tag) {
		$self->{$tag} = $cdr;
		next LINE;
	    }
	}

	# Objects
	if ($car eq "request_vars") {
	    $self->{$car} = Deluge::Variable->new($self, $cfg);
	    next LINE;
	}

	if ($car eq "translator") {
	    push(@{$self->{translators}},
	       Deluge::Translator->new($cfg));
	    next LINE;
	}

	$cfg->error("Unknown variable [$car]");
    }

  main::usage("No END tag found for user_def starting at line $tln");
}

# -------------------------------------------------------------------

sub _initialize
{
    my ($self, $mcp) = @_;

    # -- Public: Config file --
    # Switches
    $self->{accept_cookies} = 0;
    $self->{compare_content} = 0;
    $self->{get_images} = 0;
    $self->{restartable} = 0;
    $self->{playback_errors} = 0;

    # Numerical
    $self->{instances} = -1;
    $self->{delay_time} = -1;
    $self->{delay_spread} = -1;
    $self->{limit_depth} = 0;
    $self->{limit_hits_per_url} = 0;
    $self->{limit_pages_traversed} = 0;
    $self->{limit_attack_time} = 0;
    $self->{restart_time} = 0;
    $self->{restart_spread} = 0;

    # Strings
    $self->{attack_type} = "";
    $self->{script_dir} = "";
    $self->{script_file} = "";
    $self->{top_url} = "";

    # Methods
    $self->{request_vars} = 0;
    $self->{translators} = [];

    # -- Computed --
    $self->{domain_limit} = "";  # From top_url and cfg->domain_limit
    $self->{attack_mode} = 0;
    
    # -- Def specific --
    $self->{defname} = "";
    $self->{events} = [];
    $self->{event_count} = 0;

    # -- Instance specific --
    $self->{def} = 0;
    $self->{id} = -1;
    $self->{iters} = -1;
    $self->{event_num} = 0;
    $self->{all_trans_prepped} = 0;
    $self->{life_timer} = Deluge::Stopwatch->new();
    $self->{sub_timer} = Deluge::Stopwatch->new();
    $self->{page_timer} = Deluge::Stopwatch->new();
    # $self->{page_elapsed} = 0;
    $self->{sleep_until} = -1;
    $self->{status} = $STAT_SLEEPING;

    $self->{cookie_jar} = 0;
    $self->{mcp} = $mcp;
    $self->{current_page} = 0;

    
    # -- Private --
    $self->{_prereq_count} = 0;
    
    $self->{_page_queue} = [];
    $self->{_image_queue} = [];

    $self->{_per_url_hit_count} = {};
    $self->{_discarded_urls} = {};
    $self->{_pages_traversed_count} = 0;

    # -- Proxy stuff --
    $self->{script} = 0;

    $self->{_proxy_agent} = 0;
    $self->{_proxy_daemon} = 0;
    $self->{proxy_counter} = 0;
}

# -------- methods to handle private variables ----------------------
sub accept_cookies { my $s = shift; @_ ? ($s->{accept_cookies} = shift) : $s->{accept_cookies} }
sub all_trans_prepped { my $s = shift; @_ ? ($s->{all_trans_prepped} = shift) : $s->{all_trans_prepped} }
sub attack_type { my $s = shift; @_ ? ($s->{attack_type} = shift) : $s->{attack_type} }
sub attack_mode { my $s = shift; @_ ? ($s->{attack_mode} = shift) : $s->{attack_mode} }
sub compare_content { my $s = shift; @_ ? ($s->{compare_content} = shift) : $s->{compare_content} }
sub cookie_jar { my $s = shift; @_ ? ($s->{cookie_jar} = shift) : $s->{cookie_jar} }
sub current_page { my $s = shift; @_ ? ($s->{current_page} = shift) : $s->{current_page} }
sub def { my $s = shift; @_ ? ($s->{def} = shift) : $s->{def} }
sub defname { my $s = shift; @_ ? ($s->{defname} = shift) : $s->{defname} }
sub delay_spread { my $s = shift; @_ ? ($s->{delay_spread} = shift) : $s->{delay_spread} }
sub delay_time { my $s = shift; @_ ? ($s->{delay_time} = shift) : $s->{delay_time} }
sub domain_limit { my $s = shift; @_ ? ($s->{domain_limit} = shift) : $s->{domain_limit} }
sub event_count { my $s = shift; @_ ? ($s->{event_count} = shift) : $s->{event_count} }
sub event_num { my $s = shift; @_ ? ($s->{event_num} = shift) : $s->{event_num} }
sub get_images { my $s = shift; @_ ? ($s->{get_images} = shift) : $s->{get_images} }
sub id { my $s = shift; @_ ? ($s->{id} = shift) : $s->{id} }
sub iters { my $s = shift; @_ ? ($s->{iters} = shift) : $s->{iters} }
sub instances { my $s = shift; @_ ? ($s->{instances} = shift) : $s->{instances} }
sub life_timer { my $s = shift; @_ ? ($s->{life_timer} = shift) : $s->{life_timer} }
sub limit_attack_time { my $s = shift; @_ ? ($s->{limit_attack_time} = shift) : $s->{limit_attack_time} }
sub limit_depth { my $s = shift; @_ ? ($s->{limit_depth} = shift) : $s->{limit_depth} }
sub limit_hits_per_url { my $s = shift; @_ ? ($s->{limit_hits_per_url} = shift) : $s->{limit_hits_per_url} }
sub limit_pages_traversed { my $s = shift; @_ ? ($s->{limit_pages_traversed} = shift) : $s->{limit_pages_traversed} }
sub mcp { my $s = shift; @_ ? ($s->{mcp} = shift) : $s->{mcp} }
sub page_timer { my $s = shift; @_ ? ($s->{page_timer} = shift) : $s->{page_timer} }
sub playback_errors { my $s = shift; @_ ? ($s->{playback_errors} = shift) : $s->{playback_errors} }
sub proxy_counter { my $s = shift; @_ ? ($s->{proxy_counter} = shift) : $s->{proxy_counter} }
sub request_vars { my $s = shift; @_ ? ($s->{request_vars} = shift) : $s->{request_vars} }
sub restartable { my $s = shift; @_ ? ($s->{restartable} = shift) : $s->{restartable} }
sub restart_spread { my $s = shift; @_ ? ($s->{restart_spread} = shift) : $s->{restart_spread} }
sub restart_time { my $s = shift; @_ ? ($s->{restart_time} = shift) : $s->{restart_time} }
sub script { my $s = shift; @_ ? ($s->{script} = shift) : $s->{script} }
sub script_dir { my $s = shift; @_ ? ($s->{script_dir} = shift) : $s->{script_dir} }
sub script_file { my $s = shift; @_ ? ($s->{script_file} = shift) : $s->{script_file} }
sub sleep_until { my $s = shift; @_ ? ($s->{sleep_until} = shift) : $s->{sleep_until} }
sub status { my $s = shift; @_ ? ($s->{status} = shift) : $s->{status} }
sub sub_timer { my $s = shift; @_ ? ($s->{sub_timer} = shift) : $s->{sub_timer} }
sub top_url { my $s = shift; @_ ? ($s->{top_url} = shift) : $s->{top_url} }

# -------------------------------------------------------------------

sub new
{
    my ($above, $mcp, $cfg, $defname) = @_;
    my ($class) = ref($above) || $above;
    my ($self) = {};
    
    bless ($self, $class);

    $self->_initialize($mcp);

    if ($cfg) {
	my ($toplinenum) = $cfg->linenum;
	$self->defname($defname);
	$self->_read_config_file($cfg, $toplinenum);
	$self->_check_config_info($mcp);
    }
    
    return ($self);
}

# -------------------------------------------------------------------

1;
