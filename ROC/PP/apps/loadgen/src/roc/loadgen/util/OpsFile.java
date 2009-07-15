/*
 * $Id: OpsFile.java,v 1.2 2004/09/21 08:22:11 candea Exp $
 */

package roc.loadgen.util;

import roc.loadgen.rubis.*;

import java.util.*;
import java.io.*;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

/**
 * Perform input/output on files with user operations.
 *
 * @version <tt>$Revision: 1.2 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 */

public class OpsFile 
{
    // Log output
    private static Logger log = Logger.getLogger( "OpsFile" );

    /**
     * Read in the ops one line at a time and put them in a list.
     **/
    public OpsFileRes readOperations( String opsFile )
    {
	BufferedReader input = null;
	OpsFileRes res = new OpsFileRes();
	
	try 
	{
	    input = new BufferedReader( new FileReader(opsFile) );

	    for( String line=input.readLine() ; line!=null ; line=input.readLine() )
	    {
		// skip over comments
		if( line.startsWith( "#" ) )
		    continue;

		// parse the read line
		String[] elements = line.split( "\t" );

		for( int k=0 ; k < elements.length ; k++)
		    log.debug("elements[" + k + "]=" + elements[k]);
		long startTime = Long.parseLong( elements[0] );
		long endTime = Long.parseLong( elements[1] );
		boolean good = elements[2].equals( "YES" );
		String type = elements[4];
		log.debug(" Type = " + type );

		// add the operation to our list
		Operation op = new Operation( type, startTime, endTime, good );
		res.opsList.add( op );

		// keep track of minStart, maxEnd
		if( res.minStart > op.start )
		    res.minStart = op.start;
	    
		if( res.maxEnd < op.end )
		    res.maxEnd = op.end;
	    
		long duration = op.end - op.start;
		if( res.maxTime < duration )
		    res.maxTime = duration;
		if( duration >= 8000 )
		    res.numOver8Sec++;
	    }

	    input.close();

	}
	catch( Exception ex ) {
	    ex.printStackTrace();
	}

	return res;
    }

    public class OpsFileRes 
    {
	public LinkedList opsList = new LinkedList();
	public long minStart = Long.MAX_VALUE;
	public long maxEnd   = Long.MIN_VALUE;
	public long maxTime  = Long.MIN_VALUE;
	public int numOver8Sec = 0;
    }
}
