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

import java.io.*;
import java.util.*;

public class CompareInjectedAndDetected {

    static ResultStat totalstats;
    static Map perfaultstats;

    public static void main( String argv[] ) {

	try {
	    if( argv.length < 2 ) {
		System.err.println( "Usage: java roc.pinpoint.report.CompareInjectedAndDetected [injecteddir] [detecteddir]" );
		return;
	    }

	    String injecteddirname = argv[0];
	    String detecteddirname = argv[1];

	    // print header
	    System.out.println( "faultlocation,type,numinjected,truepositive,falsepositive,falsenegative" );

	    File injecteddir = new File( injecteddirname );
	    File detecteddir = new File( detecteddirname );
	    
	    File[] injectedfiles = injecteddir.listFiles();
	    File[] detectedfiles = detecteddir.listFiles();
	    
	    Map injectedMap = new HashMap();
	    Map detectedMap = new HashMap();

	    perfaultstats = new HashMap();
	    totalstats = new ResultStat();

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


	    for( int i=0; i<detectedfiles.length; i++ ) {
		File f = detectedfiles[i];
		String fname = f.getName();
		if( fname.endsWith( ".log.detected" ) ) {
		    fname = fname.substring( 0, 
					     fname.length() - ".log.detected".length() );
		    detectedMap.put( fname, detectedfiles[i] );
		}
		else {
		    System.err.println( "\tignoring file " + fname );
		}
	    }

	    
	    Iterator iter = detectedMap.keySet().iterator();
	    while( iter.hasNext() ) {
		String fname = (String)iter.next();
		File detf = (File)detectedMap.get( fname );
		File injf = (File)injectedMap.get( fname );

		System.err.println( "\tloading " + fname + " from [" + 
				    detf + " and " + injf + "]" );

		compare(fname,injf,detf);
	    }
    
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}

    }

    public static void compare( String fname, File injf, File detf ) 
	throws IOException {
	Set injSet = loadRequestIds( injf );
	Set detSet = loadRequestIds( detf );

	int idx = fname.indexOf( "-" );
	int idx2 = fname.indexOf( "-", idx );

	String location, faulttype;


	if( idx < 0 ) {
	    System.err.println( "ACK: fname = " + fname );
	}

	if( idx2 > 0 ) {
	    location = fname.substring( idx+1, idx2 );
	    faulttype = fname.substring( idx2 );
	}
	else {
	    location = fname.substring( idx+1 );
	    faulttype = "none";
	}

	int numinjected = injSet.size();

	Set truepositives = new HashSet( detSet );
	truepositives.retainAll( injSet );
	int truepositivecount = truepositives.size();

	int falsepositivecount = detSet.size() - truepositivecount;

	int falsenegativecount = injSet.size() - truepositivecount;

	System.out.println( location + "," + faulttype + "," + numinjected +
			    "," + truepositivecount + "," + falsepositivecount +
			    "," + falsenegativecount );

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

    class ResultStat {
	int numinjected;  // = truepositive + falsenegative;
	int truepositive;
	int falsepositive;
	int truenegative;
	int falsenegative;
    }

