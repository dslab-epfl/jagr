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
package roc.pinpoint.analysis.timeseries;

import java.io.*;
import java.util.*;
import roc.pinpoint.analysis.structure.*;


/**
 * This class represents a real-valued time-series, and provides support for
 * discretizing the time-series into a DiscreteTimeSeries object, and
 * loading time-series from files.
 *
 */
public class TimeSeries {

    // circular buffer
    double[] series;
    int start;
    int length;

    double[] featureBoundaries = null;

    public TimeSeries( double[] series ) {
	this( series, 0, series.length );
    }

    public TimeSeries( double[] series, int start, int length ) {
	this.series = series;
	this.start = start;
	this.length = length;
    }

    public void addValue( double d ) {
	int idx = (start + length) % series.length;
	series[ idx ] = d;
	if( length == 0 || idx != start ) 
	    length++;
	else 
	    start = (start + 1) % series.length;
    }

    public void setFeatureBoundaries( double[] featureBoundaries ) {
	this.featureBoundaries = featureBoundaries;
    }

    public double[] getFeatureBoundaries() {
	return featureBoundaries;
    }

    public void setFeatureBoundaries( int featureLength, int alphabetSize ) {
	setFeatureBoundaries( calculateFeatureBoundaries( featureLength, alphabetSize ));
    }

    public double[] calculateFeatureBoundaries( int featureLength,
						int alphabetSize ) {
	return calculateFeatureBoundaries( start, length, featureLength, alphabetSize );
    }

    private double[] calculateFeatureBoundaries( int start, int length,
						 int featureLength,
						 int alphabetSize ) {
	double[] ret = new double[ alphabetSize - 1];

	/**

	// take sqrt( length / featureLength ) number of samples
	int numSamples = 2 * (int)Math.sqrt( ((double)length) / ((double)featureLength) );
	
	// System.err.println( "calculateFeatureBoundaries(): numSamples = " + numSamples );

	double[] samples = new double[numSamples];
	int max = length / featureLength;

	Random rand = new Random();

	for( int i=0; i<numSamples; i++ ) {
	    int r = rand.nextInt( max );
	    samples[i] = extractFeature( start + (featureLength * r),
					 featureLength );
	}

	**/

	double[] samples = new double[length / featureLength ];
	for( int i=0; i<samples.length; i++ ) {
	    samples[i] = extractFeature( start + (featureLength * i ),
					 featureLength );
	}


	Arrays.sort( samples );

	/**
	for( int i=0; i<samples.length; i++ ) {
	    System.err.println( "\tsamples[" + i + "] = " 
				+ samples[i] );
	}
	**/
	
	if( samples.length == 0 ) {
	    //System.err.println( "TimeSeries -- NODATA" );
	    return null;
	}

	for( int i=0; i<alphabetSize-1; i++ ) {
	    int j = ((i+1) * samples.length) / (alphabetSize);
	    ret[i] = samples[ j ];
	}

	return ret;
    }


    public static double[] CalculateFeatureBoundaries( Collection timeseries,
						       int featureLength,
						       int alphabetSize ) {
	double[] ret = new double[ alphabetSize - 1];

	int numSamples = 0;

	/**
	System.err.println( "\tCalculateFeaturBoundaries: featurelength=" +
			    featureLength + "; alphabetSize=" + alphabetSize );
	*/


	Iterator iter = timeseries.iterator();
	while( iter.hasNext() ) {
	    TimeSeries ts = (TimeSeries)iter.next();
	    //System.err.println( "\tTimeSeries.length=" + ts.length );
	    numSamples += ts.length / featureLength;
	}
	
	//System.err.println( "\tnumSamples=" + numSamples );

	double[] samples = new double[ numSamples ];

	iter = timeseries.iterator();
	int i=0;
	while( iter.hasNext() ) {
	    TimeSeries ts = (TimeSeries)iter.next();
	    for( int c=0; c<(ts.length/featureLength); c++,i++ ) {
		samples[i] = ts.extractFeature( ts.start + (featureLength*c),
						featureLength );
	    }
	}

	Arrays.sort( samples );

	if( samples.length == 0 ) {
	    //System.err.println( "TimeSeries -- NODATA" );
	    return null;
	}


	for( i=0; i<alphabetSize-1; i++ ) {
	    int j = ((i+1) * samples.length) / (alphabetSize + 1);
	    //System.err.println( "ts:135: samples.length = " + samples.length + "; i=" + i + "; j=" + j + "; ret.length = " + ret.length );
	    ret[i] = samples[ j ];
	}

	return ret;
    }


    public DiscreteTimeSeries discretizeSubSeries( int start, int length,
						   int featureLength,
						   int alphabetSize ) {
	TimeSeries ts = new TimeSeries( series, this.start + start, length );
	ts.setFeatureBoundaries( featureBoundaries );

	return ts.discretize( featureLength, alphabetSize );
    }

    public DiscreteTimeSeries discretize( int featureLength,
					  int alphabetSize ) {
	double[] fb = featureBoundaries;

	if( fb == null ) {
	    fb = calculateFeatureBoundaries(featureLength, alphabetSize);
	}
	
	return discretize( featureLength, alphabetSize, fb );
    }

    public DiscreteTimeSeries discretize( int featureLength,
					  int alphabetSize,
					  double[] featureBoundaries ) {
	byte[] discrete = new byte[ length / featureLength + 1 ];

	// extract the discrete string
	for( int i=0,c=0; c<discrete.length; i+= featureLength, c++ ) {
	    double d = extractFeature( start + i, featureLength );
	    discrete[c] = mapFeature( d, featureBoundaries );
	}

	// add an end-of-string token
	discrete[ discrete.length - 1] = (byte)alphabetSize;

	return new DiscreteTimeSeries( discrete, 0, discrete.length );
    }

    /**
     *  its unclear what the best feature extraction technique is.  Keogh
     *  says "it may be domain-dependent."  Here, we use the mean.
     *  TODO: switch to using the slope of the best-fit line.
     */
    private double extractFeature( int start, int length ) {
	double total = 0;
	for( int i=start; i<start + length; i++ ) {
	    total += series[i % series.length];
	}

	//System.err.println( "extractFeature: total = " + total + 
	//	    "; length = " + length );

	return ((double)total/(double)length);
    }

    private byte mapFeature( double feature, double[] featureBoundaries ) {
	int i;
	for( i=0; i<featureBoundaries.length; i++ ) {
	    if( feature <= featureBoundaries[i] )
		break;
	}

	//	System.err.println( "MAPFEATURE: " + feature + " = " +
	//	    new String(new byte[] { (byte)('a' + i) }) );

	return (byte)i;
    }
    
    public String toString() {
	StringBuffer ret = new StringBuffer();
	for( int i=start; i<start + length; i++ ) {
	    ret.append( series[i % series.length ] );
	    ret.append( "\n" );
	}

	return ret.toString();
    }
    

    public static TimeSeries Load( String filename ) throws IOException {
	ArrayList values = new ArrayList();
	
	LineNumberReader lnr = new LineNumberReader( new FileReader( new File( filename )));

	boolean done = false;

	while( !done ) {
	    String line = lnr.readLine();
	    if( line == null ) {
		done = true;
	    }
	    else {
		double v = Double.parseDouble( line );

		values.add( new Double( v ));
	    }
	}

	double[] v = new double[values.size()];

	for( int i=0; i<v.length; i++ ) {
	    v[i] = ((Double)values.get(i)).doubleValue();
	}

	return new TimeSeries( v, 0, v.length );
    }
    

    public static void main( String argv[] ) {
	
	String reffilename = argv[0];
	String testfilename = argv[1];


	try {

	TimeSeries ref = TimeSeries.Load( reffilename );
	TimeSeries test = TimeSeries.Load( testfilename );


	double[] featureBoundaries = ref.calculateFeatureBoundaries( 4, 2 );

	System.err.println( "Feature Boundaries: " );
	for( int i=0; i<featureBoundaries.length; i++ ) {
	    System.err.println( "\t" + featureBoundaries[i] );
	}
		
	ref.setFeatureBoundaries( featureBoundaries );
	DiscreteTimeSeries dsRef = ref.discretize( 2, 2 );
	test.setFeatureBoundaries( featureBoundaries );
	DiscreteTimeSeries dsTest = test.discretize( 2, 2 );

	System.err.println( "Reference TimeSeries: \n" + dsRef.toString() );
	System.err.println( "Test TimeSeries: \n" + dsTest.toString() );


	SuffixTree stRef = dsRef.getSuffixTree();
	SuffixTree stTest = dsTest.getSuffixTree();

	SortedSet s = stRef.getSurprises( stTest, 0, 4, (byte)2 );

	Iterator iter = s.iterator();
	while( iter.hasNext() ) {
	    RankedObject ro = (RankedObject)iter.next();
	    System.out.println( ro.toString() );
	}	

	}
	catch( Exception e ) {
	    e.printStackTrace();
	}

	/**
	TimeSeries ts = new TimeSeries( new double[10], 0, 0 );
	for( int i=0; i<100; i++ ) {
	    ts.addValue( i );
	    System.err.println( "\n--" + i + "---\n" + ts.toString() );
	}
	*/

	/**
	double[] testData = new double[ argv.length ];
	for( int i=0; i<testData.length; i++ ) {
	    testData[i] = Double.parseDouble( argv[i] );
	}

	testTimeSeries( testData );
	**/
    }

    public static void testTimeSeries( double[] testData ) {
	TimeSeries ts = new TimeSeries( testData );
	
	//System.err.println( ts.toString() );

	double[] featureBoundaries = ts.calculateFeatureBoundaries( 2, 7 );

	/**
	for( int i=0; i<featureBoundaries.length; i++ ) {
	    System.err.println( "\tfeatureBoundaries[" + i + "] = " 
				+ featureBoundaries[i] );
	}
	**/
	
	ts.setFeatureBoundaries( featureBoundaries );

	DiscreteTimeSeries dts = ts.discretize( 2, 7 );

	System.err.println( dts.toString() );

	SuffixTree st = dts.getSuffixTree();
	//System.err.println( st.toString() );
    }

}
