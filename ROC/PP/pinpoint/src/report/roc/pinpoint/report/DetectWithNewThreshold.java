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

public class DetectWithNewThreshold {

    static Set badrequestids;
    static double threshold;

    public static void main( String[] argv ) {

	try {

	    if( argv.length != 2 ) {
		System.err.println( "Usage: java roc.pinpoint.report.DetectWithNewThreshold [threshold] [analysisfile]" );
		return;
	    }
	    
	    threshold = Double.parseDouble( argv[0] );
	    String logfile = argv[1];
	    
	    badrequestids = new HashSet();
	    
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
	String probability = line.substring( idx5 );

	double p = Double.parseDouble( probability );

	if( p > threshold ) {
	    badrequestids.add( requestid );
	    System.out.println( requestid );
	}

    }

}
