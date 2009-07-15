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
package roc.pinpoint.analysis.plugins2.observations;

// marked for release 1.0

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;
import roc.pinpoint.tracing.Observation;

/**
 * This plugin acts as a filter on a stream of observations.  It filters
 * out "details" observations, and holds on to them for some period of
 * time.  Other observations that have the "merge with details id#"
 * attribute set will get their data merged with the additional details
 * previously reported.
 * 
 * the whole point of this is that a lot of the data in a complete
 * observation does not change (like OS and JVM version numbers).  Rather
 * than waste bandwidth, we report those details once per component, then
 * just refer to them in the future with their details id.
 *
 */
public class MergeDetails implements Plugin, RecordCollectionListener {


    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for observations in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "partialrequests" ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection. this plugin will place requests in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "requesttraces" )
    };

    private static final String DETAILS_ID = "detailsId";
    private static final String MERGE_WITH_ID = "mergeWithId";

    private Timer timer;

    private Map detailsMap;

    private RecordCollection inputRecordCollection;
    private RecordCollection outputRecordCollection;
  
    public PluginArg[] getPluginArguments() {
	return args;
    }

    public void start( String id, Map args, AnalysisEngine engine ) {

        inputRecordCollection = (RecordCollection)
	    args.get(INPUT_COLLECTION_NAME_ARG);
        outputRecordCollection = (RecordCollection)
	    args.get(OUTPUT_COLLECTION_NAME_ARG);

	detailsMap = new HashMap();

        inputRecordCollection.registerListener(this);
	timer = new Timer();
	timer.schedule( new MyCheckReady(), 0, 1000 );
    }

   /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        inputRecordCollection.unregisterListener(this);
	timer.cancel();
    }
    
    static int rec_count=0;

   /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(
     *  String,  List)
     */
    public void addedRecord(String collectionName, Record rec) {
            Observation obs = (Observation) rec.getValue();

	    if( obs.eventType == Observation.EVENT_COMPONENT_DETAILS) {
		Object id = obs.originInfo.get( DETAILS_ID );
		detailsMap.put( id, obs.originInfo );
	    }
	    else {
		if( obs.originInfo != null ) {
		    Object id = obs.originInfo.get( MERGE_WITH_ID );
		    Map details = (Map)detailsMap.get( id );
		    if( details != null ) {
			obs.originInfo.putAll( details );
		    }
		}

		String k = obs.requestId + obs.sequenceNum + "_" + obs.collectedTimestamp + (rec_count++) ;

		if( outputRecordCollection.getRecord( k ) != null ) {
		    throw new RuntimeException( "Record " + k + " already exists!!!!" );
		}

		outputRecordCollection.setRecord( k,
						  new Record( obs ));
	    }

    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(
     *  String,  List)
     */
    public void removedRecords(String collectionName, List items) {
        // do nothing
    }


    class MyCheckReady extends TimerTask {

	public void run() {
	    String isReady = (String)inputRecordCollection.getAttribute( "isReady" );
	    if(( isReady == null ) || (!isReady.equals( "true" ))) {
		// not ready
		return;
	    }

	    outputRecordCollection.setAttribute( "isReady", "true" );
	    timer.cancel();
	}
    }
}
