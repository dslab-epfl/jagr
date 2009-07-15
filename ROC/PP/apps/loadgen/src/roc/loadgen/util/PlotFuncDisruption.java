/*
 * $Id: PlotFuncDisruption.java,v 1.3 2004/09/21 22:42:03 candea Exp $
 */

package roc.loadgen.util;

import roc.loadgen.rubis.*;

import java.util.*;
import java.io.*;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

/**
 * Plot functionality disruption.
 *
 * @version <tt>$Revision: 1.3 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 */

public class PlotFuncDisruption
{
    // Log output
    private static Logger log = Logger.getLogger( "PlotFuncDisruption" );

    // Directories where to find the input operations files
    private static String[] inputDirs;

    // The list of all operations
    private static LinkedList opsList = new LinkedList();

    // The earliest start time of an operation
    private static long minStart = Long.MAX_VALUE;

    // The latest start time of an operation
    private static long maxEnd = Long.MIN_VALUE;

    // A map of bucket arrays (one array for each group of operation types)
    private static HashMap bucketMap = new HashMap( TransitionFunction.states.length );

    // The width of a bucket
    private static int bucketWidth;

    // The number of buckets in a bucket array
    private static int numBuckets;

    private static boolean[] bid_buy_sell;
    private static boolean[] browse_view;
    private static boolean[] user_acct;
    private static boolean[] search;
    private static boolean[] misc;

    /**
     * main() body
     **/
    public static void main( String[] args ) 
	throws IOException
    {
	String log4jcfg = System.getProperty( "env.log4j" );
	assert !log4jcfg.equals( "null" );
	PropertyConfigurator.configure( log4jcfg );

	PlotFuncDisruption pfd = new PlotFuncDisruption();

	// Process the command-line arguments
	pfd.processArguments( args );

	// Read in the operations and update min/max
	OpsFile ops = new OpsFile();
	for( int i=0 ; i < inputDirs.length ; i++ )
	{
	    OpsFile.OpsFileRes res = ops.readOperations( System.getProperty("user.dir") + 
							 "/" + inputDirs[i] + "/user_operations.tab" );
	    if( pfd.minStart > res.minStart )
		pfd.minStart = res.minStart;
	    
	    if( pfd.maxEnd < res.maxEnd )
		pfd.maxEnd = res.maxEnd;
	    
	    pfd.opsList.addAll( res.opsList );  // I really hope Java does this in O(1) time...
	}

	numBuckets = 1 + msecToBucketIdx(maxEnd);

// 	pfd.initBucketArrays_full();
// 	pfd.analyzeOperations();
// 	pfd.outputBuckets_full();

	pfd.initBucketArrays_categories();
	pfd.analyzeOperations();
	pfd.outputBuckets_categories();
    }


    /**
     * Initialize the map of bucket arrays; should be called prior to filling the buckets.
     **/
    private void initBucketArrays_categories()
    {
	// SelectCategorySellItem, SellItemForm, RegisterItem, BuyNow,
	//    StoreBuyNow, PutBid, StoreBid
	bid_buy_sell = new boolean[ numBuckets ];
	Arrays.fill( bid_buy_sell, true );
	bucketMap.put( (Object) TransitionFunction.SELECT_CAT.getName(), (Object) bid_buy_sell );
	bucketMap.put( (Object) TransitionFunction.SELL_ITEM.getName(), (Object) bid_buy_sell );
	bucketMap.put( (Object) TransitionFunction.REG_ITEM.getName(), (Object) bid_buy_sell );
	bucketMap.put( (Object) TransitionFunction.BUY_NOW.getName(), (Object) bid_buy_sell );
	bucketMap.put( (Object) TransitionFunction.STORE_BUY.getName(), (Object) bid_buy_sell );
	bucketMap.put( (Object) TransitionFunction.PUT_BID.getName(), (Object) bid_buy_sell );
	bucketMap.put( (Object) TransitionFunction.STORE_BID.getName(), (Object) bid_buy_sell );

	// BrowseCategoriesInRegion, BrowseCategories, BrowseRegions,
	//    Browse, ViewBidHistory, ViewItem, ViewUserInfo
	browse_view = new boolean[ numBuckets ];
	Arrays.fill( browse_view, true );
	bucketMap.put( (Object) TransitionFunction.BR_CAT_REG.getName(), (Object) browse_view );
	bucketMap.put( (Object) TransitionFunction.BROWSE_CAT.getName(), (Object) browse_view );
	bucketMap.put( (Object) TransitionFunction.BROWSE_REG.getName(), (Object) browse_view );
	bucketMap.put( (Object) TransitionFunction.BROWSE.getName(), (Object) browse_view );
	bucketMap.put( (Object) TransitionFunction.VIEW_HIST.getName(), (Object) browse_view );
	bucketMap.put( (Object) TransitionFunction.VIEW_ITEM.getName(), (Object) browse_view );
	bucketMap.put( (Object) TransitionFunction.VIEW_USER.getName(), (Object) browse_view );
	
	// Login, LoginUser, Logout, Register, RegisterUser, AboutMe,
	//    PutComment, StoreComment
	user_acct = new boolean[ numBuckets ];
	Arrays.fill( user_acct, true );
	bucketMap.put( (Object) TransitionFunction.LOGIN.getName(), (Object) user_acct );
	bucketMap.put( (Object) TransitionFunction.LOGIN_USR.getName(), (Object) user_acct );
	bucketMap.put( (Object) TransitionFunction.LOGOUT.getName(), (Object) user_acct );
	bucketMap.put( (Object) TransitionFunction.REGISTER.getName(), (Object) user_acct );
	bucketMap.put( (Object) TransitionFunction.REG_USER.getName(), (Object) user_acct );
	bucketMap.put( (Object) TransitionFunction.ABOUT_ME.getName(), (Object) user_acct );
	bucketMap.put( (Object) TransitionFunction.PUT_COMMENT.getName(), (Object) user_acct );
	bucketMap.put( (Object) TransitionFunction.STORE_COMM.getName(), (Object) user_acct );

	// SearchItemsInCategory, SearchItemsInRegion
	search = new boolean[ numBuckets ];
	Arrays.fill( search, true );
	bucketMap.put( (Object) TransitionFunction.SEARCH_CAT.getName(), (Object) search );
	bucketMap.put( (Object) TransitionFunction.SEARCH_REG.getName(), (Object) search );

	// Home, Back, AbandonSession
	misc = new boolean[ numBuckets ];
	Arrays.fill( misc, true );
	bucketMap.put( (Object) TransitionFunction.HOME.getName(), (Object) misc );
	bucketMap.put( (Object) TransitionFunction.BACK.getName(), (Object) misc );
	bucketMap.put( (Object) TransitionFunction.ABANDON.getName(), (Object) misc );
    }


    private void initBucketArrays_full()
    {
	for( int i=0 ; i < TransitionFunction.states.length ; i++ )
	{
	    boolean[] buckets = new boolean[ numBuckets ];
	    Arrays.fill( buckets, true );
	    Object stateName = (Object) TransitionFunction.states[i].getName();
	    bucketMap.put( (Object) stateName, (Object) buckets );
	}
    }


    /**
     * 
     **/
    private void analyzeOperations ()
    {
	log.debug( "analyzeOperations" );

	// Each bad operation sets all buckets it spans to false
	for( Iterator it = opsList.iterator() ; it.hasNext() ; )
	{
	    Operation op = (Operation) it.next();
	    if( op.isOK )
		continue;
	    
	    log.debug( "Operation=" + op );
	    boolean[] bucketArray = (boolean[]) bucketMap.get( (Object)op.type );

	    for( int i = msecToBucketIdx(op.start) ; i <= msecToBucketIdx(op.end) ; i++ )
	    {
		log.debug( "i=" + i );
		bucketArray[i] = false;
	    }
	}
    }


    private static int msecToBucketIdx( long msec )
    {
	return (int) (msec-minStart)/bucketWidth;
    }


    /**
     * Write buckets out to a file.
     **/
    private void outputBuckets_full()
	throws IOException
    {
	for( Iterator it = bucketMap.keySet().iterator() ; it.hasNext() ; )
	{
	    String stateName = (String) it.next();
	    boolean[] buckets = (boolean[]) bucketMap.get( (Object)stateName );
	    
	    FileWriter output = new FileWriter( System.getProperty("user.dir") + 
						"/" + stateName + ".tab" );

	    output.write("# Bucket\tUp/Down\t\n");
	    output.write("# (sec) \t(1/0) \n#\n");

	    for( int i=0 ; i < buckets.length ; i++ )
	    {
		int bucketSec = (int) (i * bucketWidth / 1000);
		String upDown = (buckets[i] ? "1" : "0");
		output.write( bucketSec + "\t" + upDown + "\n" );
	    }

	    output.flush();
	    output.close();
	}

    }
	

    /**
     * Write buckets out to a file.
     **/
    private void outputBuckets_categories()
	throws IOException
    {
	writeBucksCat( bid_buy_sell, "bid_buy_sell" );
	writeBucksCat( browse_view, "browse_view" );
	writeBucksCat( user_acct, "user_acct" );
	writeBucksCat( search, "search" );
	writeBucksCat( misc, "misc" );
    }


    private void writeBucksCat( boolean[] bucks, String file )
	throws IOException
    {
	FileWriter output = new FileWriter( System.getProperty("user.dir") + 
					    "/" + file + ".categ" );

	output.write("# Bucket\tUp/Down\t\n");
	output.write("# (sec) \t(1/0) \n#\n");

	for( int i=0 ; i < bucks.length ; i++ )
	{
	    int bucketSec = (int) (i * bucketWidth / 1000);
	    String upDown = (bucks[i] ? "1" : "0");
	    output.write( bucketSec + "\t" + upDown + "\n" );
	}

	output.flush();
	output.close();
    }


    /**
     * Extract our arguments from the command line.
     **/
    private void processArguments( String[] args )
    {
	if( args.length != 2 )
	{
	    log.fatal( "Usage: PlotFuncDisruption bucketwidth=<seconds> dirs=<path1>,<path2>,..." );
	    System.exit( -1 );
	}

	for( int i=0 ; i < args.length ; i++ )
	{
	    String arg = args[i];
	    int x = arg.indexOf( "=" );

	    if( arg.startsWith( "bucketwidth=" ))
	    {
		this.bucketWidth = 1000 * Integer.parseInt( arg.substring( 1+x, arg.length() ));
	    }
	    else if( arg.startsWith( "dirs=" ))
	    {
		String files = arg.substring( 1+x, arg.length() );
		this.inputDirs = files.split( "," );
	    }
	    else
	    {
		log.fatal( "Unknown argument " + arg );
		System.exit( -1 );
	    }
	}
    }
}
