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

public class LogMonitor {

    public static void main( String[] argv ) {

	try {

	    if( argv.length != 1 ) {
		System.err.println( "Usage: java roc.pinpoint.report.LogMonitor [analysisdir]" );
		return;
	    }

	    String analysisdirname = argv[0];

	    File analysisdir = new File( analysisdirname );
	    File[] analysisfiles = analysisdir.listFiles();
	    
	    Map analysisMap = new HashMap();

	    for( int i=0; i<analysisfiles.length; i++ ) {
		File f = analysisfiles[i];
		String fname = f.getName();
		if( fname.endsWith( ".server.b1" ) ) {
		    fname = fname.substring( 0, 
					     fname.length() - ".server.b1".length() );
		    List l = (List)analysisMap.get( fname );
		    if( l == null ) {
			l = new LinkedList();
			analysisMap.put( fname, l );
		    }
		    l.add( analysisfiles[i] );
		}
		if( fname.endsWith( ".server" ) ) {
		    fname = fname.substring( 0, 
					     fname.length() - ".server".length() );
		    List l = (List)analysisMap.get( fname );
		    if( l == null ) {
			l = new LinkedList();
			analysisMap.put( fname, l );
		    }
		    l.add( analysisfiles[i] );
		}
		else if( fname.endsWith( ".server.b2" ) ) {
		    fname = fname.substring( 0, 
					     fname.length() - ".server.b2".length() );
		    List l = (List)analysisMap.get( fname );
		    if( l == null ) {
			l = new LinkedList();
			analysisMap.put( fname, l );
		    }
		    l.add( analysisfiles[i] );
		}
		else if( fname.endsWith( ".server.b3" ) ) {
		    fname = fname.substring( 0, 
					     fname.length() - ".server.b3".length() );
		    List l = (List)analysisMap.get( fname );
		    if( l == null ) {
			l = new LinkedList();
			analysisMap.put( fname, l );
		    }
		    l.add( analysisfiles[i] );
		}
		else {
		    System.err.println( "\tignoring file " + fname );
		}
	    }


	    Iterator iter = analysisMap.keySet().iterator();
	    while( iter.hasNext() ) {
		String f = (String)iter.next();
		

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

		List filelist = (List)analysisMap.get( f );
		Iterator fileiter = filelist.iterator();
		int numerrors = 0;
		while( fileiter.hasNext() ) {
		    File analysisfile = (File)fileiter.next();
		    System.err.println( "loading file " + analysisfile.toString() );
		    numerrors += loadServerFile( f, analysisfile );
		}

		System.out.println( location + "," + faulttype + "," +
				    numerrors );
	    }

	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
	
    }

    public static final int loadServerFile( String f, File analysisfile ) 
    throws IOException {
	int ret =0;

	System.err.println( "Loading " + analysisfile.toString() );
	       
	LineNumberReader lnr =
	    new LineNumberReader( new FileReader( analysisfile ));

	// keep reading till JBoss is done loading

	boolean docount = false;

	while( true ) {
	    String line = lnr.readLine();
	    
	    if( line == null )
		break;
	    
	    if( line.indexOf( "Started in" ) != -1 ) {
		docount = true;
	    }
	    
	    if( docount && line.indexOf( "Exception" ) != -1 ) {
		ret ++;
	    }
	}

	lnr.close();

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
		
		ret.addAll( analysistail );
	    }
	    
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


