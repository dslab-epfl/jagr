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

public class DetectSingleWithDynamicThreshold {

    static SortedSet points;
    static Set badexpts;

    static int totalinjected;

    static int totalread = 0;

    static String outputdir;

    public static void main( String[] argv ) {

	try {

	    if( argv.length != 2 ) {
		System.err.println( "Usage: java roc.pinpoint.report.DetectWithDynamicThreshold goodlog badlog" );
		return;
	    }

	    points = new TreeSet();
	    badexpts = new HashSet();

	    String goodlog = argv[0];
	    String badlog = argv[1];

	    File goodlogfile = new File( goodlog );
	    File badlogfile = new File( badlog );

	    SortedSet gooddata = 
		loadAnalysisFile( goodlog, goodlogfile );

	    SortedSet baddata =
		loadAnalysisFile( badlog, badlogfile );

	    Set badrequests = compareData( gooddata, baddata, 1.0 );

	    printBadRequests( badrequests );
	    
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
	
    }

    public static final void printBadRequests( Set badrequests ) 
	throws IOException {

	Iterator iter = badrequests.iterator();
	while( iter.hasNext() ) {
	    RequestInfo ri = (RequestInfo)iter.next();
	    System.out.println( ri.requestid );
	}
    }


    public static final SortedSet loadAnalysisFile( String f, File analysisfile ) 
    throws IOException {

	TreeSet ret = new TreeSet();

	LineNumberReader lnr =
	    new LineNumberReader( new FileReader( analysisfile ));
	    
	while( true ) {
	    String line = lnr.readLine();
	    if( line == null )
		break;
	    RequestInfo ri = processLine( f, line );
	    if( ri != null ) {
		ret.add( ri );
	    }
	}

	return ret;
    }


    public static Set compareData( SortedSet normaldata, 
				    SortedSet analysisdata,
				    double alpha ) {
	
	HashSet ret = new HashSet();

	Iterator iter = normaldata.iterator();

	while( iter.hasNext() ) {
	    RequestInfo ri = ((RequestInfo)iter.next()).getSuccessor();

	    Set normaltail = normaldata.headSet( ri );
	    Set analysistail = analysisdata.headSet( ri );
	    
	    if( analysistail.size() > alpha * normaltail.size() ) {
		// if true, we've detected serious anomalies!

		//System.err.println( "EMKDEBUG: detected anomalies at threshold=" + ri.score );
		
		ret.addAll( analysistail );
	    }
	    
	}

	return ret;
    }

    public static final RequestInfo processLine( String exptname, String line ) {
	
	if( !line.startsWith( "pcfgdetector:" ))
	    return null;

	if( line.indexOf( "; probability= " ) == -1 )
	    return null;

	int idx1 = "pcfgdetector: ".length();
	int idx4 = line.indexOf( "; probability= " );
	int idx5 = idx4 + "; probability= ".length();
	
	String requestid = line.substring( idx1, idx4 );
	String score = line.substring( idx5 );

	double s = Double.parseDouble( score );

	totalread ++;

	RequestInfo ri = new RequestInfo();
	ri.score = s;
	//ri.failure = currentbadrequests.contains( requestid );
	ri.requestid = requestid;
	ri.exptname = exptname;

	return ri;
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


