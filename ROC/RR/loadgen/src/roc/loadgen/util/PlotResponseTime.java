/*
 * $Id: PlotResponseTime.java,v 1.7 2004/09/21 02:26:15 candea Exp $
 */

package roc.loadgen.util;

import roc.loadgen.rubis.*;

import java.util.*;
import java.io.*;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

/**
 * Plot response time.
 *
 * @version <tt>$Revision: 1.7 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 */

public class PlotResponseTime 
{
    // Log output
    private static Logger log = Logger.getLogger( "PlotResponseTime" );

    int bucketWidth=0;
    int numBuckets=0;
    Bucket[] good;
    long minStart = Long.MAX_VALUE;
    long maxEnd = Long.MIN_VALUE;
    long maxTime = Long.MIN_VALUE;
    LinkedList opsList = new LinkedList();
    private static String[] inputDirs;
    int numOver8Sec=0;
    private static String pwd;

    /**
     * main() body
     **/
    public static void main( String[] args ) 
	throws IOException
    {
	String log4jcfg = System.getProperty( "env.log4j" );
	assert !log4jcfg.equals( "null" );
	PropertyConfigurator.configure( log4jcfg );

	PlotResponseTime prt = new PlotResponseTime();

	prt.processArguments( args );

	pwd = System.getProperty( "user.dir" );

	OpsFile ops = new OpsFile();
	for( int i=0 ; i < inputDirs.length ; i++ )
	{
	    OpsFile.OpsFileRes res = ops.readOperations( pwd + "/" + inputDirs[i] + "/user_operations.tab" );

	    if( prt.minStart > res.minStart )
		prt.minStart = res.minStart;
	    
	    if( prt.maxEnd < res.maxEnd )
		prt.maxEnd = res.maxEnd;
	    
	    if( prt.maxTime < res.maxTime )
		prt.maxTime = res.maxTime;

	    prt.numOver8Sec += res.numOver8Sec;

	    prt.opsList.addAll( res.opsList );  // I really hope Java does this in O(1) time...
	}

	prt.bucketResponses();
	prt.outputBuckets( prt.good );
    }


    /**
     * Bucket responses by their start time.
     **/
    private void bucketResponses( )
    {
	// total number of buckets
	numBuckets = 1 + (int)( (maxEnd-minStart)/bucketWidth );

	// Initialize the buckets
	good = new Bucket[ numBuckets ];
	for( int i=0 ; i < numBuckets ; good[i++] = new Bucket() );

	// Place each operation into the bucket corresp to its start time
	for( Iterator it = opsList.iterator() ; it.hasNext() ; )
	{
	    Operation op = (Operation) it.next();
	    int idx = (int) ( (op.start - minStart) / bucketWidth );
	    good[idx].add( op );
	}
    }


    /**
     * Write buckets out to a file.
     **/
    private void outputBuckets( Bucket[] buckets )
	throws IOException
    {
	FileWriter output = new FileWriter( pwd + "/response_times.tab" );

	output.write("# " + numOver8Sec + " requests took >= 8 sec\n#\n");
	output.write("# Second\tBucket start \t\tNumber of\tAverage \tMax   \n");
	output.write("#       \t(millisec)   \t\tsamples  \t(msec)  \t(msec)\n#\n");

	for( int i=0 ; i < good.length ; i++ )
	{
	    Bucket b = good[i];
	    long startTimeMillis = minStart + i * bucketWidth;
	    String line = "  " + (i*bucketWidth / 1000.0) + "\t\t";
	    line += startTimeMillis + "\t\t";
	    line += b.count + "\t\t";
	    line += b.getAvg() + "\t\t";
	    line += b.max;
	    output.write( line+"\n" );
	}

	output.flush();
	output.close();
    }
	
    /**
     * Read in the ops one line at a time and put them in a list.
     **/
    public void readOperations( String opsFile )
    {
	BufferedReader input = null;
	
	try 
	{
	    input = new BufferedReader( new FileReader(opsFile) );

	    for( String line=input.readLine() ; line!=null ; line=input.readLine() )
	    {
		// skip over comments
		if( line.startsWith( "#" ) )
		    continue;

		// parse the read line
		String[] elements = line.split( "\\s" );
		long startTime = Long.parseLong( elements[0] );
		long endTime = Long.parseLong( elements[1] );
		boolean good = elements[2].equals( "YES" );
		String type = elements[3];

		// add the operation to our list
		Operation op = new Operation( type, startTime, endTime, good );
		opsList.add( op );

		// keep track of minStart, maxEnd
		if( minStart > op.start )
		    minStart = op.start;
	    
		if( maxEnd < op.end )
		    maxEnd = op.end;
	    
		long duration = op.end - op.start;
		if( maxTime < duration )
		    maxTime = duration;
		if( duration >= 8000 )
		    numOver8Sec++;
	    }

	    input.close();

	}
	catch( Exception ex ) {
	    ex.printStackTrace();
	}
	
    }
    

    /**
     * Extract our arguments from the command line.
     **/
    private void processArguments( String[] args )
    {
	if( args.length != 2 )
	{
	    log.fatal( "Usage: PlotResponseTime bucketwidth=<seconds> dirs=<path1>,<path2>,..." );
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

    //---------------------------------------------------------------------------

    private class Bucket 
    {
	public int count=0;
	public int sum=0;
	public int min=0;
	public int max=0;

	public void add( Operation op )
	{
	    count++;
	    int duration = (int) (op.end - op.start);
	    sum += duration;
	    if( min > duration ) min = duration;
	    if( max < duration ) max = duration;
	}

	public int getAvg()  
	{ 
	    if( count > 0 )
		return sum/count;
	    else
		return 0;
	}
    }
}
