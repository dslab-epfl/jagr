use Carp;

# -------------------------------------------------------------------

use strict;

use Deluge::Config;

package Deluge::Variable;

use vars qw($AUTOLOAD);

# -------------------------------------------------------------------
# Internal Constants

my (%SYSTEM_VARS);

# -------------------------------------------------------------------

sub _set_system_vars
{
    my $self = shift;

    $SYSTEM_VARS{"__HOST"} = $self->user->mcp->hostname;
    $SYSTEM_VARS{"__PID"} = $$;
    $SYSTEM_VARS{"__ID"} = $self->user->id;
    $SYSTEM_VARS{"__ITER"} = $self->user->iters;
}

# -------------------------------------------------------------------

sub _dump_vars
{
    my ($self) = @_;
	my ($i);

	for ($i=0; $i<=($#{@{$self->{_find}}}); $i++) {
		print STDERR "VAR: " .
			@{$self->{_find}}[$i] . " ||| ". 
			@{$self->{_repl}}[$i] . " ||| ". 
			@{$self->{_fixed}}[$i] . "\n";
	}
}

# -------------------------------------------------------------------

sub prep
{
    my $self = shift;
    my ($find, $fixed, $j, $val, @fixed);

    $self->_set_system_vars();
    $self->{_fixed} = [];

    for (my $i=0; $i<(scalar @{$self->{_find}}); $i++) {
	$fixed = $self->{_repl}->[$i];
	# URI-escape the @ character - XXX others?
	$fixed =~ s|\@|\%40|g;
	
	for $j (keys(%SYSTEM_VARS)) {
	    $fixed =~ s|$j|$SYSTEM_VARS{$j}|g;
	}
	for ($j=0; $j<$i; $j++) {
	    $find = $self->{_find}->[$j];
	    $val = $self->{_fixed}->[$j];
	    $fixed =~ s|$j|$val|g;
	}
	push @{$self->{_fixed}}, $fixed;
    }
    $self->{_prepped} = 1;
}

# -------------------------------------------------------------------

sub replace
{
    my ($self, $line) = @_;
    $self->prep() unless $self->{_prepped};

    my ($find, $fixed);

    for (my $i=0; $i<scalar(@{$self->{_find}}); $i++) {
	$find = $self->{_find}->[$i];
	$fixed = $self->{_fixed}->[$i];
	$line =~ s|$find|$fixed|g;
    }
    return $line;
}

# -------------------------------------------------------------------

sub push_pair
{
    my ($self, $find, $repl) = @_;

	push(@{$self->{_find}}, "$find");
	push(@{$self->{_repl}}, "$repl");
}

# -------------------------------------------------------------------

sub clone
{
    my ($self) = @_;
	my ($clone) = Deluge::Variable->new($self->user);
	my ($i);

	for ($i=0; $i<(scalar @{$self->{_find}} ); $i++) {
		my ($find) = @{$self->{_find}}[$i];
		my ($repl) = @{$self->{_repl}}[$i];

		$clone->push_pair($find, $repl);
	}

	return ($clone);
}

# -------------------------------------------------------------------

sub _read_config_file
{
    my ($self, $cfg, $tln) = @_;
	my ($line, $tag);

  LINE:
	while ($line = $cfg->get_next_line) {
		($line eq "END") && (return);
		
		my ($car, $cdr) = split('=', $line);

		$car = $cfg->remove_edge_whitespace($car);
		$cdr = $cfg->remove_edge_whitespace($cdr);

		$self->push_pair($car, $cdr);
	}

	main::usage("No END tag found for variable list starting at line $tln");
}

# -------------------------------------------------------------------

sub DESTROY
{
    my ($self) = @_;
}

# -------------------------------------------------------------------

sub _initialize
{
    my ($self, $user) = @_;

	$self->{_prepped} = 0;

	$self->{user} = $user;
	
	$self->{_find} = [];  # Parallel lists.
	$self->{_repl} = [];
	$self->{_fixed} = [];
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
	my ($above, $user, $cfg) = @_;
	my ($class) = ref($above) || $above;
	my ($self) = {};

	bless ($self, $class);

	$self->_initialize($user);
	($cfg) && ($self->_read_config_file($cfg, $cfg->linenum));
	
	return ($self);
}

# -------------------------------------------------------------------

package main;

1;
