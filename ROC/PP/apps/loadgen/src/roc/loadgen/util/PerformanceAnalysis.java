/*
 * $Id: PerformanceAnalysis.java,v 1.1 2004/09/22 03:43:18 candea Exp $
 */

package roc.loadgen.util;

import roc.loadgen.rubis.*;

import java.util.*;
import java.io.*;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

/**
 * Analyze performance.
 *
 * @version <tt>$Revision: 1.1 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 */

public class PerformanceAnalysis
{
    // Log output
    private static Logger log = Logger.getLogger( "PerformanceAnalysis" );

    // The list of all operations
    private static LinkedList opsList = new LinkedList();

    // Start time of measurement period
    long startMsec;

    // End time of measurement period
    long endMsec;

    // Name of file from which to read the user operations
    String fileName;

    /**
     * main() body
     **/
    public static void main( String[] args ) 
	throws IOException
    {
	String log4jcfg = System.getProperty( "env.log4j" );
	assert !log4jcfg.equals( "null" );
	PropertyConfigurator.configure( log4jcfg );

	PerformanceAnalysis pa = new PerformanceAnalysis();
	pa.doMain( args );
    }


    public void doMain( String[] args )
	throws IOException
    {
	// Process the command-line arguments
	processArguments( args );
	log.info("Analyzing " + fileName + " in interval [" +
		 startMsec/1000 + "," + endMsec/1000 + "] sec");

	// Read in the operations and update startMsec and endMsec
	OpsFile ops = new OpsFile();
	OpsFile.OpsFileRes res = ops.readOperations( fileName );

	startMsec += res.minStart;
	endMsec += res.minStart;

	opsList.addAll( res.opsList );  // I hope Java does this in O(1) time...
	
	printData();
    }


    void printData()
    {
	float numOps=0;
	float totalLatency=0;

	for( Iterator it = opsList.iterator() ; it.hasNext() ; )
	{
	    Operation op = (Operation) it.next();
	    assert op.isOK;

	    if( op.start >= startMsec  &&  op.end <= endMsec )
	    {
		numOps++;
		totalLatency += (op.end - op.start);
	    }
	}

	log.info( "Total operations: " + numOps );
	log.info( "Total operation time: " + (totalLatency/1000) + " sec" );
	log.info( "Throughput: " + (1000 * numOps / (endMsec - startMsec)) + " req/sec" );
	log.info( "Average latency: " + (totalLatency/numOps) + " msec" );
    }
    

    /**
     * Extract our arguments from the command line.
     **/
    private void processArguments( String[] args )
    {
	if( args.length != 3 )
	{
	    log.fatal( "Usage: PerformanceAnalysis start=<seconds> end=<seconds> file=<path_to_file>" );
	    System.exit( -1 );
	}

	for( int i=0 ; i < args.length ; i++ )
	{
	    String arg = args[i];
	    int x = arg.indexOf( "=" );

	    if( arg.startsWith( "start=" ))
	    {
		this.startMsec = 1000 * Integer.parseInt( arg.substring( 1+x, arg.length() ));
	    }
	    else if( arg.startsWith( "end=" ))
	    {
		this.endMsec = 1000 * Integer.parseInt( arg.substring( 1+x, arg.length() ));
	    }
	    else if( arg.startsWith( "file=" ))
	    {
		this.fileName = arg.substring( 1+x, arg.length() );
	    }
	    else
	    {
		log.fatal( "Unknown argument " + arg );
		System.exit( -1 );
	    }
	}
    }
}
