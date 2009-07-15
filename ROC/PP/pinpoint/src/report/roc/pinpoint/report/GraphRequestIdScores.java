/**
    Copyright (C) 2004 Emre Kiciman and Stanford University

    This file is part of Pinpoint

    Pinpoint is free software; you can distribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    Pinpoint is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Pinpoint; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**/
package roc.pinpoint.report;

import java.util.*;
import java.io.*;

public class GraphRequestIdScores {

    static int[] goodbuckets;
    static int[] badbuckets;

    static Set badrequestids;

    public static void main( String[] argv ) {

	try {

	if( argv.length != 2 ) {
	    System.err.println( "Usage: java roc.pinpoint.report.GraphRequestIdScores [injecteddir] [analysisfile]" );
	    return;
	}

	String injecteddir = argv[0];
	String logfile = argv[1];

	badrequestids = new HashSet();

	String basename = 
	    logfile.substring( 0, 
			       logfile.length() - ".log.analysis".length() );

	basename = basename.substring( basename.lastIndexOf( "/" ));

	File injectedfile = new File( injecteddir + "/" + 
				      basename + ".injected" );

	loadInjectedIds( injectedfile );

	goodbuckets = new int[400];
	badbuckets = new int[400];

	LineNumberReader lnr =
	    new LineNumberReader( new FileReader( new File(logfile )));

	while( true ) {
	    String line = lnr.readLine();
	    if( line == null )
		break;
	    processLine( line );
	}

	printBuckets( "goodrequests.trc", goodbuckets );
	printBuckets( "badrequests.trc", badbuckets );

	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
    }

    public static final void loadInjectedIds( File f ) throws IOException {

	LineNumberReader lnr;

	try {
	    lnr = new LineNumberReader( new FileReader( f ));
	}
	catch( FileNotFoundException e ) {
	    System.err.println( "could not find injected id file: " + f );
	    System.err.println( "\tContinuing, assuming no injected requests" );
	    return;
	}


	while( true ) {
	    String line = lnr.readLine();
	    if( line == null )
		break;
	    badrequestids.add( line );
	    //System.err.println( "badrequestid: " + line );
	}
       
    }

    public static final void printBuckets( String fname, int[] buckets ) 
	throws IOException {
	File f = new File( fname );
	PrintWriter pw = new PrintWriter( new FileWriter( f ));

	for( int i=0; i<buckets.length; i++ ) {
	    pw.println( Double.toString((double)i/buckets.length ) +
			"\t" + buckets[i] );
	}

	pw.close();
    }

    public static final void processLine( String line ) {
	
	if( !line.startsWith( "pcfgdetector:" ))
	    return;

	int idx1 = "pcfgdetector: ".length();
	int idx4 = line.indexOf( "; probability= " );
	int idx5 = idx4 + "; probability= ".length();

	if( idx4 == -1 )
	    return;
	
	String requestid = line.substring( idx1, idx4 );
	String probability = line.substring( idx5 );

	double p = Double.parseDouble( probability );

	if( badrequestids.contains( requestid )) {
	    int i = (int)(p * (badbuckets.length-1));
	    badbuckets[i]++;
	}
	else {
	    int i = (int)(p * (goodbuckets.length-1));
	    goodbuckets[i]++;
	}
	
	//System.err.println( requestid + "," + threshold + "," + probability );
    }

}
