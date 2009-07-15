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

public class ExtractHTTPErrorRequestIds {

    static Set badrequestids;
    

    public static void main( String[] argv ) {

	try {

	    if( argv.length != 1 ) {
		System.err.println( "Usage: java roc.pinpoint.report.ExtractHTTPErrorRequestIds [logfile]" );
	    }

	    String logfile = argv[0];

	    String basename = logfile.substring( 0, 
						 logfile.length() - ".log".length() );

	    for( int i=0; i<100; i++ ) {
		String loadgenfile = basename + ".loadgen.log." + i;
		File f = new File( loadgenfile );
		
		if( f.exists() ) {
		    System.err.println( "processing " + loadgenfile );
		    ParseFile( f );
		}
	    }


	}
	catch( Exception e ) {
	    e.printStackTrace();
	}

    }


    public static void ParseFile( File f ) throws IOException {

	LineNumberReader lnr =
	    new LineNumberReader( new FileReader( f ));

	while( true ) {
	    String line = lnr.readLine();
	    if( line == null ) 
		break;
	    processLine( line );
	}

    }

    public static void processLine( String line ) {

	if( !line.startsWith( "PPLOG: " ))
	    return;

	int idx1 = "PPLOG: requestid=[".length();
	int idx2 = line.indexOf( "] ;response=" );
	int idx3 = idx2 + "] ;response=".length();

	String requestid = line.substring( idx1, idx2 );
	String responsecode = line.substring( idx3 );

	if( responsecode.startsWith( "4" ) ||
	    responsecode.startsWith( "5" )) {
	    // this request has an error!

	    System.out.println( requestid );
	}
	
    }

}
