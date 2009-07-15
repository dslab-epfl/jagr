package roc.pinpoint.graph;

import java.io.*;
import java.util.*;

public class MakeDensityData {

    public static void main( String[] argv ) {

	try {

	int numbuckets = Integer.parseInt( argv[0] );
	String filename = argv[1];
	String outputfile = argv[2];


	LineNumberReader lr = null;
	if( filename.equals( "-" )) {
	    lr = new LineNumberReader( new InputStreamReader( System.in )); 
	}
	else {
	    lr = new LineNumberReader( new FileReader( filename ));
	}

	PrintWriter pw = null;
	if( outputfile.equals( "-" )) {
	    pw = new PrintWriter( System.out);
	}
	else {
	    pw = new PrintWriter( new FileWriter( outputfile ));
	}

	int[][] density = new int[ numbuckets+1][numbuckets+1];

	while( true ) {
	    String l = lr.readLine();
	    if( l == null )
		break;

	    l = l.trim();
	    if( l.length() == 0 )
		continue;

	    int idx = l.indexOf( " " );
	    if( idx == -1 )
		idx = l.indexOf( "\t" );
	    
	    String sY = l.substring(0,idx).trim();
	    String sX = l.substring(idx+1).trim();

	    double x = Double.parseDouble( sX );
	    double y = Double.parseDouble( sY );

	    int iX = (int)Math.floor(x * numbuckets);
	    int iY = (int)Math.floor(y * numbuckets);

	    //System.err.println( "(" + sX + "," + sY + ") mapped to bucket (" + iX + "," + iY + ")");

	    density[iY][iX]++;
	}

	int[][] smoothdensity = smoothdensity( density, density.length );

	for(int i=0; i<smoothdensity.length; i++ ) {
	    for( int j=0; j<smoothdensity[i].length; j++ ) {
		int d = smoothdensity[i][j];
		pw.println( ((double)j/numbuckets) + " " +
			    ((double)i/numbuckets) + " " + 
			    d /*(d>=1?Math.log(d):0)*/);
	    }
	    pw.println("" );
	}

	pw.close();

	
	}
	catch( Exception ex ) {
	    ex.printStackTrace();
	}

    }

    static final int S_X = 5;
    static final int S_Y = 5;

    public static final int[][] smoothdensity( int[][] density, int sz ) {
	int[][] ret = new int[sz][sz];

	
	double[][] smoothingFn = new double[S_X][S_Y];
	for( int i=0; i<S_X; i++ ) {
	    for( int j=0; j<S_Y; j++ ) {
		double xdistfromcenter = (S_X+1)/2 - i - 1;
		double ydistfromcenter = (S_Y+1)/2 - j - 1;
		//System.err.println( "distance2center[" + i + "][" + j + "]=" + xdistfromcenter + "," + ydistfromcenter );
		smoothingFn[i][j] = (Math.max(0, (S_X+1)/2 -
		    Math.sqrt( xdistfromcenter*xdistfromcenter +
			       ydistfromcenter*ydistfromcenter ))) /
		    ((S_X+1)/2);

		//	System.err.println( "smooth[" + i + "][" + j + "]=" + smoothingFn[i][j] );
	    }
	}

       


	for(int i=0; i<density.length; i++ ) {
	    for( int j=0; j<density[i].length; j++ ) {

		int count=0;
	
		for( int si=0; si<S_X; si++ ) {
		    for( int sj=0; sj<S_Y; sj++ ) {
			int d_i = i+si-((S_X+1)/2);
			int d_j = j+sj-((S_Y+1)/2);
			if( d_i>=0 && d_j >=0 &&
			    d_i < density.length &&
			    d_j < density.length ) {
			    ret[i][j] += 
				smoothingFn[si][sj] * 
				density[d_i][d_j];
			    count++;
			}
		    }
		}

		if( count == 0 ) {
		    System.err.println( "ACK! why did we process nothing at [" + i + "," + j + "]" );
		}
		else if( count != S_X * S_Y ) {
		    ret[i][j] = ((ret[i][j])*S_X*S_Y)/count;
		}
		
		if( ret[i][j] > 80 )
		    ret[i][j] = 80;

	    }

	}
     
	return ret;
    }


}
