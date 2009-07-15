#!/usr/bin/perl

use Time::HiRes;
use strict;

package Deluge::Stopwatch;
local $^W = 1;

# -------------------------------------------------------------------

sub reset
{
    my ($self) = @_;

    $self->{start_time} = [Time::HiRes::gettimeofday()];
    $self->{mark} = 0;
}

# -------------------------------------------------------------------

sub new
{
    my ($above) = shift;
    my ($class) = ref($above) || $above;
    my ($self) = {};
    
    bless ($self, $class);
    $self->reset();
    return ($self);
}

# -------------------------------------------------------------------

sub time
{
    return Time::HiRes::gettimeofday;
}

# -------------------------------------------------------------------

sub elapsed
{
	my ($self) = @_;
	return Time::HiRes::tv_interval($self->{start_time},
					[Time::HiRes::gettimeofday()]);
}

# -------------------------------------------------------------------

sub set_mark
{
    my ($self) = @_;
    $self->{mark} = $self->elapsed();
}

# -------------------------------------------------------------------

sub get_mark
{
    my ($self) = @_;
    return ($self->{mark});
}

# -------------------------------------------------------------------

sub sleep
{
    my ($self, $duration) = @_;
    
    ($duration < 0.15) && return;
    #print STDERR "Sleeping for $duration seconds...\n";
    Time::HiRes::sleep($duration);
}

1;
# end Stopwatch.pm
