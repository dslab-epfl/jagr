use Carp;
use Data::Dumper;
use FileHandle;
use Crypt::SSLeay;
use HTML::LinkExtor;
use HTML::TokeParser;
use LWP::Parallel::UserAgent;

# -------------------------------------------------------------------

use strict;
local $^W = 1;

require Deluge::Etc;
require Deluge::Log;
require Deluge::Mcp;
require Deluge::User;

package Deluge::Prereq;
use vars qw();

# -------------------------------------------------------------------
# Internal Constants

my ($C_META_HTTP) = 1;
my ($C_META_NAME) = 2;

# -------------------------------------------------------------------

sub dump_state
{
    my ($self) = @_;

	print STDERR "\t\tPrereq: $self->{id}\n";
	print STDERR "\t\t\tURL: $self->{dest_url}\n";
	print STDERR "\t\t\tIn_agent: $self->{in_agent}\n";
	print STDERR "\t\t\tExecuted: $self->{executed}\n";
	print STDERR "\t\t\tFailed: $self->{failed}\n";
	print STDERR "\t\t\tIgnored: $self->{ignored}\n";
}

# -------------------------------------------------------------------

sub shut_down
{
    my ($self) = @_;
    $self->ignored(1);
}

# -------------------------------------------------------------------

sub agent_callback
{
    my ($self, $content, $response, $protocol, $entry) = @_;
	
	$self->callback(1);
	$self->req_wait->set_mark;
	$self->user->page_timer->set_mark;

	($self->is_image) || ($self->{content} .= $content);
	$self->{content_length} += length($content);
		
	return undef;
}

# -------------------------------------------------------------------

sub preprocess
{
    my ($self, $response) = @_;

	$self->response($response);
	
	if ($self->callback) {
		$self->response->content($self->content);
		$self->{content} = "";
	} else {
		$self->req_wait->set_mark;
		$self->user->page_timer->set_mark;
	}
}

# -------------------------------------------------------------------

sub push_ext_header
{
    my ($self, $car, $cdr) = @_;
    push(@{$self->{_ext_headers}}, $car, $cdr);
}

# -------------------------------------------------------------------

sub shift_ext_header
{
    my $self = shift;
    @{$self->{_ext_headers}} ?
	return ( shift(@{$self->{_ext_headers}}),
		 shift(@{$self->{_ext_headers}}) )
	    # return undef?
	    : return ("", "");
}

# -------------------------------------------------------------------

# XXX mike@blakeley.com: someday I hope to convert ext_header to a hash
sub ext_header {
    my ($self, $key, $value) = @_;
    # set, if there's a value to set
    $self->{_ext_headers}->{$key} = $value if defined $value;
    # always return the current value
    return $self->{_ext_headers}->{$key};
} # end ext_header

# -------------------------------------------------------------------

sub _cache_link
{
    my ($self, $url, $tag) = @_;
    my ($prereq, $is_image);

    $self->user->playback_mode() &&
	($self->user->extract_values_from_url($url));
    
    if (Deluge::Etc::url_is_image($url, $tag)) {
	if ($self->user->get_images) {
	    push (@{$self->{_image_link_cache_urls}}, $url);
	    push (@{$self->{_image_link_cache_tags}}, $tag);
	}
    } else {
	push (@{$self->{_page_link_cache_urls}}, $url);
	push (@{$self->{_page_link_cache_tags}}, $tag);
    }
}

# -------------------------------------------------------------------

sub _parse_response_meta_tag_pairs
{
    my ($self, $metatype, $metasubtype, $car, $cdr) = @_;

    if (($metatype == $C_META_HTTP) &&
	(($metasubtype =~ m|^refresh$|i) ||
	 ($metasubtype =~ m|^content-style-type$|i)) &&
	(($car =~ m|^content$|) && ($cdr =~ m|\=(.*)|))) {
	$self->_cache_link($1, "a");
    }

    if (($metatype == $C_META_NAME) &&
	($metasubtype =~ m|^postinfo$|i) &&
	($car =~ m|^content$|) &&
	($cdr =~ m|\=(.*)|)) {
	$self->_cache_link($1, "a");
    }
}

# -------------------------------------------------------------------

sub _parse_response_vis_text
{
    my ($self, $text) = @_;
    my (@temp) = ();
    my ($item);

    foreach $item (@{$self->{_pos_vis}}) {
	($text =~ m|$item|) || (push (@temp, $item));
    }
    $self->{_pos_vis} = [];
    foreach $item (@temp) {
	push (@{$self->{_pos_vis}}, $item);
    }

    @temp = ();
    foreach $item (@{$self->{_neg_vis}}) {
	($text =~ m|$item|) ?
	    ($self->Log->new_tag($Deluge::Log::TAG_NVVR, $item)) :
		(push (@temp, $item));
    }
    $self->{_neg_vis} = [];
    foreach $item (@temp) {
	push (@{$self->{_neg_vis}}, $item);
    }
}

# -------------------------------------------------------------------

sub _parse_response_invis_text
{
    my ($self, $text) = @_;
    my (@temp) = ();
    my ($item);

    foreach $item (@{$self->{_pos_invis}}) {
	($text =~ m|$item|) || (push (@temp, $item));
    }
    $self->{_pos_invis} = [];
    foreach $item (@temp) {
	push (@{$self->{_pos_invis}}, $item);
    }
    
    @temp = ();
    foreach $item (@{$self->{_neg_invis}}) {
	($text =~ m|$item|) ?
	    ($self->Log->new_tag($Deluge::Log::TAG_NIVR, $item)) :
		(push (@temp, $item));
    }
    $self->{_neg_invis} = [];
    foreach $item (@temp) {
	push (@{$self->{_neg_invis}}, $item);
    }
}

# -------------------------------------------------------------------

sub _parse_response_tag_start
{
    my ($self, $lump) = @_;
    my ($tag) = $lump->[1];
    my (%attr) = %{$lump->[2]};
    my (@attrseq) = @{$lump->[3]};
    my ($car, $cdr);

    if ($tag eq "meta") {
	my ($metatype) = 0;
	my ($metasubtype);

	if (exists($attr{"http-equiv"})) {
	    $metatype = $C_META_HTTP;
	    $metasubtype = $attr{"http-equiv"};
	}

	if (exists($attr{"name"})) {
	    $metatype = $C_META_NAME;
	    $metasubtype = $attr{"name"};
	}

	if (! $metatype) {
	    # ERROR: Mandatory META tag type missing
	    ($self->user->mcp->verbose_logs) &&
		$self->Log->new_tag($Deluge::Log::TAG_XMME, 1);
	    return;
	}
	
	foreach $car (keys(%attr)) {
	    $cdr = $attr{$car};
	    (($car eq "http-equiv") || ($car eq "name")) && next;
	    
	    $self->_parse_response_meta_tag_pairs($metatype, $metasubtype,
						  $car, $cdr);
	}
    }

    $self->_parse_response_invis_text($lump->[4]);

}

# -------------------------------------------------------------------

sub _parse_response_tag_end
{
    my ($self, $lump) = @_;
    my ($tag) = $lump->[1];

    $self->_parse_response_invis_text($lump->[2]);
}

# -------------------------------------------------------------------

sub _prep_local_expects
{
    my ($self) = @_;
    my ($car, $cdr);

    foreach $car (keys(%{$self->user->mcp->pos_vis_regexps})) {
	($self->dest_url =~ m|$car|) &&
	    (push (@{$self->{_pos_vis}},
		   $self->user->mcp->pos_vis_regexps->{$car}));
    }

    foreach $car (keys(%{$self->user->mcp->neg_vis_regexps})) {
	($self->dest_url =~ m|$car|) &&
	    (push (@{$self->{_neg_vis}},
		   $self->user->mcp->neg_vis_regexps->{$car}));
    }

    foreach $car (keys(%{$self->user->mcp->pos_invis_regexps})) {
	($self->dest_url =~ m|$car|) &&
	    (push (@{$self->{_pos_invis}},
		   $self->user->mcp->pos_invis_regexps->{$car}));
    }

    foreach $car (keys(%{$self->user->mcp->neg_invis_regexps})) {
	($self->dest_url =~ m|$car|) &&
	    (push (@{$self->{_neg_invis}},
		   $self->user->neg_invis_regexps->{$car}));
    }
}

# -------------------------------------------------------------------
#
# _parse_response
#      This parses (as HTML) any text body in an HTTP response, looking
#      for visible text and invisible text that matches
#      the user-defined pos_invis, neg_invis, pos_vis, and neg_vis
#      critera.

sub _parse_response
{
    my ($self) = @_;
    my ($parser);

    # handle multiple content types
  SWITCH: for ($self->response->content_type()) {
      # this allows future support for XML, WML, and other formatted text
      #m|^text/vnd.wap.wml$| && do {
      #$parser = WML::TokeParser->new(\$self->response->content());
      #last SWITCH;
      #};
      # XXX plain text could also be handled (vis only)
      # any unmatched text: try HTML
      m|^text/| && do {
	  $parser = HTML::TokeParser->new(\$self->response->content());
	  last SWITCH;
      };
      # default: do nothing
  } # end SWITCH

    unless ($parser) {
	$self->Log->new_tag($Deluge::Log::TAG_XNRD, 1) 
	    if $self->user->mcp->verbose_logs();
	return 0;
    }

    $self->_prep_local_expects;

    my ($item, $type);
    while (1) {
	if ($item = $parser->get_trimmed_text()) {
	    $self->_parse_response_vis_text($item);
	    next;
	}

	if ($item = $parser->get_token()) {
	    $type = $item->[0];

	    if ($type eq "S") {
		$self->_parse_response_tag_start($item);
		next;
	    }

	    if ($type eq "E") {
		$self->_parse_response_tag_end($item);
		next;
	    }

	    if ($type eq "T") {
		$self->_parse_response_vis_text($item->[1]);
		next;
	    }

	    if (($type eq "C") || ($type eq "D") || ($type eq "PI")) {
		$self->_parse_response_invis_text($item->[1]);
		next;
	    }

	    ($self->user->mcp->verbose_logs) &&
		$self->Log->new_tag($Deluge::Log::TAG_XUKT, $type);
	    next;
	}

	last;
    }

    foreach $item (@{$self->{_pos_vis}}) {
	$self->{Log}->new_tag($Deluge::Log::TAG_PVVD, $item);
    }

    foreach $item (@{$self->{_pos_invis}}) {
	$self->{Log}->new_tag($Deluge::Log::TAG_PIVD, $item);
    }
}

# -------------------------------------------------------------------

sub _link_finder_callback
{
    my ($self, $tag, %attr) = @_;
    my ($item, $val);
    
    foreach $item (keys(%attr)) {
	$val = $attr{$item};
	$val =~ s|%7e|~|ig;

	(($val =~ m|\#|) && ($tag eq "img")) && next;
	$val =~ s|\#.*||ig;

	$self->_cache_link($val, $tag);
    }
}

# -------------------------------------------------------------------

sub _process_links
{
    my ($self) = @_;
    my ($url, $newurl, $tag, $prereq);
    my (@temp_list);
    
    #print STDERR "_process_links() from " . join('; ', caller) . "\n";
    
    while (@{$self->{_page_link_cache_urls}}) {
	$url = shift(@{$self->{_page_link_cache_urls}});
	$tag = shift(@{$self->{_page_link_cache_tags}});

	$newurl = main::url($url, $self->response->base)->abs;

	($self->user->mcp->verbose_logs) &&
	    $self->Log->new_tag($Deluge::Log::TAG_GOTO, $newurl);

	$prereq = Deluge::Prereq->new("GET", $newurl, $self->dest_url,
				      $tag, $self->depth + 1, 1,
				      $self->user,
				      $self->user->get_unique_prereq_id);
	push (@temp_list, $prereq);
    }

    if (@temp_list) {
	@temp_list = sort(@temp_list) if $self->user->ok_to_presort_queue();

	if ($self->user->ok_to_preshuffle_queue) {
	    # op. cit. "perldoc -q shuffle"
	    my ($i, $j);
	    for ($i = @temp_list; --$i; ) {
		$j = int rand ($i+1);
		next if $i == $j;
		@temp_list[$i,$j] = @temp_list[$j,$i];
	    }
	}
	
	while ($prereq = shift(@temp_list)) {
	    $self->user->queue_page($prereq);
	}
    } # end if templist

    if ($self->user->get_images) {
	while (@{$self->{_image_link_cache_urls}}) {
	    $url = shift(@{$self->{_image_link_cache_urls}});
	    $tag = shift(@{$self->{_image_link_cache_tags}});
	    
	    $newurl = main::url($url, $self->response->base)->abs;

	    ($self->user->mcp->verbose_logs) &&
		$self->Log->new_tag($Deluge::Log::TAG_GIMG, $newurl);
	    
	    $prereq = Deluge::Prereq->new("GET", $newurl, $self->dest_url,
					  $tag, $self->depth, 0,
					  $self->user,
					  $self->user->get_unique_prereq_id);
	    $self->user->queue_image($prereq);
	}
    }
} # end _process_links

# -------------------------------------------------------------------

sub _compare_responses
{
    my ($self) = @_;

    ($self->user->mcp->debug_level >= 2) &&
	(print STDERR "Need to write _compare_responses code\n");
}

# -------------------------------------------------------------------

sub _dump_response
{
    my ($self) = @_;
    my ($fh);

    ($self->is_image) && return;
    
    $fh = new FileHandle $self->id . "_client", "w";
    $fh->print($self->dest_url);
    $fh->print("\n------------------------------------\n");
    $fh->print($self->response->request->content);
    $fh->print("\n------------------------------------\n");
    $fh->print($self->response->request->headers->as_string);
    $fh->close;

    $fh = new FileHandle $self->id . "_server.html", "w";
    $fh->print($self->response->content);
    $fh->close;
}

# -------------------------------------------------------------------

sub process_response
{
    my ($self) = @_;

    ($self->executed) ? (return) : ($self->executed(1));

    #print STDERR ( ref($self->response) ? "response: " . ref($self->response) :
    #"no ref" ) . "\n";
    return unless 
	($self->response) and (ref $self->response) and ($self->response->code);
    
    $self->Log->new_tag($Deluge::Log::TAG_DNAM, $self->user->defname);
    $self->Log->new_tag($Deluge::Log::TAG_CODE, $self->response->code);

    ($self->wait_for_click) &&
	($self->Log->new_tag($Deluge::Log::TAG_DTIM, $self->wait_for_click));

    # increment global count, for max_rate
    $self->user->mcp->increment_counter();

    ($self->user->mcp->debug_level >= 1) &&
	($self->response->code >= 400) &&
	    (print STDERR "Code " . $self->response->code . " on URL " .
	     $self->dest_url . "\n");

    ($self->user->mcp->dump_responses) && $self->_dump_response();

    if ($self->user->compare_content){
	($self->expected_code) &&
	    ($self->response->code != $self->expected_code) &&
		$self->Log->new_tag($Deluge::Log::TAG_CDMM,
				    $self->expected_code);
	
	($self->expected_size) &&
	    ($self->content_length != $self->expected_size) &&
		$self->Log->new_tag($Deluge::Log::TAG_LNMM,
				    $self->content_length);

	($self->compare_fname) && ($self->_compare_responses);
    }

    # speedups - don't do work unless we need to
    # no point in parsing redirects, errors, or images for text
    # XXX this was tagged to parse images if $self->user->playback_mode(): why?
    $self->_parse_response() 
	unless ($self->response->code() != 200 or $self->is_image());

    $self->user->cookie_jar->extract_cookies($self->response)
	if $self->user->accept_cookies();

    return if $self->user->playback_mode() and $self->user->all_trans_prepped();

    # Playback mode has to do this to extract session IDs
    if ($self->primary() 
	# don't try to process images, nor redirects, errors, etc
	and not $self->is_image() and $self->response->code() == 200
	# don't look for links unless it's html
	# XXX find a way to parse XML, WML, etc....
	and $self->response->content_type() eq 'text/html') {
        my ($links) =
	  HTML::LinkExtor->new(sub { $self->_link_finder_callback(@_) },
			       $self->response->base);
	# if any links are found, Translator will perform variable
	# substitution via the callback (to User::extract_values...)
        $links->parse($self->response->content);
	# if necessary, check the response for links
	$self->_process_links() 
	    unless $self->user->playback_mode() ;
    }
} # end process_response

# -------------------------------------------------------------------

sub DESTROY
{
    my ($self) = @_;
    my ($item);

    ($self->ignored) && return;
    ($self->Log) || return;
    ($self->dest_url) || return;
    ($self->in_agent) || return;
    
    #print STDERR "Prereq::DESTROY from "
    #. join("; ", caller) . "\n" if $self->user->mcp->debug_level();
    
    unless ($self->failed) {
	($self->response) &&
	    ($self->Log->new_tag($Deluge::Log::TAG_SIZE,
				 $self->content_length));

	($self->req_wait) && ($self->req_wait->get_mark) &&
	    ($self->Log->new_tag($Deluge::Log::TAG_ELAP,
				 $self->req_wait->get_mark));

	if ($self->primary && $self->user && $self->user->page_timer) {
	    ($self->Log->new_tag($Deluge::Log::TAG_PTIM,
				 $self->user->page_timer->get_mark));
	}
    }
    
    return unless $self->user() and $self->user->mcp()
	# skip logging if request falls in user_ramp_time
	and $self->user->mcp->timer->elapsed() > 
	    $self->user->mcp->user_ramp_time();

    # each object represents one request
    # buffer the log entry, in hope of achieving atomic writes
    # XXX lock the logfile as well or instead?
    $self->user->mcp->log_fh->print(join("\n",$self->Log->dump_tags())."\n\n");
    $self->user->mcp->log_fh->flush();
} # end DESTROY

# -------------------------------------------------------------------

sub clone
{
    my ($self, $user, $id) = @_;
    my ($vname, $item);

    my $clone = Deluge::Prereq->new($self->method, $self->dest_url,
				    $self->ref_url, $self->tag,
				    $self->depth, $self->primary,
				    $user, $id);

    foreach $vname (qw(
		       req_content
		       response
		       content
		       content_length
		       expected_code
		       expected_size
		       user_agent_code
		       compare_fname
		       secure_request
		       
		       executed
		       in_agent
		       callback
		       failed
		       ignored
		       
		       delete_cookies_when_registered
		       clear_visited_list_when_registered
		       )) {
	$clone->{$vname} = $self->{$vname};
    }

    foreach $vname (qw(
		       _page_link_cache_urls
		       _page_link_cache_tags
		       _image_link_cache_urls
		       _image_link_cache_tags

		       _pos_vis
		       _pos_invis
		       _neg_vis
		       _neg_invis

		       _ext_headers
		       ext_content
		       )) {
	push @{$clone->{$vname}}, @{$self->{$vname}};
    }
    $clone->replace_params();

    return $clone;
} # end clone

# -------------------------------------------------------------------
# replace contents of Request-Content, extra headers, and
# the dest_url query string with data from request vars, if any.

sub replace_params {
    my $self = shift;

    return unless $self->user->request_vars();

    # the _ext_headers alternate key/value
    # we only want to replace the values
    # XXX make this a hash instead? why reinvent the hash?
    my $i = 0;
    for (@{$self->{_ext_headers}}) {
	next unless $i++ % 2; # skip keys
	$_ = $self->user->request_vars->replace($_);
    }

    for (@{$self->{ext_content}}) {
	$_ = $self->user->request_vars->replace($_);
    }

    my $uri = new URI($self->{dest_url});
    my $query = $uri->query();
    if ($query) {
	$uri->query($self->user->request_vars->replace($query));
	$self->{dest_url} = $uri->as_string();
    }
} # end replace_params

# -------------------------------------------------------------------

sub _initialize
{
    my ($self) = @_;

    # Various data
    $self->{req_content} = "";
    $self->{is_image} = Deluge::Etc::url_is_image($self->dest_url, $self->tag);
    $self->{response} = 0;  # HTTP::Response, from the response callback
    $self->{content} = "";
    $self->{content_length} = 0;
    $self->{expected_code} = 0;
    $self->{expected_size} = 0;
    $self->{user_agent_code} = 0;
    $self->{compare_fname} = "";
    $self->{secure_request}  = 0;
    $self->{wait_for_click} = 0;

    # Pipeline flags
    $self->{executed} = 0;
    $self->{in_agent} = 0;
    $self->{callback} = 0;
    
    $self->{failed} = 0;
    $self->{ignored} = 0;

    # Timers
    $self->{req_wait} = Deluge::Stopwatch->new();
    # $self->{req_elapsed} = -1;
    $self->{req_start_time} = -1;

    # Private
    $self->{_page_link_cache_urls} = [];
    $self->{_page_link_cache_tags} = [];
    $self->{_image_link_cache_urls} = [];
    $self->{_image_link_cache_tags} = [];

    $self->{_pos_vis} = [];
    $self->{_pos_invis} = [];
    $self->{_neg_vis} = [];
    $self->{_neg_invis} = [];

    $self->{_ext_headers} = [];
    $self->{ext_content} = [];

    # Extra fun stuff
    $self->{delete_cookies_when_registered} = 0;
    $self->{clear_visited_list_when_registered} = 0;

    # Log stuff
    $self->{Log} = Deluge::Log->new();

    $self->Log->new_tag($Deluge::Log::TAG_URI,  $self->dest_url);
    $self->Log->new_tag($Deluge::Log::TAG_METH, $self->method);
    $self->Log->new_tag($Deluge::Log::TAG_FROM, $self->ref_url);
    $self->Log->new_tag($Deluge::Log::TAG_HOST, $self->user->mcp->hostname);
    $self->Log->new_tag($Deluge::Log::TAG_PROC, $$);
    $self->Log->new_tag($Deluge::Log::TAG_PREQ, $self->id);
}

# ------ get/set convenience functions ------------------------------
sub Log { my $s = shift; @_ ? ($s->{Log} = shift) : $s->{Log} }
sub callback { my $s = shift; @_ ? ($s->{callback} = shift) : $s->{callback} }
sub clear_visited_list_when_registered { my $s = shift; @_ ? ($s->{clear_visited_list_when_registered} = shift) : $s->{clear_visited_list_when_registered} }
sub compare_fname { my $s = shift; @_ ? ($s->{compare_fname} = shift) : $s->{compare_fname} }
sub content { my $s = shift; @_ ? ($s->{content} = shift) : $s->{content} }
sub content_length { my $s = shift; @_ ? ($s->{content_length} = shift) : $s->{content_length} }
sub delete_cookies_when_registered { my $s = shift; @_ ? ($s->{delete_cookies_when_registered} = shift) : $s->{delete_cookies_when_registered} }
sub depth { my $s = shift; @_ ? ($s->{depth} = shift) : $s->{depth} }
sub dest_url { my $s = shift; @_ ? ($s->{dest_url} = shift) : $s->{dest_url} }
sub executed { my $s = shift; @_ ? ($s->{executed} = shift) : $s->{executed} }
sub expected_code { my $s = shift; @_ ? ($s->{expected_code} = shift) : $s->{expected_code} }
sub expected_size { my $s = shift; @_ ? ($s->{expected_size} = shift) : $s->{expected_size} }
sub failed { my $s = shift; @_ ? ($s->{failed} = shift) : $s->{failed} }
sub ignored { my $s = shift; @_ ? ($s->{ignored} = shift) : $s->{ignored} }
sub id { my $s = shift; @_ ? ($s->{id} = shift) : $s->{id} }
sub in_agent { my $s = shift; @_ ? ($s->{in_agent} = shift) : $s->{in_agent} }
sub is_image { my $s = shift; @_ ? ($s->{is_image} = shift) : $s->{is_image} }
sub method { my $s = shift; @_ ? ($s->{method} = shift) : $s->{method} }
sub primary { my $s = shift; @_ ? ($s->{primary} = shift) : $s->{primary} }
sub ref_url { my $s = shift; @_ ? ($s->{ref_url} = shift) : $s->{ref_url} }
sub req_wait { my $s = shift; @_ ? ($s->{req_wait} = shift) : $s->{req_wait} }
sub response { my $s = shift; @_ ? ($s->{response} = shift) : $s->{response} }
sub tag { my $s = shift; @_ ? ($s->{tag} = shift) : $s->{tag} }
sub user { my $s = shift; @_ ? ($s->{user} = shift) : $s->{user} }
sub user_agent_code { my $s = shift; @_ ? ($s->{user_agent_code} = shift) : $s->{user_agent_code} }
sub wait_for_click { my $s = shift; @_ ? ($s->{wait_for_click} = shift) : $s->{wait_for_click} }

# -------------------------------------------------------------------

sub new
{
    my ($above) = shift;
    my ($class) = ref($above) || $above;
    my ($self) = {};

    bless ($self, $class);

	# -- Public: Main --
	$self->{method} = shift;
	$self->{dest_url} = shift;
	$self->{ref_url} = shift;
	$self->{tag} = shift;
	$self->{depth} = shift;
	$self->{primary} = shift;

	$self->{user} = shift;
	$self->{id} = shift;

	$self->_initialize();
	
    return ($self);
}

# -------------------------------------------------------------------

1;
# end Prereq.pm
