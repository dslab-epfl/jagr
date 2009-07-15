use Carp;

# -------------------------------------------------------------------

use strict;

package Deluge::Translator;
use vars qw();

# -------------------------------------------------------------------

sub clone
{
    my ($self) = @_;
    my ($clone) = Deluge::Translator->new;

    $clone->header($self->header);
    $clone->regexp($self->regexp);
    $clone->footer($self->footer);
    $clone->update_per_page($self->update_per_page);

    return ($clone);
}

# -------------------------------------------------------------------

sub reset { (shift)->{val} = "" }

# -------------------------------------------------------------------

sub extract_value_from_url
{
    my ($self, $url) = @_;

    ($self->val) && (! $self->update_per_page) && return;
    
    my ($hdr) = $self->header;
    my ($rgx) = $self->regexp;
    my ($ftr) = $self->footer;

    if (! $hdr) {
	if ($url =~ m|($rgx)$ftr|) {
	    $url =~ s|$ftr.*||;
	    $url =~ m|($rgx)$|;
	    $self->{val} = $1;
	    print STDERR "EXTRACTED.f: " . $self->val . "\n";
	}
    } elsif (! $ftr) {
	if ($url =~ m|$hdr($rgx)|) {
	    $url =~ s|.*$hdr||;
	    $url =~ m|^($rgx)|;
	    $self->{val} = $1;
	    print STDERR "EXTRACTED.h: " . $self->val . "\n";
	}
    } else {
	if ($url =~ m|$hdr($rgx)$ftr|) {
	    $url =~ s|.*$hdr||;
	    $url =~ s|$ftr.*||;
	    $self->val($url);
	    print STDERR "EXTRACTED.b: " . $self->val . "\n";
	}
    }
}

# -------------------------------------------------------------------

sub insert_value_into_url
{
    my ($self, $url) = @_;

    ($self->val) || return ($url);
    ($url) || return;

    my ($hdr) = $self->header;
    my ($rgx) = $self->regexp;
    my ($ftr) = $self->footer;
    my ($val) = $self->val;

    my ($unhdr) = $hdr;
    $unhdr =~ s|\\||g;
    
    my ($unftr) = $ftr;
    $unftr =~ s|\\||g;

    if (! $hdr) {
	print STDERR "ATTEMPTING.f: $url\n";
	
	if ($url =~ m|$rgx$ftr|) {
	    $url =~ s|$rgx$ftr|$val$unftr|;
	    print STDERR "INSERTED.f: $url\n";
	}
    } elsif (! $ftr) {
	print STDERR "ATTEMPTING.h: $url\n";

	if ($url =~ m|$hdr$rgx|) {
	    $url =~ s|$hdr$rgx|$unhdr$val|;
	    print STDERR "INSERTED.h: $url\n";
	}
    } else {
	print STDERR "ATTEMPTING.b: $url\n";
	
	if ($url =~ m|$hdr$rgx$ftr|) {
	    $url =~ s|$hdr$rgx$ftr|$unhdr$val$unftr|;
	    print STDERR "INSERTED.b: $url\n";
	}
    }

    print STDERR "\n";

    return ($url);
}

# -------------------------------------------------------------------

sub _check_config_info
{
    my ($self, $tln) = @_;

    my ($in) = "in translator beginning at line $tln";
    
    (! $self->header) && (! $self->footer) &&
	(main::usage("At least one of [header, footer] necessary $in"));

    ($self->regexp) ||
	(main::usage("Missing mandatory [regexp] assignment $in"));
}

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
	foreach $tag (qw(update_per_page)) {
	    if ($car eq $tag) {
		$self->{$tag} = $cfg->get_switch($car, $cdr);
		next LINE;
	    }
	}

	# String values
	foreach $tag (qw(header
			 regexp
			 footer)) {
	    if ($car eq $tag) {
		$self->{$tag} = $cdr;
		next LINE;
	    }
	}

	$cfg->error("Unknown variable [$car]");
    }

  main::usage("No END tag found for user_def starting at line $tln");
}

# -------------------------------------------------------------------

sub _initialize
{
    my ($self) = @_;

    $self->{header} = "";
    $self->{footer} = "";
    $self->{regexp} = "";
    $self->{update_per_page} = 0;

    $self->{val} = "";
}

# ---------------- get/set functions --------------------------------
# model:
#sub xxx { my $s = shift; @_ ? ($s->{xxx} = shift) : $s->{xxx} }
sub footer { my $s = shift; @_ ? ($s->{footer} = shift) : $s->{footer} }
sub header { my $s = shift; @_ ? ($s->{header} = shift) : $s->{header} }
sub regexp { my $s = shift; @_ ? ($s->{regexp} = shift) : $s->{regexp} }
sub update_per_page { my $s = shift; @_ ? ($s->{update_per_page} = shift) : $s->{update_per_page} }
sub val { my $s = shift; @_ ? ($s->{val} = shift) : $s->{val} }

# -------------------------------------------------------------------

sub new
{
    my ($above, $cfg) = @_;
    my ($class) = ref($above) || $above;
    my ($self) = {};

    bless($self, $class);

    $self->_initialize();

    if ($cfg) {
	my ($toplinenum) = $cfg->linenum;
	$self->_read_config_file($cfg);
	$self->_check_config_info($toplinenum);
    }

    return $self;
}

# -------------------------------------------------------------------

1;

# end Translator.pm
