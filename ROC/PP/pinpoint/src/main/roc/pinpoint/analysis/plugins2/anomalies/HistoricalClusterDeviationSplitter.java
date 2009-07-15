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
package roc.pinpoint.analysis.plugins2.anomalies;

import java.util.*;

import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.clustering.*;
import roc.pinpoint.analysis.structure.*;

/**
 * 
 * @author emrek@cs.stanford.edu
 *
 */
public class HistoricalClusterDeviationSplitter implements Plugin {

    public static final String INPUT_COLLECTION_ARG = "inputCollection";
    public static final String HISTORICAL_COLLECTION_ARG = "historicalCollection";
    public static final String OUTPUT_GOOD_COLLECTION_ARG = 
	"outputGoodCollection";
    public static final String OUTPUT_BAD_COLLECTION_ARG = 
	"outputBadCollection";

    public static final String DETECTION_PERIOD_ARG = "detectionPeriod";
    public static final String SENSITIVITY_ARG = "sensitivity";
    public static final String ONLINE_ARG = "online";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_ARG,
		       "input collection.  this plugin will look for (Identifiable) possible anomalies in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( HISTORICAL_COLLECTION_ARG,
		       "historical collection.  this plugin will look for historical comparisons (Clusters) in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_GOOD_COLLECTION_ARG,
		       "output good collection. this plugin will place non-anomalous paths in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_BAD_COLLECTION_ARG,
		       "output bad collection. this plugin will place anomalous paths in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( DETECTION_PERIOD_ARG,
		       "detection period.  this plugin will run the anomaly detection every period...  unit is milliseconds.",
		       PluginArg.ARG_INTEGER,
		       false,
		       "30000" ),
	new PluginArg( SENSITIVITY_ARG,
		       "distance beyond which to mark anomalies",
		       PluginArg.ARG_DOUBLE,
		       false,
		       "2" ),
	new PluginArg( ONLINE_ARG,
		       "set to 'true' to work online",
		       PluginArg.ARG_BOOLEAN,
		       false,
		       "false" )
    };

    private RecordCollection inputCollection;
    private RecordCollection historicalCollection;
    private RecordCollection outputGoodCollection;
    private RecordCollection outputBadCollection;

    private Collection definingAttributes;

    private long detectionPeriod;
    private double sensitivity;

    private Timer timer;

    private AnalysisEngine engine;
    private boolean online;



    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {
	inputCollection = (RecordCollection) args.get(INPUT_COLLECTION_ARG);
        historicalCollection = (RecordCollection) args.get(HISTORICAL_COLLECTION_ARG);
        outputGoodCollection = (RecordCollection) args.get(OUTPUT_GOOD_COLLECTION_ARG);
	outputBadCollection = (RecordCollection) args.get(OUTPUT_BAD_COLLECTION_ARG);
 	detectionPeriod = ((Integer)args.get(DETECTION_PERIOD_ARG)).intValue();
	sensitivity = ((Double)args.get(SENSITIVITY_ARG)).doubleValue();

	this.engine = engine;
        timer = new Timer();
        timer.schedule(new MyAnomalySearchingTask(), 0, detectionPeriod);
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        timer.cancel();
    }
    

    class MyAnomalySearchingTask extends TimerTask {

	public void run() {
	    System.err.println( "HistoricalClusterDeviationSplitter Plugin pass..." );

	    List goodRecords = new LinkedList();
	    List badRecords = new LinkedList();



	    synchronized( historicalCollection ) {
		Map historicalRecords = historicalCollection.getAllRecords();

		synchronized( inputCollection ) {
		    Map records = inputCollection.getAllRecords();
		    int size = records.size();

		    Iterator iter = records.keySet().iterator();
		    while( iter.hasNext() ) {
			Object key = iter.next();
			
			Record rec = (Record)records.get( key );
			Distanceable test = (Distanceable)rec.getValue();

			boolean passed = false;
			double minDistance = 10000000;
			Iterator historicalIter = 
			    historicalRecords.values().iterator();
			while( historicalIter.hasNext() ) {
			    Record historicalRec = (Record)historicalIter.next();
			    Distanceable histDistanceable = 
				(Distanceable)historicalRec.getValue();
                            
                            // TODO
			    // double dist = histDistanceable.getDistance( new Cluster( test ));
                            double dist=0;

			    if( dist < sensitivity ) {
				passed = true;
				break;
			    }
			    else if( dist < minDistance )  {
				minDistance = dist;
			    }

			}
			

			Record r = new Record( test );
			if( passed ) {
			    goodRecords.add( r );
			    //outputGoodCollection.setRecord( test, r );
			}
			else {
			    badRecords.add( r );
			    //outputBadCollection.setRecord( test, r );
			}
			
		    }

		}
	    }

	    outputGoodCollection.clearAllRecords();
	    Iterator iter = goodRecords.iterator();
	    while( iter.hasNext() ) {
		Record r = (Record)iter.next();
		outputGoodCollection.setRecord( r.getValue(), r );
	    }

	    outputBadCollection.clearAllRecords();
	    iter = badRecords.iterator();
	    while( iter.hasNext() ) {
		Record r = (Record)iter.next();
		outputBadCollection.setRecord( r.getValue(), r );		
	    }

            String inIsReady = (String)inputCollection.getAttribute( "isReady" );
            if( !online && (inIsReady != null) && (inIsReady.equals( "true" ))) {
                timer.cancel();
            }

	}

    }

    


}
