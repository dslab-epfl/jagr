#!/usr/bin/perl

# CVS
# $Id: compare_table.pl,v 1.1 2004/03/19 02:11:43 fjk Exp $
#

if($#ARGV != 1 && $#ARGV != 2){
	print "usage ./compare_table.pl workload_file generated_file2 [row]\n";
	exit;
}

if(! -r $ARGV[0]){
	print "usage ./compare_table.pl workload_file generated_file2 [row]\n";
	exit;
}

if(! -r $ARGV[1]){
	print "usage ./compare_table.pl workload_file generated_file2 [row]\n";
	exit;
}

$row_num = 29;
if($#ARGV == 2){
	if($ARGV[2] =~ m/[^0-9]/){
		print "usage ./compare_table.pl workload_file generated_file2 [row]\n";
		exit;
	}
	$row_num = $ARGV[2]-1;
}
	 
#open workload file
$workload_file = $ARGV[0];
open(IN, $workload_file);


$title = <IN>;
<IN>;
<IN>;
<IN>;
$i=0;
@workload_array;
while($i<=$row_num){
	$line = <IN>;
	$j=0;
	#print "$line\n";
	while($j<=$row_num && $line =~ m/[0-9\.]+/) {
		$workload_array[$i*($row_num+1)+$j] = $&;
		#print "$workload_array[$i*($row_num+1)+$j]\n";
		$line = "$'";
		$j++;
	}
	if($j < $row_num){
		print "work_load file inappropriate : insufficient columns\n";
		exit;
	}
	$i++;
}
if($i < $row_num){
	print "work_load file inappropriate : insufficient rows\n";
	exit;
}

close(IN);
		
#test print workload
#for($i=0; $i<=$row_num; $i++){
#	for($j=0; $j<=$row_num; $j++){
#		print "$workload_array[$i*($row_num+1)+$j]\t";
#	}
#	print "\n"
#}




#open generated file
$generated_file = $ARGV[1];
open(IN, $generated_file);

$names = <IN>;
$names =~ s/\t//;
#print "$names\n";
$i=0;
while($i<=$row_num && $names =~ m/[\t\n]/){
    $name_array[$i] = "$`";
    $names = "$'";
    $i++;
}
if($i < $row_num){
    print "work_load file inappropriate : insufficient columns(name)\n";
    exit;
}

#test name output
#for($i=0; $i<=$row_num; $i++){
 #   print "$name_array[$i]\t";
#}
#print "\n";

$i=0;
@generated_array;
while($i<=$row_num){
	$line = <IN>;
	$j=0;
	#print "$line\n";
	while($j<=$row_num && $line =~ m/[0-9\.]+/) {
		$generated_array[$i*($row_num+1)+$j] = $&;
		#print "$generated_array[$i*($row_num+1)+$j]\n";
		$line = "$'";
		$j++;
	}
	if($j < $row_num){
		print "work_load file inappropriate : insufficient columns\n";
		exit;
	}
	$i++;
}
if($i < $row_num){
	print "work_load file inappropriate : insufficient rows\n";
	exit;
}

close(IN);
		
#test print generated
#for($i=0; $i<=$row_num; $i++){
	#for($j=0; $j<=$row_num; $j++){
		#print "$generated_array[$i*($row_num+1)+$j]\t";
	#}
	#print "\n";
#}

#open out file
open(OUT, ">compared_table.txt");

#compare
print(OUT "\t");
for($j=0; $j<=$row_num; $j++){
    print(OUT "$name_array[$j]\t");
}
print(OUT "\n");
for($i=0; $i<=$row_num; $i++){
    print(OUT "$name_array[$i]\t");
    for($j=0; $j<=$row_num; $j++){
	if($generated_array[$i*($row_num+1)+$j] != 0 && $workload_array[$i*($row_num+1)+$j] == 0){
	    print(OUT "false \| $workload_array[$i*($row_num+1)+$j] \| $generated_array[$i*($row_num+1)+$j]");
	} else {
	    print(OUT "$workload_array[$i*($row_num+1)+$j] \| $generated_array[$i*($row_num+1)+$j]");
	}
	print (OUT "\t");
    }
    print (OUT "\n");
}

close(OUT);
	    
