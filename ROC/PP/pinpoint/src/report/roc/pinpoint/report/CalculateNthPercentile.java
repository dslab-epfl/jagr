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

public class CalculateNthPercentile {

    static SortedSet requestids;

    public static void main( String[] argv ) {

	try {

	if( argv.length != 1 ) {
	    System.err.println( "Usage: java roc.pinpoint.report.CalculateNthPercentile [analysisfile]" );
	    return;
	}

	String logfile = argv[0];

	requestids = new TreeSet();

	String basename = 
	    logfile.substring( 0, 
			       logfile.length() - ".log.analysis".length() );

	basename = basename.substring( basename.lastIndexOf( "/" ));

	LineNumberReader lnr =
	    new LineNumberReader( new FileReader( new File(logfile )));

	while( true ) {
	    String line = lnr.readLine();
	    if( line == null )
		break;
	    processLine( line );
	}

	}
	catch( Exception e ) {
	    e.printStackTrace();
	}


	printPercentiles();

    }


    static double[] percentilesToPrint = {
	1.0,
	0.999,
	0.995,
	0.99,
	0.95,
	0.90
    };

    public static void printPercentiles() {

	ArrayList a = new ArrayList( requestids );

	System.err.println( "a.size = " + a.size() );
	System.out.println( "Percentile,Value" );

	for( int i=0; i<percentilesToPrint.length; i++ ) {
	    double p = percentilesToPrint[i];

	    int idx = (int)(p * ((double)(a.size()-1)));
	    
	    System.err.println( "idx = " + idx );

	//	    double prob = ((RequestInfo)a.get(idx)).probability;
	    double prob = ((RequestInfo)a.get(idx)).score;

	    System.out.println( p + "," + prob );
	}

    }



    public static final void processLine( String line ) {
	
	if( !line.startsWith( "pcfgdetector:" ))
	    return;

	if( line.indexOf( " threshold= " ) == -1 )
	    return;

	int idx1 = "pcfgdetector: ".length();
	int idx2 = line.indexOf( " threshold= " );
	int idx3 = idx2 + "threshold= ".length();
	int idx4 = line.indexOf( "; probability= " );
	int idx5 = idx4 + "; probability= ".length();
	
	String requestid = line.substring( idx1, idx2 );
	String threshold = line.substring( idx3, idx4 );
	String probability = line.substring( idx5 );

	double t = Double.parseDouble( threshold );
	double p = Double.parseDouble( probability );

	RequestInfo ri = new RequestInfo();
	ri.requestid = requestid;
	//	ri.probability = p;
	ri.score = p;

	System.err.println( requestid + "," + threshold + "," + probability );

	requestids.add( ri );
    }

}
/*
    class RequestInfo implements Comparable {
	String requestid;
	double probability;
	
	public boolean equals( Object obj ) {
	    boolean ret = false;

	    if( obj instanceof RequestInfo ) {
		if( compareTo( obj ) == 0 )
		    ret = true;
	    }

	    return ret;
	}

	public int compareTo( Object obj ) {

	    RequestInfo other = (RequestInfo)obj;

	    if( other.probability > this.probability ) {
		return -1;
	    }
	    else if( other.probability < this.probability ) {
		return 1;
	    }
	    else {
		return this.requestid.compareTo( other.requestid );
	    }
	}
    }
*/
