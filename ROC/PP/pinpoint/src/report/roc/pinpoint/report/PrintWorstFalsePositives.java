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

public class PrintWorstFalsePositives {

    static SortedSet points;
    static Set badexpts;

    static int totalinjected;
    static Set currentbadrequests;

    static int totalread = 0;

    public static void main( String[] argv ) {

	try {

	    if( argv.length != 2 ) {
		System.err.println( "Usage: java roc.pinpoint.report.ROCCurvePSDetection [injecteddir] [analysisdir]" );
		return;
	    }

	    points = new TreeSet();
	    
	    String injecteddirname = argv[0];
	    String analysisdirname = argv[1];

	    File injecteddir = new File( injecteddirname );
	    File analysisdir = new File( analysisdirname );

	    File[] injectedfiles = injecteddir.listFiles();
	    File[] analysisfiles = analysisdir.listFiles();
	    
	    Map injectedMap = new HashMap();
	    Map analysisMap = new HashMap();

	    for( int i=0; i<injectedfiles.length; i++ ) {
		File f = injectedfiles[i];
		String fname = f.getName();
		if( fname.endsWith( ".injected" ) ) {
		    fname = fname.substring( 0, 
					     fname.length() - ".injected".length() );
		    injectedMap.put( fname, injectedfiles[i] );
		}
		else {
		    System.err.println( "\tignoring file " + fname );
		}
	    }
	    
	    for( int i=0; i<analysisfiles.length; i++ ) {
		File f = analysisfiles[i];
		String fname = f.getName();
		if( fname.endsWith( ".log.analysis" ) ) {
		    fname = fname.substring( 0, 
					     fname.length() - ".log.analysis".length() );
		    analysisMap.put( fname, analysisfiles[i] );
		}
		else {
		    System.err.println( "\tignoring file " + fname );
		}
	    }


	    Iterator iter = analysisMap.keySet().iterator();
	    while( iter.hasNext() ) {
		String f = (String)iter.next();
		
		File injectedfile = (File)injectedMap.get( f );
		currentbadrequests = loadRequestIds( injectedfile );
		totalinjected += currentbadrequests.size();

		File analysisfile = (File)analysisMap.get( f );

		System.err.println( "Loading " + analysisfile.toString() );
	       
		LineNumberReader lnr =
		    new LineNumberReader( new FileReader( analysisfile ));
	    
		while( true ) {
		    String line = lnr.readLine();
		    if( line == null )
			break;
		    processLine( f, line );
		}
		
		System.err.println( "CURR TOTAL REQ = " + points.size() + 
				    "; " + totalread );
	    }

	    System.out.println( "TOTAL REQUESTS = " + points.size() );


	    iter = points.iterator();
	    int countTP=0;
	    int countFP=0;
	    double lastscore = Double.MAX_VALUE;

	    System.out.println( "threshold, numtruepositives, numfalsepositives, numfalsenegatives,numexptsdetected" );

	    while( iter.hasNext() ) {
		RequestInfo ri = (RequestInfo)iter.next();

		if( ri.score != lastscore ) {
		    System.out.println( lastscore + ", " +
					countTP + ", " +
					countFP + ", " +
					(totalinjected-countTP) + ", " +
					badexpts.size() );
		}

		if( ri.failure )
		    countTP++;
		else
		    countFP++;
		
		badexpts.add( ri.exptname );

		lastscore = ri.score;
	    }


	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
	
    }


    public static final void processLine( String exptname, String line ) {
	
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
	String score = line.substring( idx5 );

	double s = Double.parseDouble( score );

	totalread ++;

	RequestInfo ri = new RequestInfo();
	ri.score = s;
	ri.failure = currentbadrequests.contains( requestid );
	ri.requestid = requestid;
	ri.exptname = exptname;

	points.add( ri );
    }

    public static Set loadRequestIds( File f ) throws IOException {
        LineNumberReader lnr = 
	    new LineNumberReader( new FileReader( f ));

	HashSet ret = new HashSet();
	
	while( true ) {
	    String l = lnr.readLine();
	    if( l == null )
		break;
	    ret.add( l );
	}

	return ret;	
    }
    

}


