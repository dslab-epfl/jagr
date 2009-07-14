#!/usr/bin/perl

open(GSTAT,"gstat | grep Berkeley.EDU | head -n 10 -|");

@machines;

while(<GSTAT>) {
    chomp;
#    print( "I saw $_ in the gstat output!" );
    @machines = (@machines,$_);
}

close(GSTAT);

open(MACHINECONFIG,">gstat.machine");
print MACHINECONFIG "client=",$machines[0],"\n";
#print MACHINECONFIG "frontend=",$machines[1],"\n";
print MACHINECONFIG "frontend=",$machines[1] . " " . $machines[2] . " " . $machines[3],"\n";
print MACHINECONFIG "backend=",$machines[4] . " " . $machines[5] . " " . $machines[6],"\n";
print MACHINECONFIG "dbmachine=",$machines[7],"\n";
print MACHINECONFIG "pinpoint=",$machines[8],"\n";
close(MACHINECONFIG);






