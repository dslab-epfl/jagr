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
import roc.pinpoint.analysis.structure.*;

/**
 * rank components by their distance from the target component
 * @author emrek@cs.stanford.edu
 *
 */
public class ComponentPerformanceAnomalies implements Plugin {

    public static final String INPUT_COLLECTION_ARG = "inputCollection";
    public static final String HISTORICAL_COLLECTION_ARG = "historicalCollection";
    public static final String OUTPUT_COLLECTION_ARG = "outputCollection";
    public static final String DETECTION_PERIOD_ARG = "detectionPeriod";
    public static final String DEVIATION_ARG = "deviation";
    public static final String DEFINING_ATTRIBUTES_ARG = "definingAttributes";
    public static final String ONLINE_ARG = "online";


    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_ARG,
		       "input collection.  this plugin will look for (ComponentBehavior) possible performance anomalies in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( HISTORICAL_COLLECTION_ARG,
		       "historical collection.  this plugin will look for historical comparisons (GrossComponentBehaviors) in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_ARG,
		       "output collection. this plugin will place anomalies in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( DEFINING_ATTRIBUTES_ARG,
		       "comma-separated component 'defining attributes'. the plugin uses these attributes to define logical components.",
		       PluginArg.ARG_LIST,
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
		       "0.2" ),
	new PluginArg( ONLINE_ARG,
		       "set to 'true' to work online",
		       PluginArg.ARG_BOOLEAN,
		       false,
		       "false" )
    };

    private RecordCollection inputCollection;
    private RecordCollection historicalCollection;
    private RecordCollection outputCollection;

    private Collection definingAttributes;

    private long detectionPeriod;
    private double deviation;

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
        outputCollection = (RecordCollection) args.get(OUTPUT_COLLECTION_ARG);
 	detectionPeriod = ((Integer)args.get(DETECTION_PERIOD_ARG)).intValue();
	deviation = ((Double)args.get(DEVIATION_ARG)).doubleValue();
	definingAttributes = (List)args.get( DEFINING_ATTRIBUTES_ARG );
	online = ((Boolean)args.get( ONLINE_ARG )).booleanValue();


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
	    System.err.println( "ComponentPerformanceAnomalies Plugin pass starting..." );

	    Set results = new TreeSet();

	    synchronized( inputCollection ) {
		Map records = inputCollection.getAllRecords();
		int size = records.size();

		outputCollection.clearAllRecords();

		Iterator iter = records.keySet().iterator();
		while( iter.hasNext() ) {
		    Object key = iter.next();
		    
		    Record rec = (Record)records.get( key );
		    ComponentBehavior test = (ComponentBehavior)rec.getValue();

		    Map id = test.getId();
		    Map logicalId = IdentifiableHelper.ReduceMap( id, definingAttributes );
		    
		    Record deviantRec = historicalCollection.getRecord( logicalId );
		    if( deviantRec == null ) { 
			System.err.println( "No GrossComponentBehavior found for " + logicalId );
			results.add( new RankedObject( 100, test ));
		    }
		    else {
			GrossComponentBehavior deviantContainer = (GrossComponentBehavior)deviantRec.getValue();
			double dev = deviantContainer.getPerformanceDeviation( test );
			System.err.println( "adding component " + test.getId() + " with deviancy " + dev );
			results.add( new RankedObject( dev, test ));
		    }
		}
	    }

	    outputCollection.clearAllRecords();
	    Record r = new Record( results );
	    outputCollection.setRecord( "deviants", r );

	    System.err.println( "ComponentPerformanceAnomalies Plugin pass finished." );

            String inIsReady = (String)inputCollection.getAttribute( "isReady" );
            if( !online && (inIsReady != null) && (inIsReady.equals( "true" ))) {
                timer.cancel();
            }
	}

    }

    


}
