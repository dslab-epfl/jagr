package Deluge::Log;

use Carp;

use strict;
local $^W = 1;

use Data::Dumper qw/Dumper/;
use Deluge::Histogram;

use vars qw($AUTOLOAD);

# -------------------------------------------------------------------
# External constants

# -- Generated at traversal time --
#    -- Before processing --
$Deluge::Log::TAG_URI  = 101;  # Requested URL
$Deluge::Log::TAG_METH = 102;  # Method of transmit
$Deluge::Log::TAG_OCNT = 103;  # Outgoing content
$Deluge::Log::TAG_FROM = 104;  # Refered from URL
$Deluge::Log::TAG_HOST = 105;  # Host ID
$Deluge::Log::TAG_PROC = 106;  # Process ID
$Deluge::Log::TAG_PREQ = 107;  # User/Prereq ID
$Deluge::Log::TAG_DNAM = 108;  # User_def name
$Deluge::Log::TAG_TIME = 109;  # Time when event started, for merging logs

#    -- After processing (not errors) --
$Deluge::Log::TAG_FWDP = 201;  # If forwarding occurred, list the steps
$Deluge::Log::TAG_CODE = 202;  # Response code
$Deluge::Log::TAG_GOTO = 203;  # Refers to non-image URLs
$Deluge::Log::TAG_GIMG = 204;  # Refers to image URLs
$Deluge::Log::TAG_ELAP = 205;  # Response time
$Deluge::Log::TAG_SIZE = 206;  # Response size
$Deluge::Log::TAG_PTIM = 207;  # Response time for entire page
$Deluge::Log::TAG_DTIM = 208;  # Time from "want to click" to "click"

#    -- Discovered errors --
$Deluge::Log::TAG_BREQ = 301;  # Error: Couldn't complete request
$Deluge::Log::TAG_XMME = 301;  # Error: META tag missing HTTP-EQUIV
#        or NAME entry
$Deluge::Log::TAG_XNRD = 302;  # Error: No response data
$Deluge::Log::TAG_XUKT = 303;  # Error: Unknown tag found
$Deluge::Log::TAG_PVVD = 304;  # Error: Positive vis validation denied
#        from regexp
$Deluge::Log::TAG_NVVR = 305;  # Error: Negative vis validation received
#        from regexp
$Deluge::Log::TAG_PIVD = 306;  # Error: Positive invis validation denied
#        from regexp
$Deluge::Log::TAG_NIVR = 307;  # Error: Negative invis validation received
#        from regexp
$Deluge::Log::TAG_CDMM = 308;  # Error: Expected code from playback
#        script doesn't match actual code
$Deluge::Log::TAG_LNMM = 309;  # Error: Expected length from response
#        doesn't match expected from playback

#    -- Comments --
$Deluge::Log::TAG_CMNT = 401;  # Comments

# -------------------------------------------------------------------

sub _initialize
{
    my ($self) = @_;
    
    # Log stuff
    $self->{tags} = [];

    $self->{linenum} = 0;

    $self->{event_car} = [];
    $self->{event_cdr} = [];

    $self->{fh} = 0;
}

# -------------------------------------------------------------------

sub new_tag
{
    my ($self, $car, $cdr) = @_;

    ($cdr) || return;
    push (@{$self->{tags}}, "$car $cdr");
}

# -------------------------------------------------------------------

sub dump_tags
{
    my ($self) = @_;

    return (@{$self->{tags}});
}

# -------------------------------------------------------------------

sub prep_log_for_eval
{
    my ($self, $filename) = @_;

    ($self->{fh} = FileHandle->new) ||
	(main::usage("Can't create new filehandle"));

    ($self->{fh}->open($filename)) ||
	(main::usage("Can't open log file [$filename]"));
}


# -------------------------------------------------------------------

sub reset_log
{
    my ($self) = @_;

    $self->{fh}->close();
    $self->{linenum} = 0;
}

# -------------------------------------------------------------------

sub get_next_event
{
    my ($self) = @_;
    my ($started) = 0;
    my ($line, $fh);

    $fh = $self->{fh};
    $self->{event_car} = [];
    $self->{event_cdr} = [];
    
    while ($line = <$fh>) {
	my ($car, $cdr);
	
	$self->{linenum} ++;

	chomp($line);
	if (! $line) { ($started) ? (last) : (next); }

	$started = 1;

	($car, $cdr) = split(' ', $line, 2);
	push (@{$self->{event_car}}, $car);
	push (@{$self->{event_cdr}}, $cdr);
    }

    return ($started);
}

# -------------------------------------------------------------------

# given one log event, parse it and return a list of elements
sub parse_event {
    my $self = shift;
    my ($car, $cdr, $url, $dnam, $wall, $code, $elap, $ptim, $dtim, $size);

    while (@{$self->{event_car}}) {
	$car = shift(@{$self->{event_car}});
	$cdr = shift(@{$self->{event_cdr}});

	if ($car == $Deluge::Log::TAG_URI ) { $url  = $cdr; next; }
	if ($car == $Deluge::Log::TAG_DNAM) { $dnam = $cdr; next; }
	if ($car == $Deluge::Log::TAG_TIME) { $wall = $cdr; next; }
	if ($car == $Deluge::Log::TAG_CODE) { $code = $cdr; next; }
	if ($car == $Deluge::Log::TAG_ELAP) { $elap = $cdr; next; }
	if ($car == $Deluge::Log::TAG_PTIM) { $ptim = $cdr; next; }
	if ($car == $Deluge::Log::TAG_DTIM) { $dtim = $cdr; next; }
	if ($car == $Deluge::Log::TAG_SIZE) { $size = $cdr; next; }
    }

    # XXX skip events that preceeded the end of the warmup period
    

    return ($url, $dnam, $wall, $code, $elap, $ptim, $dtim, $size);
} # parse_event

# -------------------------------------------------------------------

sub evaluate
{
    my ($self, $filename, $opts_href) = @_;
    my ($url_time_h, $page_time_h, $size_h, $errors_h, $user_wait_h,
	$url_time_over_time_h, $page_time_over_time_h, $user_wait_over_time_h);
    
    # summary stats will go here
    my %summary = (start => time(), stop => 0,
		   responsetime => [],
		   bytes => 0, http => 0
		   );
    # if needed, per-url and per-user stats will go here
    my (%url_stats, %user_stats);

    if ($opts_href->{histograms}) {
	my $vb = $opts_href->{hist_value_buckets};
	my $tb = $opts_href->{hist_time_buckets};

	$url_time_h = Deluge::Histogram->new($vb, 0);
	$page_time_h = Deluge::Histogram->new($vb, 0);
	$size_h = Deluge::Histogram->new($vb, 0);
	$user_wait_h = Deluge::Histogram->new($vb, 0);
	$errors_h = Deluge::Histogram->new($vb, 1);
	
	$url_time_over_time_h = Deluge::Histogram->new($tb, 1);
	$page_time_over_time_h = Deluge::Histogram->new($tb, 1);
	$user_wait_over_time_h = Deluge::Histogram->new($tb, 1);
    } # end if histograms

    $self->prep_log_for_eval($filename);

    # First pass: Find various counts, minimums and maximums, and so forth.
    my ($car, $cdr, $url, $user, $code, $elap, $ptim, $dtim, $size, $wall);
    while ($self->get_next_event() ) {
	($url, $user, $wall, $code, $elap, $ptim, $dtim, $size) =
	    $self->parse_event();
	
	next unless $wall and $code;
	
	$summary{http} += 1; # http ops count

	if ($size) {
	    $size_h && $size_h->set_min_max_indirectly($size);
	    $summary{bytes} += $size;
	}
	$page_time_h && $ptim && $page_time_h->set_min_max_indirectly($ptim);
	$user_wait_h && $dtim && $user_wait_h->set_min_max_indirectly($dtim);
	if ($elap) {
	    $url_time_h && $url_time_h->set_min_max_indirectly($elap);
	    # store as msec
	    push @{ $summary{responsetime} }, 1000*$elap;
	    # update global start/stop times
	    my $stop = $wall + $elap;
	    $summary{start} = $wall if $summary{start} > $wall;
	    $summary{stop}  = $stop if $summary{stop}  < $stop;
	}

	if ($opts_href->{histograms}) {
	    $url_time_over_time_h->set_min_max_indirectly($wall);
	    $page_time_over_time_h->set_min_max_indirectly($wall);
	    $user_wait_over_time_h->set_min_max_indirectly($wall);
	    $errors_h->set_min_max_indirectly($wall);
	}

	(exists $summary{$code}) || ($summary{$code} = 0);
	$summary{$code}++;

	if ($opts_href->{per_url}) {
	    (exists $url_stats{$url}) ||
		($url_stats{$url}->{$code} = 0);
	    $url_stats{$url}->{$code} ++;

	    defined $url_stats{$url}->{responsetime} or
		$url_stats{$url}->{responsetime} = ();
	    push @{ $url_stats{$url}->{responsetime} }, 1000*$elap;
	}

	if ($opts_href->{per_user}) {
	    (exists $user_stats{$user}) ||
		($user_stats{$user}->{$code} = 0);
	    $user_stats{$user}->{$code} ++;

	    defined $user_stats{$user}->{responsetime} or
		$user_stats{$user}->{responsetime} = ();
	    push @{ $user_stats{$user}->{responsetime} }, 1000*$elap;
	}
	
    } # end while log events
    
    if ($opts_href->{histograms}) {
	$url_time_h->prep_for_data();
	$page_time_h->prep_for_data();
	$size_h->prep_for_data();
	$errors_h->prep_for_data();
	$user_wait_h->prep_for_data();

	$url_time_over_time_h->prep_for_data();
	$page_time_over_time_h->prep_for_data();
	$user_wait_over_time_h->prep_for_data();

	# Second pass: Find various counts, minimums and maximums, etc.
	# only necesary for histograms
	$self->prep_log_for_eval($filename);

	while ($self->get_next_event) {
	    ($url, $user, $wall, $code, $elap, $ptim, $dtim, $size) =
		$self->parse_event();
	
	    ($wall && $code) || next;
	    
	    $size && $size_h->insert_value_in_bucket(1, $size);

	    if ($elap) {
		$url_time_h->insert_value_in_bucket(1, $elap);
		$url_time_over_time_h->insert_value_in_bucket($elap, $wall);
	    }

	    if ($ptim) {
		$page_time_h->insert_value_in_bucket(1, $ptim);
		$page_time_over_time_h->insert_value_in_bucket($ptim, $wall);
	    }

	    if ($dtim) {
		$user_wait_h->insert_value_in_bucket(1, $dtim);
		$user_wait_over_time_h->insert_value_in_bucket($dtim, $wall);
	    }

	    ($code >= 400) && $errors_h->insert_value_in_bucket(1, $wall);
	}

	$url_time_h->prep_for_dump();
	$page_time_h->prep_for_dump();
	$size_h->prep_for_dump();
	$errors_h->prep_for_dump();
	$user_wait_h->prep_for_dump();

	$url_time_over_time_h->prep_for_dump();
	$page_time_over_time_h->prep_for_dump();
	$user_wait_over_time_h->prep_for_dump();
	
	$url_time_h->dump_results("Load time per URL histogram", 1);
	$url_time_over_time_h->
	    dump_results("Average load time per URL over time", 3);

	$page_time_h->dump_results("Load time per page histogram", 1);
	$page_time_over_time_h->
	    dump_results("Average load time per page over time", 3);

	$user_wait_h->dump_results("User wait time histogram", 1);
	$user_wait_over_time_h->dump_results("User wait time over time", 3);

	$size_h->dump_results("URL bytes histogram", 1);
	$errors_h->dump_results("Errors over time histogram", 1);
    } # end if histograms

    my $seconds = $summary{stop}-$summary{start};
    my ($i90th, $responsetime_avg);
    if ($opts_href->{per_url}) {
	print "RESPONSE CODE STATISTICS (per URL):\n";
	for my $item (sort(keys(%url_stats))) {
	    print "\t$item\n";
	    $url_stats{$item}->{responsetime} =  
		[ sort {$a <=> $b} @{ $url_stats{$item}->{responsetime} } ];
	    $i90th = int(0.90*(scalar @{ $url_stats{$item}->{responsetime} }));
	    $responsetime_avg = 0;
	    for (@{ $url_stats{$item}->{responsetime} }) { 
		$responsetime_avg += $_ }
	    if (scalar @{ $url_stats{$item}->{responsetime} }) {
		$responsetime_avg /= 
		    scalar @{ $url_stats{$item}->{responsetime} };
	    } else {
		$responsetime_avg = -1;
	    }
	    
	    foreach (keys(%{$url_stats{$item}})) {
		next unless /\d\d\d/; # skip non-code data
		print "\t\t$_: " .
		    $url_stats{$item}->{$_} . "\n";
	    }
	    printf "\t\taverage response time (ms): %.f\n", $responsetime_avg;
	    printf "\t\t90th-percentile response time (ms): %.f\n", 
	    $url_stats{$item}->{responsetime}->[$i90th];
	}
	print "\n";
    }

    if ($opts_href->{per_user}) {
	print "RESPONSE CODE STATISTICS (per USER):\n";
	for my $item (sort(keys(%user_stats))) {
	    print "\t$item\n";
	    $user_stats{$item}->{responsetime} =  
		[ sort {$a <=> $b} @{ $user_stats{$item}->{responsetime} } ];
	    $i90th = int(0.90*(scalar @{ $user_stats{$item}->{responsetime} }));
	    $responsetime_avg = 0;
	    for (@{ $user_stats{$item}->{responsetime} }) { 
		$responsetime_avg += $_ }
	    if (scalar @{ $user_stats{$item}->{responsetime} }) {
		$responsetime_avg /= 
		    scalar @{ $user_stats{$item}->{responsetime} };
	    } else {
		$responsetime_avg = -1;
	    }
	    
	    foreach (keys(%{$user_stats{$item}})) {
		next unless /\d\d\d/; # skip non-code data
		print "\t\t$_: " .
		    $user_stats{$item}->{$_} . "\n";
	    }
	    printf "\t\taverage response time (ms): %.f\n", $responsetime_avg;
	    printf "\t\t90th-percentile response time (ms): %.f\n", 
	    $user_stats{$item}->{responsetime}->[$i90th];
	}
	print "\n";
    }

    # HTTP response counts, and other totals
    my %format = (
		  title => "%-27s%12s%10s\n",
		  count => "%-27s%12d\n",
		  ii =>    "%-27s%12.f%10.f\n",
		  i =>     "%-27s%12.f\n",
		  f2 =>    "%-27s%15.2f\n"
		  );
    printf $format{title}, "EVENT", "COUNT", "";
    printf $format{title}, "^^^^^", "^^^^^", "";
    for my $item (sort(keys(%summary))) {
	next unless $item =~ /\d\d\d/; # skip non-code data
	printf $format{count}, "HTTP $item", $summary{$item};
    }
    print "\n";

    # summary stats: response time, throughput
    # find 90th percentile for responsetime...
    $summary{responsetime} = [ sort {$a <=> $b} @{ $summary{responsetime} } ];
    $i90th = int(0.90*(scalar @{ $summary{responsetime} }));
    $responsetime_avg = 0;
    for (@{ $summary{responsetime} }) { $responsetime_avg += $_ }
    if (scalar @{ $summary{responsetime} }) {
	$responsetime_avg /= scalar @{ $summary{responsetime} };
    } else {
	$responsetime_avg = -1;
    }
    printf $format{title}, "METRIC (UNITS)", "AVERAGE", "90%";
    printf $format{title}, "^^^^^^^^^^^^^^", "^^^^^^^", "^^^";
    printf $format{ii}, "Response Time (ms)",
    $responsetime_avg, $summary{responsetime}->[$i90th];
    printf $format{f2}, "Throughput (http ops/s)",
    ($summary{http}/$seconds);
    printf $format{i}, "Throughput (kb/s)",
    (8*$summary{bytes}/(1000*$seconds));
    print "\n";
} # end evaluate

# -------------------------------------------------------------------

sub new
{
    my ($above) = shift;
    my ($class) = ref($above) || $above;
    my ($self) = {};

    bless ($self, $class);

    $self->_initialize();
    
    return ($self);
}

1;
# end Log.pm
