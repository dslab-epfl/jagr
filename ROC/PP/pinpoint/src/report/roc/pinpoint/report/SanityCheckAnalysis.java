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

public class SanityCheckAnalysis {

    public static void main( String argv[] ) {
	
	try {
	    
	    if( argv.length != 2 ) {
		System.err.println( "Usage: java roc.pinpoint.report.SanityCheckAnalysis [injecteddir] [analysisdir]" );
	    }

	    String injecteddirname = argv[0];
	    String detecteddirname = argv[1];

	    File injecteddir = new File( injecteddirname );
	    File detecteddir = new File( detecteddirname );
	    	    
	    File[] injectedfiles = injecteddir.listFiles();
	    File[] detectedfiles = detecteddir.listFiles();
	    
	    Map injectedMap = new HashMap();
	    Map detectedMap = new HashMap();

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
		if( fname.endsWith( ".log.analysis" ) ) {
		    fname = fname.substring( 0, 
					     fname.length() - ".log.analysis".length() );
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

		sanitycheck( fname,injf,detf);
	    }	    
	    
	    
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
	

    }



    public static void sanitycheck( String fname, File injf, File detf ) 
	throws IOException {
	System.out.println( "Sanity Checking " + fname + "..." );

	Set injSet = loadRequestIds( injf );
	Set detSet = loadRequestIdsFromAnalysis( detf );

	injSet.removeAll( detSet );

	System.out.println( "\t" + injSet.size() + " injected requests not processed at all!" );

	Iterator iter = injSet.iterator();
	while( iter.hasNext() ) {
	    String s = (String)iter.next();
	    System.out.println( "\tRequestId " + s + " NOT PROCESSED" );
	}

    }

    public static Set loadRequestIdsFromAnalysis( File f ) throws IOException {

	Set ret = new HashSet();

	LineNumberReader lnr =
	    new LineNumberReader( new FileReader( f ));

	while( true ) {
	    String line = lnr.readLine();
	    if( line == null )
		break;

	    if( !line.startsWith( "pcfgdetector:" ))
		continue;

	    if( line.indexOf( " threshold= " ) == -1 )
		continue;

	    int idx1 = "pcfgdetector: ".length();
	    int idx2 = line.indexOf( " threshold= " );
	    
	    String requestid = line.substring( idx1, idx2 );
	    
	    
	    ret.add( requestid );
	}
	
	return ret;
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
