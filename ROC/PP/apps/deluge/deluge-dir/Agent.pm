# -------------------------------------------------------------------
# This is a subclass of LWP::UserAgent.
#
# Deluge normally uses LWP::Parallel::UserAgent.  However, in certain
# cases (the record proxy) it uses this agent instead.  This is for
# simplicity, and problems with POST in the parallelized agent.

use Carp;

use LWP::UserAgent;

# -------------------------------------------------------------------

use strict;

package Deluge::Agent;
@Deluge::Agent::ISA = qw(LWP::UserAgent);

use vars qw($AUTOLOAD);

# -------------------------------------------------------------------

sub redirect_ok
{
	my ($self) = @_;
	
    return 0;
}

# -------------------------------------------------------------------

sub DESTROY
{
    my ($self) = @_;
}

# -------------------------------------------------------------------

sub new
{
    my ($class, %opt) = @_;
    my ($self) = $class->SUPER::new(%opt);

    bless($self, $class);

    return ($self);
}

1;

# end Agent.pm
