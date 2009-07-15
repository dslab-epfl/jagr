use Carp;

use FileHandle;
use Sys::Hostname;

# -------------------------------------------------------------------

use strict;

package Deluge::Config;
use vars qw($AUTOLOAD);

# -------------------------------------------------------------------

sub remove_edge_whitespace
{
    my ($self, $line) = @_;

    $line && ($line =~ s|^\s+||);
    $line && ($line =~ s|\s+$||);
    $line && (return ($line));
}

# -------------------------------------------------------------------

sub error
{
    my ($self, $complaint) = @_;
    &main::usage("$complaint on line $self->{linenum}");
}

# -------------------------------------------------------------------

sub get_next_line
{
    my ($self) = @_;
    my ($line);

    while (@{$self->{_cfgfile}}) {
	$self->{linenum}++;

	$line = $self->remove_edge_whitespace(shift(@{$self->{_cfgfile}}));
	($line) || next;
	$line =~ s|\#.*$||;
	($line) || next;

	return ($line);
    }

    return "";
}

# -------------------------------------------------------------------

sub get_pair
{
	my ($self, $line) = @_;
	# limit split to 2, so top_url can contain a query string
	my ($car, $cdr) = split('=', $line, 2);

	$car = $self->remove_edge_whitespace($car);
	$cdr = $self->remove_edge_whitespace($cdr);

	return ($car, $cdr);
}

# -------------------------------------------------------------------

sub get_switch
{
    my ($self, $vname, $switch) = @_;

	if ($switch ne "") {
		($switch =~ m|^[1TtYy]|) && (return 1);
		($switch =~ m|^[0FfNn]|) && (return 0);
	}

	main::usage("Unknown value for [$vname] on line " .
				 "$self->{linenum} (should be {0,1})");
}

# -------------------------------------------------------------------

sub get_number
{
    my ($self, $vname, $val) = @_;

	if ($val ne "") {
		($val =~ m|^\d+\.*\d*$|) && (return $val);
		($val =~ m|^\d*\.\d+$|) && (return $val);
	}

	main::usage("Unknown value for [$vname] on line ".
				 "$self->{linenum} (should be number)");
}

# -------------------------------------------------------------------

sub get_list
{
    my ($self, $vname) = @_;
	my (@alist) = ();
	my ($line, $item);

	while ($line = $self->get_next_line()) {
		($line eq "END") && (return (@alist));

		foreach $item (split(' ', $line)) {
			push (@alist, $item);
		}
	}

	main::usage("No END tag found for [$vname] list");
}

# -------------------------------------------------------------------

sub get_hash
{
    my ($self, $vname) = @_;
	my (%ahash) = ();
	my ($line, $car, $cdr);

	while ($line = $self->get_next_line()) {
		($line eq "END") && (return (%ahash));

		($car, $cdr) = split(' ', $line);

		(exists($ahash{$car})) &&
			(main::usage("Duplicate CAR in [$vname] at line " .
						  "$self->{linenum}"));

		($cdr) ||
			(main::usage("Unknown value for CDR in [$vname] " .
						  "at line $self->{linenum}"));
			
		$ahash{$car} = $cdr;
	}

	main::usage("No END tag found for [$vname] paired list");
}

# -------------------------------------------------------------------

sub DESTROY
{
    my ($self) = @;
}

# -------------------------------------------------------------------

sub _initialize
{
    my ($self, $fname) = @_;
	my ($CFGSTREAM);

	# -- Private --
	$self->{_cfgfile} = [];

	# -- Public --
	$self->{linenum} = 0;
	$self->{config_file} = $fname;

	(-f $self->config_file) ||
		(main::usage("Can't find config file [$self->{config_file}]"));
	(-r $self->config_file) ||
		(main::usage("Can't read config file [$self->{config_file}]"));

	($CFGSTREAM = FileHandle->new()) ||
		main::usage("Can't create new filehandle");

	$CFGSTREAM->open($self->config_file) ||
		(main::usage("Can't open config file [$self->{config_file}]"));
	@{$self->{_cfgfile}} = <$CFGSTREAM>;
	$CFGSTREAM->close();
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
	my ($above, $fname) = @_;
	my ($class) = ref($above) || $above;
	my ($self) = {};
	
	bless ($self, $class);

	$self->_initialize($fname);

	return ($self);
}

# -------------------------------------------------------------------

package main;

1;
