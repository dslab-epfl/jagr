package roc.pinpoint.analysis.plugins2.observations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;
import roc.pinpoint.tracing.Observation;


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
    }

   /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        inputRecordCollection.unregisterListener(this);
    }
    
   /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(
     *  String,  List)
     */
    public void addedRecords(String collectionName, List items) {
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            Record rec = (Record) iter.next();
            Observation obs = (Observation) rec.getValue();

	    if( obs.eventType == Observation.EVENT_COMPONENT_DETAILS) {
		Object id = obs.originInfo.get( DETAILS_ID );
		detailsMap.put( id, obs.originInfo );
	    }
	    else {
		Object id = obs.originInfo.get( MERGE_WITH_ID );
		Map details = (Map)detailsMap.get( id );
		if( details != null ) {
		    obs.originInfo.putAll( details );
		}
		
		outputRecordCollection.setRecord( obs.requestId
						  + obs.sequenceNum 
						  + "_"
						  + obs.collectedTimestamp,
						  new Record( obs ));
	    }

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
}
