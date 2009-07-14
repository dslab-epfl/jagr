#!/usr/bin/perl

# CVS
# $Id: mktbl.pl,v 1.4 2004/03/30 18:42:58 fjk Exp $
#

#if you want to have debug message set 1
$debug = 1;

#check arguments
if($#ARGV != 1){
    print "usage : ./mktbl.pl file_name config_file_name\n";
    exit;
}

if(! -r $ARGV[0]){
    print "usage : ./mktbl.pl file_name config_file_name\n";
    exit;
}
	
if(! -r $ARGV[1]){
    print "usage : ./mktbl.pl file_name config_file_name\n";
    exit;
}

#extract config_info from config_file(like rubis.sess_state.properties)
$session_num;
$numuser;
open(IN, $ARGV[1]);
while($line_conf = <IN>){
    if($line_conf =~ m/workload_number_of_clients_per_node/){
        if($' =~ m/=(\s)*/ && $' =~ m/\n/){
	    $session_num = $`;
        }
    }
    if($line_conf =~ m/database_number_of_users/){
        if($' =~ m/=(\s)*/ && $' =~ m/\n/){
	    $numuser = $`;
        }
    }
}
close(IN);

#open output file
$out = $ARGV[0];
$out =~ s/\.html/\.txt/;
open(OUT, "> $out");

#prepare array of page names.
@def_array = default_array();

#prepare array of matrix
# : value of (To, from)
@double_array;
for($j=0; $j<=$#def_array; $j++){
    for($k=0; $k<=$#def_array; $k++){
	$double_array[$j*($#def_array+1)+$k] = 0;
    }
}

#read input file per session, per user.

for($I=0; $I<$session_num; $I++){
    for($i=0; $i<$numuser; $i++){
	#open input file.
	open(IN, $ARGV[0]);
	
	#set variables for pattern matching
	#ex: UserSession0, user3
	#$array[$i] = "user".$i;
	#$user = $array[$i];
	$user = "user".$i;
	$session = "UserSession".$I;
    
	# current referes to current request.
	# next referes to next request. 
	$current = "initial";
	$next ="end";
	while ($line = <IN>) {
	    #if pattern matches, then it is 
	    #next request.
	    if ($line =~ m/$session / &&
		$line =~ m/$user /){
		if($line =~ m/going to /){
		    $string = "$'";
		    if($string =~ m/\</){ 
		    #deal w/tag.
			$next = $`;
		    }
		    else {
			$next = $string;
		    } 
		} elsif ($line =~ m/Starting a new user session/){
		    $next = "initial";
		} else {
		    if($debug == 1){
		#	print "Unknown line : $line";
		    }
		    next;
		}
		
		# if (current, next) matches 
		# workload tables (To, from)
		# then incliment the correspondent
		# matrix value.
		$flag = 0;
		for($j=0; $j<=$#def_array;$j++){
		    #if($def_array[$j] =~ /(\s*)$current(\s*)/){
		    if($def_array[$j] eq $current){
			for($k=0; $k<=$#def_array; $k++){
			    if($def_array[$k] eq $next){
				#if($def_array[$k] =~ /(\s*)$next(\*s)/){
				$double_array[$j*($#def_array+1)+$k]++;
				$flag = 1;
				last;
			    }
			}
			last;
		    }
		}
		# if no match,  stdout.
		if($debug == 1 && $current ne "initial" && $next ne "end" && $next ne "initial"){
		    if($flag == 0){
			print "Didn't match prepared (To,From) combination\n";
			print "current is ".$current."\n";
			print "next is ".$next."\n\n";
		    }
		}
		
		#set next to current and go to next loop.
		$current = $next;
	    }
	}
	
	#close input.
	close(IN);
    }
}

#print results to file.
print(OUT "\t");
for($j=0; $j<=$#def_array; $j++){
    print(OUT $def_array[$j]);
    print(OUT "\t");
}
print(OUT "\n");
for($k=0; $k<=$#def_array;$k++){
    print(OUT $def_array[$k]);
    print(OUT "\t");
    for($j=0; $j<=$#def_array; $j++){
	print(OUT $double_array[$j*($#def_array+1)+$k]);
	print(OUT "\t");
    }
    print(OUT "\n");
}	

#close output.
close(OUT);

#end message.
print "successfully generated file : ";
print "$out\n";
#this subroutine returns workload tables row(array)
sub default_array {
	@array = ("Home", "Register", "RegisterUser in DB", "Browse", "BrowseCategories","SearchItemsInCategory","BrowseRegions", "BrowseCategoriesInRegion", "SearchItemsInRegion", "ViewItem", "ViewUserInfo", "ViewBidHistory", "BuyNowAuth", "BuyNow", "StoreBuyNow", "PutBidAuth", "PutBid", "StoreBid", "PutCommentAuth", "PutComment", "StoreComment", "Sell", "SelectCategoryToSellItem", "SellItemForm", "RegisterItem", "AboutMe (auth form)", "AboutMe", "Login", "LoginUser", "Logout");
	return @array;
}
	
