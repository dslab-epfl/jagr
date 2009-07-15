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

public class DetectWithDynamicThreshold {

    static SortedSet points;
    static Set badexpts;

    static int totalinjected;

    static int totalread = 0;

    static String outputdir;

    public static void main( String[] argv ) {

	try {

	    if( argv.length != 3 ) {
		System.err.println( "Usage: java roc.pinpoint.report.DetectWithDynamicThreshold [injecteddir] [analysisdir] [outputdir]" );
		return;
	    }

	    points = new TreeSet();
	    badexpts = new HashSet();

	    String injecteddirname = argv[0];
	    String analysisdirname = argv[1];
	    outputdir = argv[2];

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
		    System.err.println( "\tadding file " + fname );
		}
		else {
		    System.err.println( "\tignoring file " + fname );
		}
	    }


	    /*	    SortedSet normaldata = 
		loadAnalysisFile( "faultconfig-baseline",
				  (File)analysisMap.get( "faultconfig-baseline" ));
	    */    
	    SortedSet normaldata = 
		loadAnalysisFile( "faultconfig-nofault1",
				  (File)analysisMap.get( "faultconfig-nofault1" ));
	    

	    Iterator iter = analysisMap.keySet().iterator();
	    while( iter.hasNext() ) {
		String f = (String)iter.next();
		
		System.err.println( "----" + f + "----" );

		if( f.indexOf( "baseline" ) != -1 ) {
		    System.err.println( "\tIgnoring baseline..." );
		    continue;
		}

		int idx = f.indexOf( "_" );
		int idx2 = f.indexOf( "-", idx+1 );

		String location, faulttype;
		
		if( idx < 0 ) {
		    System.err.println( "ACK: fname = " + f );
		}
		
		if( idx2 > 0 ) {
		    //System.err.println( "f=" + f + "; idx=" + idx + "; idx2=" + idx2 );
		    location = f.substring( idx+1, idx2 );
		    faulttype = f.substring( idx2+1 );
		}
		else {
		    location = f.substring( idx+1 );
		    faulttype = "none";
		}

		location = swig.util.StringHelper.ReplaceAll( location, "_", " " );

		System.err.println( "looking for " + f );
		File injectedfile = (File)injectedMap.get( f );

		if( injectedfile == null ) {
		    System.err.println( "SIZE: " + injectedMap.size() ) ;
		    Iterator iter43 = injectedMap.keySet().iterator();
		    while( iter43.hasNext() ) {
			Object k = iter43.next();
			System.err.println( "EMKDEBUGGGGG: " + k );
		    }

		}

		Set injectedrequests = loadRequestIds( injectedfile );

		/*
		if( injectedrequests.size() < 10 &&
		    f.indexOf( "nofault" ) == -1 ) {
		    System.err.println( "Skipping " + f + ": too few fault injections" );
		    continue;
		}
		*/

		System.err.println( "EMKDEBUG: analyzing " + f.toString() );
		File analysisfile = (File)analysisMap.get( f );
		SortedSet analysisdata = loadAnalysisFile( f, analysisfile );
		Set badrequests = compareData( normaldata, analysisdata, 1.0 );	       

		System.out.println( location + "," + faulttype + "," +
				    badrequests.size() );
		printBadRequests( f, badrequests );
	    }





	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
	
    }

    public static final void printBadRequests( String fname, Set badrequests )  throws IOException {

	String outputfilename = outputdir + "/" + fname + ".detected";

	PrintWriter pw = new PrintWriter( new FileWriter( new File( outputfilename )));

	Iterator iter = badrequests.iterator();
	while( iter.hasNext() ) {
	    RequestInfo ri = (RequestInfo)iter.next();
	    pw.println( ri.requestid );
	}

	pw.close();
	
    }


    public static final SortedSet loadAnalysisFile( String f, File analysisfile ) 
    throws IOException {

	TreeSet ret = new TreeSet();

	System.err.println( "Loading " + analysisfile.toString() );
	       
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

		System.err.println( "EMKDEBUG: detected anomalies at threshold=" + ri.score );
		
		ret.addAll( analysistail );
	    }
	    
	}

	return ret;
    }

    public static final RequestInfo processLine( String exptname, String line ) {
	
	if( !line.startsWith( "pcfgdetector:" ))
	    return null;

	if( line.indexOf( " threshold= " ) == -1 )
	    return null;

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


