#!/usr/bin/perl -w

use strict;

my %faumachine;
my %fautmpdir;

sub start_fau_machine($$$) {

    my $node=$_[0];
    my $machine=$_[1];
    my $vmdir=$_[2];

    ## TODO
    ## copy the vm subdir to a new, temp dir.  of course, the vm subdir
    ## should not have a writable disk image, but symlink to a write-protected
    ## image.

    ## TODO figure out the tmpdir ... make sure its local, not on nfs;
    $tmpdir = something;

    system( "cp -R $vmdir $tmpdir" );

    $faumachine{$node} = $machine;
    $fautmpdir{$node} = $tmpdir;

    spawn_process( "injector -B $tmpdir", $machine );
    
    ## TODO SLEEP FOR A LONG TIME (better yet, since we're starting multiple faumachines, spawn all faumachines in parallel, then sleep...

    ### MAJOR TODO
    spawn_process( "

}





#
#
#  injector arguments are:
#
#     Fault         Param1           Param2             Param3
#    ------------  ---------------  -----------------  --------------
#    CpuBF         CPU (int)         Register (int)    Bit (int)
#    DiskBad       Disk (int)        ---               ---
#    DiskBlockBad  Disk (int)        Start block (int) End block (int)
#    MemBF         Address (int)     Bit (int)         ---
#    NetRecv       Interface (int)   Drop % (int)      ---
#    NetSend       Interface (int)   Drop % (int)      ---
#
# CpuBF: Flip the selected bit in the selected register on the selected CPU.
#        See --help-registers for details.
# DiskBad: Cause all I/O operations on the specified disk to fail.  The disk is
#          specified by a character, which corresponds to /dev/hd%c on the VM.
# DiskBlockBad: Cause all I/O operations on a range of blocks on the specified
#               disk to fail.  The disk is specified as in the DiskBad fault.
# MemBF: Flip the selected bit (0-7) at the specified memory address.
# NetRecv: Randomly drop the specified percentage of packets received by the
#          selected interface.
# NetSend: Randomly drop the specified percentage of packets sent by the
#          selected interface.
#
#
#  possible registers values for CPUBF are::
#
# 
#    Number     x86 Register
#    ------     ------------
#     0         eax
#     1         ebx
#     2         ecx
#     3         edx
#     4         edi
#     5         esi
#     6         ebp
#     7         esp
#     8         eflags
#     9         cs
#    10         ds
#    11         es
#    12         fs
#    13         gs
#    14         ss
#
#
sub inject_fau_machine_error($$) {

    my $node=$_[0];
    my $fault=$_[1];

    my $injectoptions = "";

    if( $fault eq "CpuBF" ) {
	$injectoptions="-f CpuBF 0 0 0";  ### TODO  add a way to make it a random register and random bit being flipped...
    }
    else if( $fault eq "DiskBad" ) {
	$injectoptions="-f DiskBad 0";  ### TODO: is disk id a number or a character?
    }
    else if( $fault eq "DiskBlockBad" ) {
	$injectoptions="-f DiskBlockBad 0 $STARTBLOCK $ENDBLOCK"; ## TODO set startblock and endblock somehow (random?)
    }
    else if( $fault eq "MemBF" ) {
	$injectoptions="-f MemBF $ADDRESS $BIT"; ## TODO set address and bit to flip somehow (random?)
    }
    else if( $fault eq "NetRecv" ) {
	$injectoptions="-f NetRecv $IF $DROPRATE"; # TODO set interface and droprate somehow
    }
    else if( $fault eq "NetSend" ) {
	$injectoptions="-f NetSend $IF $DROPRATE"; # TODO set interface and drop rate somehow.
    }

    $machine = fau_get_physical_machine($node);

    spawn_process($PATHTOINJECTOR . " " . $injectoptions
}


return 1;
