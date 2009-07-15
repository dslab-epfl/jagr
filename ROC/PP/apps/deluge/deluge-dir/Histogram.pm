use Carp;

# -------------------------------------------------------------------

use strict;

package Deluge::Histogram;
use vars qw($AUTOLOAD);

# -------------------------------------------------------------------

sub set_min_max_indirectly
{
    my ($self, $val) = @_;

	($self->{_min} == -1) && ($self->{_min} = $val);
	($self->{_max} == -1) && ($self->{_max} = $val);

	($val < $self->{_min}) && ($self->{_min} = $val);
	($val > $self->{_max}) && ($self->{_max} = $val);
}

# -------------------------------------------------------------------

sub set_min_max_directly
{
    my ($self, $min, $max) = @_;

	$self->{_min} = $min;
	$self->{_max} = $max;
}

# -------------------------------------------------------------------

sub prep_for_data
{
    my ($self) = @_;
	my ($i);

	if ($self->{_max} == $self->{_min}) {
		$self->{_valid} = 0;
		return;
	}

	if ($self->{_shift_range}) {
		$self->{_max} -= $self->{_min};
		$self->{_shift} -= $self->{_min};
		$self->{_min} = 0;
	}

	for ($i=0; $i<$self->{_num_buckets}; $i++) {
		push (@{$self->{_buckets}}, 0);
		push (@{$self->{_bucket_counters}}, 0);
	}

	$self->{_inv_range} = 1.0 / ($self->{_max} - $self->{_min});
}

# -------------------------------------------------------------------

sub insert_value_in_bucket
{
    my ($self, $value, $bucket) = @_;
	my ($b);

	($self->{_shift_range}) && ($bucket += $self->{_shift});
	
	$self->{_sum} += $value;
	$self->{_count} ++;

	$b = int(($bucket - $self->{_min}) * $self->{_inv_range} *
			 ($self->{_num_buckets} + 1));

	($b == $self->{_num_buckets}) && ($b --);

	@{$self->{_buckets}}[$b] += $value;
	@{$self->{_bucket_counters}}[$b] ++;
}

# -------------------------------------------------------------------

sub prep_for_dump
{
    my ($self) = @_;
	my ($i);

	if (! $self->{_count}) {
		$self->{_valid} = 0;
		return;
	}

	$self->{_avg} = $self->{_sum} / $self->{_count};

	for ($i=0; $i<$self->{_num_buckets}; $i++){
		( (! @{$self->{_bukcet_counters}}[$i]) or @{$self->{_bucket_counters}}[$i] == 0) ?
			(@{$self->{_bucket_avg}}[$i] = 0) :
				(@{$self->{_bucket_avg}}[$i] =
				 @{$self->{_buckets}}[$i] / @{$self->{_bucket_counters}}[$i]);
	}
}

# -------------------------------------------------------------------

# Mode 1: Show _bucket_counters
# Mode 2: Show _buckets
# Mode 3: Show _buckets / _bucket_counters

sub dump_results
{
    my ($self, $desc, $mode) = @_;
	my ($i);

	print "$desc:\n";
	
	if (! $self->{_valid}) {
		print "\tResults are null\n\n";
		return;
	}
	
	print "\t$self->{_count} events, summing to $self->{_sum}\n";

	print "\tRange";
	($self->{_shift_range}) && (print " (shifted by $self->{_shift})");
	print ": $self->{_min} --> $self->{_max}\n";

	print "\tAverage: $self->{_avg}\n";
	print "\tData:\n";

	for ($i=0; $i<$self->{_num_buckets}; $i++) {
		my ($localmin) =
			($i / $self->{_num_buckets}) * ($self->{_max} - $self->{_min});

		printf "\t\t%.2f:\t", $localmin;
		
		if ($mode == 1) {
			print @{$self->{_bucket_counters}}[$i] . "\n";
		}

		if ($mode == 2) {
			print @{$self->{_buckets}}[$i] . "\n";
		}

		if ($mode == 3) {
			printf "%.4f\n", @{$self->{_bucket_avg}}[$i];
		}	
	}

	print "\n";
}

# -------------------------------------------------------------------

sub DESTROY
{
    my ($self) = @_;
}

# -------------------------------------------------------------------

sub _initialize
{
    my ($self, $nb, $sr) = @_;

	$self->{_num_buckets} = $nb;
	$self->{_shift_range} = $sr;

	$self->{_shift} = 0;
	$self->{_min} = -1;
	$self->{_max} = -1;
	$self->{_inv_range} = 0;
	$self->{_sum} = 0;
	$self->{_count} = 0;
	$self->{_avg} = 0;

	$self->{_buckets} = [];
	$self->{_bucket_counters} = [];
	$self->{_bucket_avg} = [];

	$self->{_valid} = 1;
}

# -------------------------------------------------------------------

sub new
{
	my ($above, $num_buckets, $shift_range) = @_;
    my ($class) = ref($above) || $above;
    my ($self) = {};

    bless($self, $class);

	$self->_initialize($num_buckets, $shift_range);

    return ($self);
}

# -------------------------------------------------------------------

package main;

1;
