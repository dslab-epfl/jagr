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

/**
 * rank components by their distance from the target component
 * @author emrek@cs.stanford.edu
 *
 */
public class PeerAnomalies implements Plugin {

    public static final String INPUT_COLLECTION_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_ARG = "outputCollection";
    public static final String DETECTION_PERIOD_ARG = "detectionPeriod";
    public static final String DEVIATION_ARG = "deviation";

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
		       "0.5" )
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

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {
	inputCollection = (RecordCollection) args.get(INPUT_COLLECTION_ARG);
        outputCollection = (RecordCollection) args.get(OUTPUT_COLLECTION_ARG);
 	detectionPeriod = ((Integer)args.get(DETECTION_PERIOD_ARG)).intValue();
	deviation = ((Double)args.get(DEVIATION_ARG)).doubleValue();

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
	    System.err.println( "PeerAnomalies Plugin pass..." );

	    outputCollection.clearAllRecords();

	    synchronized( inputCollection ) {
		Map records = inputCollection.getAllRecords();
		int size = records.size();

		outputCollection.clearAllRecords();

		Iterator iter = records.keySet().iterator();
		while( iter.hasNext() ) {
		    Object key = iter.next();
		    
		    Record rec = (Record)records.get( key );
		    Deviants deviantContainer = (Deviants)rec.getValue();
		    
		    deviantContainer.setAcceptableDeviation( deviation );
		    SortedSet deviants = deviantContainer.getDeviants();

		    if( deviants.size() > 0 ) {
			RankedObject biggestAnomaly = (RankedObject)deviants.last();
//  			Iterator i = deviants.iterator();
//  			System.out.print("deviants: " );
//  			while (i.hasNext()) {
//  			    System.out.print(((RankedObject)i.next()).getRank() + " ");
//  			}
//  			System.out.println("####");

			double anomalyThreshold =
			    0.75 * biggestAnomaly.getRank();

//  			if( Math.abs(anomalyThreshold) > deviation ) {
  			if( anomalyThreshold > deviation ) {
			    SortedSet tailSet =
				deviants.tailSet( new RankedObject( anomalyThreshold, null ));

			    boolean reportAnomaly = true;
			    
			    String errmsg = null;
  			    if (anomalyThreshold < deviation * 2) {
				if( tailSet.size() < 2 ) {
				    errmsg = "*** tailSet size < 2";
				    reportAnomaly = false;
				}

				Iterator tIter = tailSet.iterator();
				while( tIter.hasNext() ) {
				    RankedObject ro = (RankedObject)tIter.next();
				    if( !ro.getValue().equals( biggestAnomaly.getValue() )) {
					errmsg = "*** other reason";
					reportAnomaly = false;
				    }
				}
  			    }

			    if( reportAnomaly ) {
				Record r = new Record( Collections.singleton( biggestAnomaly ));
				outputCollection.setRecord( key, r );
				System.err.println( "PeerAnomalies: reporting anomaly" );
				System.err.println( "\t biggestAnomaly = " + biggestAnomaly );
				System.err.println( "\t tailSet = " + tailSet );
				System.err.println( "\t deviantSet = " + deviants );
			    } 
			    else {
				System.err.println( "PeerAnomalies: not reporting anomaly (" + errmsg + ")");
				System.err.println( "\t biggestAnomaly = " + biggestAnomaly );
				System.err.println( "\t tailSet = " + tailSet );
				System.err.println( "\t deviantSet = " + deviants );
			    }
			} 
		    } 

		} // end while
	    }

	}

    }

    


}
