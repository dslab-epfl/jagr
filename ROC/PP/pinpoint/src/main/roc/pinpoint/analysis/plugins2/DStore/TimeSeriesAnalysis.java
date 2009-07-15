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
package roc.pinpoint.analysis.plugins2.DStore;

import java.util.*;
import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;
import roc.pinpoint.analysis.timeseries.*;

public class TimeSeriesAnalysis implements Plugin {

    public static final String INPUT_COLLECTION_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_ARG = "outputCollection";
    public static final String DETECTION_PERIOD_ARG = "detectionPeriod";
    public static final String DEVIATION_ARG = "deviation";

    public static final String ALPHABET_SIZE_ARG = "alphabetSize";
    public static final String FEATURE_LEN_ARG = "featureLength";
    public static final String PATTERN_LEN_ARG = "patternLength";

    int ALPHABET_SIZE = 2;
    int FEATURE_LENGTH = 1;
    int PATTERN_LENGTH = 3;

    // TODO possible new arguments:
    //   - feature length, alphabet size, max anomaly size

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_ARG,
		       "input collection.  this plugin will look for Deviants in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_ARG,
		       "output collection. this plugin will place anomalies in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( DETECTION_PERIOD_ARG,
		       "detection period.  this plugin will run the anomaly detection every period...  unit is milliseconds.",
		       PluginArg.ARG_INTEGER,
		       false,
		       "30000" ),
	new PluginArg( DEVIATION_ARG,
		       "unacceptable deviation threshold",
		       PluginArg.ARG_DOUBLE,
		       false,
		       "0.5" ),
	new PluginArg( ALPHABET_SIZE_ARG,
		       "size of alphabet when discretizing time series",
		       PluginArg.ARG_INTEGER,
		       false,
		       "2" ),
	new PluginArg( FEATURE_LEN_ARG,
		       "number of features to lump together when discretizing time series",
		       PluginArg.ARG_INTEGER,
		       false,
		       "1" ),
	new PluginArg( PATTERN_LEN_ARG,
		       "max of length of pattern",
		       PluginArg.ARG_INTEGER,
		       false,
		       "3" )
    };


    private RecordCollection inputCollection;
    private RecordCollection outputCollection;

    private long detectionPeriod;
    private double deviation;

    private Timer timer;

    private AnalysisEngine engine;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    public void start( String id, Map args, AnalysisEngine engine ) {
	inputCollection = (RecordCollection) args.get(INPUT_COLLECTION_ARG);
        outputCollection = (RecordCollection) args.get(OUTPUT_COLLECTION_ARG);
 	detectionPeriod = ((Integer)args.get(DETECTION_PERIOD_ARG)).intValue();
	deviation = ((Double)args.get(DEVIATION_ARG)).doubleValue();

	ALPHABET_SIZE=((Integer)args.get(ALPHABET_SIZE_ARG)).intValue();
	FEATURE_LENGTH=((Integer)args.get(FEATURE_LEN_ARG)).intValue();
	PATTERN_LENGTH=((Integer)args.get(PATTERN_LEN_ARG)).intValue();

	this.engine = engine;
        timer = new Timer();
        timer.schedule(new MyAnomalySearchingTask(), 0, detectionPeriod);
    }

    public void stop() {
	timer.cancel();
    }

    class MyAnomalySearchingTask extends TimerTask {
	
	public void run() {
//    	    long starttime = System.currentTimeMillis();
	    System.err.println( "ENTER: TimeSeries Plugin pass..." );

	    synchronized( inputCollection ) {
		Map records = inputCollection.getAllRecords();

		int size = records.size();

		outputCollection.clearAllRecords();

		Iterator iter = records.keySet().iterator();
		while( iter.hasNext() ) {
		    Object key = iter.next();

		    //		    System.err.println( "\tTimeSeriesAnalysis: " + key.toString() );

		    Record rec = (Record)records.get( key );
		    // each record should contain a hashmap of TimeSeries instances
		    Map tsMap = (Map)rec.getValue();
		    Map dtsMap = new HashMap( tsMap.size() );
		    ArrayList indices = new ArrayList( tsMap.keySet() );

		    // this will contain the list of anomalous components
		    SortedSet deviants = new TreeSet();

		    for( int i=0; i<indices.size(); i++ ) {
			Object targetKey = indices.get(i);
			TimeSeries targetTS = (TimeSeries)
			    tsMap.get( targetKey );

			// copy non-targets as the references
			Map refTS = new HashMap();
			for( int j=0; j<indices.size(); j++ ) {
			    if( j != i ) {
				refTS.put( indices.get(j), 
					   tsMap.get( indices.get(j) ));
			    }
			}

			// calculate the feature boundaries based
			//    on the references
			double[] featureBoundaries = 
			    TimeSeries.CalculateFeatureBoundaries( refTS.values(), FEATURE_LENGTH, ALPHABET_SIZE );

			/**
			if( featureBoundaries == null ) {
			    System.err.println( "TimeSeriesAnalysis -- skipping this set, NODATA" );
			    continue;
			}
			**/
			
			// discretize the target time series
			targetTS.setFeatureBoundaries( featureBoundaries );
			DiscreteTimeSeries targetDTS = 
			    targetTS.discretize( FEATURE_LENGTH,
						 ALPHABET_SIZE );

			// discretize the ref time series and put them in
			//     refDTS map
			Map refDTS = new HashMap();
			Iterator refIter = refTS.keySet().iterator();
			while( refIter.hasNext() ) {
			    Object refKey = refIter.next();
			    TimeSeries ts = (TimeSeries)refTS.get( refKey );
			
			    ts.setFeatureBoundaries( featureBoundaries );  
			    DiscreteTimeSeries dts = 
				ts.discretize( FEATURE_LENGTH, ALPHABET_SIZE );
			    refDTS.put( refKey, dts );
			}
			
			
			DiscreteTimeSeries masterRef =
			    mergeAll( refDTS, ALPHABET_SIZE );

			SortedSet surprises = getSurprises( masterRef,
							    targetDTS,
							    (byte)ALPHABET_SIZE /* ignore the ending character */ );

			if( !surprises.isEmpty() ) {
			    RankedObject biggestSurprise = (RankedObject)
				surprises.last();
			    
			    // debug
//  			    Iterator iter2 = surprises.iterator();
//  			    while( iter2.hasNext() ) {
//    				System.err.print( "\t!!surprise!! " + ((RankedObject)iter2.next()).toString() );
//  			    }
			    
			    
			    deviants.add( new RankedObject( biggestSurprise.getRank(), targetKey ));				
			}
		    }
		    

		    if( deviants.size() > 0 ) {
			RankedObject biggestAnomaly = (RankedObject)
			    deviants.last();

			double anomalyThreshold =
			    0.75 * biggestAnomaly.getRank();

			if( anomalyThreshold > deviation ) {
			    SortedSet tailSet =
				deviants.tailSet( new RankedObject( anomalyThreshold, null ));
			    if( tailSet.size() == 1 ) {
//      				System.err.println( "*** REPORTING ANOMALY ***" );
				SortedSet topdeviant = new TreeSet();
				topdeviant.add( biggestAnomaly );

				Record r = new Record( topdeviant );
				outputCollection.setRecord( key, r );
			    }
			    else {
//      				System.err.println( "*** NOT REPORTING ANOMALY ***" );
			    }
			}
		    }
		}
		
	    }
	    
//  	    long endtime = System.currentTimeMillis();
//  	    System.err.println( "EXIT: TimeSeries Plugin pass... " + (endtime - starttime));
	}
	
	public SortedSet getSurprises( DiscreteTimeSeries ref, 
				       DiscreteTimeSeries x, byte ignore ) {
	    SuffixTree refTree = ref.getSuffixTree();
	    SuffixTree xTree = x.getSuffixTree();

	    return refTree.getSurprises( xTree, deviation, PATTERN_LENGTH, ignore );
	}

	public DiscreteTimeSeries mergeAll( Map dtsMap, int alphabetSize ) {
	    int totalSize = 0;
	    LinkedList list = new LinkedList();
	    Iterator iter = dtsMap.keySet().iterator();
	    while( iter.hasNext( ) ) {
		Object key = iter.next();

		DiscreteTimeSeries dts = (DiscreteTimeSeries)dtsMap.get( key );
		totalSize += dts.getLength() - 1 ; // -1 for the end-of-string token
		list.add( dts );
	    }

	    byte[] buf = new byte[ totalSize + 1 ];
	    int idx = 0;
	    iter = list.iterator();
	    while( iter.hasNext() ) {
		DiscreteTimeSeries dts = (DiscreteTimeSeries)iter.next();
		// TODO consider adding a marker in between merged dts...
		//      then ignore any surprise that contains the marker
		System.arraycopy( dts.getSeries(), dts.getStart(), 
				  buf, idx, dts.getLength()-1 );
		idx += dts.getLength() - 1;
	    }

	    buf[ buf.length - 1 ] = (byte)alphabetSize;

	    return new DiscreteTimeSeries( buf );
	}


	public DiscreteTimeSeries mergeAllDTSButOne( Map dtsMap, Object but,
						     int alphabetSize ) {
	    
	    int totalSize = 0;
	    LinkedList list = new LinkedList();
	    Iterator iter = dtsMap.keySet().iterator();
	    while( iter.hasNext( ) ) {
		Object key = iter.next();
		if( key == but )
		    continue;

		DiscreteTimeSeries dts = (DiscreteTimeSeries)dtsMap.get( key );
		totalSize += dts.getLength() - 1 ; // -1 for the end-of-string token
		list.add( dts );
	    }

	    byte[] buf = new byte[ totalSize + 1 ];
	    int idx = 0;
	    iter = list.iterator();
	    while( iter.hasNext() ) {
		DiscreteTimeSeries dts = (DiscreteTimeSeries)iter.next();
		// TODO consider adding a marker in between merged dts...
		//      then ignore any surprise that contains the marker
		System.arraycopy( dts.getSeries(), dts.getStart(), 
				  buf, idx, dts.getLength()-1 );
		idx += dts.getLength() - 1;
	    }

	    buf[ buf.length - 1 ] = (byte)alphabetSize;

	    return new DiscreteTimeSeries( buf );
	}

    }

}
