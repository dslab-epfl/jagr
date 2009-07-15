use Carp;

# -------------------------------------------------------------------

use strict;

use Deluge::Variable;

package Deluge::Script;

use vars qw($AUTOLOAD);

# -------------------------------------------------------------------
# Internal constants

my ($TAG_hdr) = "DLG-";

# Automatic
my ($TAG_URL) = $TAG_hdr . "URL:";
my ($TAG_MTH) = $TAG_hdr . "Method:";
my ($TAG_UAC) = $TAG_hdr . "User-Agent-Code:";
my ($TAG_RQC) = $TAG_hdr . "Request-Content:";
my ($TAG_RLN) = $TAG_hdr . "Response-Length:";
my ($TAG_CMP) = $TAG_hdr . "Compare:";
my ($TAG_COD) = $TAG_hdr . "Code:";
my ($TAG_MSG) = $TAG_hdr . "Message:";
my ($TAG_IGN) = $TAG_hdr . "Ignored:";

# Hand-inserted
my ($TAG_DCK) = $TAG_hdr . "Delete-Cookies";
my ($TAG_DCH) = $TAG_hdr . "Delete-Cache";

# -------------------------------------------------------------------

sub _get_critical_tags
{
    my ($self) = @_;
	my ($method, $dest_url, $ref_url);
	my ($line);

	$ref_url = "";
	
	foreach $line (@{$self->{_event}}) {
		if ($line =~ m|^$TAG_MTH (.*)|) {
			$method = $1;
			next;
		}
		if ($line =~ m|^$TAG_URL (.*)|) {
			$dest_url = $1;
			next;
		}
		if ($line =~ m|^Referer: (.*)|) {
			$ref_url = $1;
			next;
		}
	}

	(($method) && ($dest_url)) ||
		(die "ERROR: Script event ending at line $self->{_linenum} in file $self->{fname} missing critical element(s)\n");
		
	return ($method, $dest_url, $ref_url);
}

# -------------------------------------------------------------------

sub event_to_prereq
{
    my ($self, $user, $primary) = @_;
	my ($method, $dest_url, $ref_url, $prereq, $line, $request);
	my ($cont_flag) = 0;
	my ($cont_tag) = "";

	(@{$self->{_event}}) || return;
	
	($method, $dest_url, $ref_url) = $self->_get_critical_tags();

	$prereq = Deluge::Prereq->new($method, $dest_url, $ref_url,
								  "a", 0, $primary, $user,
								  $user->get_unique_prereq_id);

	foreach $line (@{$self->{_event}}) {
		my ($car, $cdr);

		if ($cont_flag) {
			$car = $cont_tag;
			$cdr = $line;
#			print STDERR "SET: ($car) ($cdr)\n";
			$cont_flag = 0;
		} else {
			($car, $cdr) = split(' ', $line, 2);
		}

		if ($cdr =~ m|\r|){
			$cdr =~ s|\r|\r\n|;
			$cont_flag = 1;
			$cont_tag = $car;
		}

		(($car eq $TAG_MTH) || ($car eq $TAG_URL)) && next;
		
		if (! ($car =~ m|^$TAG_hdr|)) {
			$car =~ s|:.*||;

#			($user->request_vars) &&
#				($cdr = $user->request_vars->replace($cdr));
			
			$prereq->push_ext_header($car, "$cdr");
			next;
		}

		if ($car eq $TAG_RQC) {
#			($user->request_vars) &&
#				($cdr = $user->request_vars->replace($cdr));
			push(@{$prereq->{ext_content}}, "$cdr");
			next;
		}
		
		if ($car eq $TAG_COD) {
			$prereq->expected_code($cdr);
			next;
		}

		if ($car eq $TAG_RLN) {
			$prereq->expected_size($cdr);
			next;
		}

		if ($car eq $TAG_UAC) {
			$prereq->user_agent_code($cdr);
			next;
		}
		
		if ($car eq $TAG_CMP) {
			$prereq->compare_fname($cdr);
			next;
		}
		
		if ($car eq $TAG_DCK) {
			$prereq->delete_cookies_when_registered(1);
			next;
		}
		
		if ($car eq $TAG_DCH) {
			$prereq->clear_visited_list_when_registered(1);
			next;
		}

		($car eq $TAG_MSG) && next;

		die "ERROR: Unknown tag [$car] in script event ending at line $self->{_linenum} in file $self->{fname}\n";
	}

#	my ($item);
#	print STDERR "FINAL CONTENT\n";
#	foreach $item (@{$prereq->{ext_content}}) {
#		print STDERR "\t$item\n";
#	}
#	print STDERR "END FINAL CONTENT\n";

	$self->next_event;
	return ($prereq);
}

# -------------------------------------------------------------------

sub bad_request_to_event
{
    my ($self, $request) = @_;

	$self->delete_tags;
	
	$self->new_tag($TAG_IGN . " bad scheme");
	$self->new_tag($TAG_URL ." ". $request->url);
	$self->new_tag($TAG_MTH ." ". $request->method);
	$self->new_tag($request->headers_as_string());

	($request->content) && $self->new_tag($TAG_RQC ." ". $request->content);

	$self->dump_tags();
}

# -------------------------------------------------------------------

sub _dump_response_to_file
{
    my ($self, $response, $fname) = @_;
	my ($fh);

	($fh = new FileHandle $fname, "w") ||
		(die "Can't write to " . $fname . " in pid $$\n");

	$fh->print($response->content);
	$fh->flush;
}

# -------------------------------------------------------------------

sub basic_request_to_event
{
    my ($self, $request) = @_;

	local $^W = 0;
	$self->new_tag($TAG_URL ." ". $request->url);
	$self->new_tag($TAG_MTH ." ". $request->method);
	$self->new_tag($TAG_UAC ." ". $request->header("User-Agent"));
	$self->new_tag($request->headers_as_string());

	($request->content) &&
		$self->new_tag($TAG_RQC ." ". $request->content);
	
}

# -------------------------------------------------------------------

sub response_to_event
{
    my ($self, $request, $response, $user) = @_;

	$self->new_tag($TAG_COD ." ". $response->code);
	$self->new_tag($TAG_MSG ." ". $response->message);

	if ((! Deluge::Etc::url_is_image($request->url, "")) &&
		(length($response->content))) {
		my ($fn) = sprintf("%.5d", $user->proxy_counter);

		$self->new_tag($TAG_RLN ." ". length($response->content));
		$self->_dump_response_to_file($response,
									  $user->script_dir."/".$fn);
		$self->new_tag($TAG_CMP ." ". $fn);
	}
	
	$self->dump_tags();
}

# -------------------------------------------------------------------

sub new_tag
{
    my ($self, $tag) = @_;

	chomp($tag);
	push (@{$self->{_event}}, $tag);
}

# -------------------------------------------------------------------

sub delete_tags
{
    my ($self) = @_;

	$self->{_event} = [];
	$self->{_linenum} = 0;
}

# -------------------------------------------------------------------

sub dump_tags
{
    my ($self) = @_;
	my ($tag);

	$self->_open_file();
	
	foreach $tag (@{$self->{_event}}) {
		$self->{_fh}->print("$tag\n");
	}

	$self->{_fh}->print("\n");
	$self->{_fh}->flush;
	$self->{_fh} = 0;

	$self->delete_tags;
}

# -------------------------------------------------------------------

sub next_event_wh
{
    my ($self) = @_;
	my ($started) = 0;
	my ($valid) = 1;

	($self->{_fh}) || ($self->_open_file());
	
	$self->{_event} = [];
	$self->event_is_image(0);
	
	while (1) {
		my ($line);

		if (! ($line = $self->{_fh}->getline)) {
			$started = 1;
			last;
		}

		$self->{_linenum}++;

		($line) && chomp($line);

		if (! $line) { ($started) ? (last) : (next); }

		$started = 1;

		($line =~ m|^$TAG_URL (.*)|) &&
			($self->event_is_image(Deluge::Etc::url_is_image($1, "")));

		if ($line =~ m|^$TAG_COD (.*)|) {
			my ($code) = $1;

			($code >= 400) && (! $self->user->playback_errors) && ($valid = 0);
		}
			
		push (@{$self->{_event}}, $line);
	}

	return ($valid);  # Just because it's valid doesn't mean it exists.
}

# -------------------------------------------------------------------

sub next_event
{
    my ($self) = @_;

	while (1) {
		($self->next_event_wh()) && last;
	}
}

# -------------------------------------------------------------------

sub _open_file
{
	my ($self) = @_;

	($self->{_fh} = new FileHandle $self->fname, $self->mode) ||
		(die "Can't open $self->{fname} with mode $self->{mode} in pid $$\n");
}

# -------------------------------------------------------------------

sub DESTROY
{
    my ($self) = @_;
}

# -------------------------------------------------------------------

sub _initialize
{
    my ($self, $user, $fname, $mode) = @_;

	$self->{fname} = $fname;
	$self->{mode} = $mode;
	$self->{user} = $user;

	$self->{_fh} = 0;
	$self->{_event} = [];
	$self->{_linenum} = 0;

	$self->{event_is_image} = 0;
}

# -------------------------------------------------------------------

sub AUTOLOAD
{
    my ($self) = shift;
    my ($type) = ref($self) || main::confess "$self is not an object\n";
    my ($name) = $AUTOLOAD;

    $name =~ s|.*:||;

    (exists $self->{$name}) || main::confess "$name is not a method here\n";
    ($name =~ m|^_|) && main::confess "Access to method $name denied\n";

    (@_) ? (return $self->{$name} = shift) : (return $self->{$name});
}

# -------------------------------------------------------------------

sub new
{
	my ($above, $user, $fname, $mode) = @_;
    my ($class) = ref($above) || $above;
    my ($self) = {};

    bless($self, $class);

	$self->_initialize($user, $fname, $mode);
	($mode eq "r") && $self->next_event;

    return ($self);
}

# -------------------------------------------------------------------

package main;

1;
