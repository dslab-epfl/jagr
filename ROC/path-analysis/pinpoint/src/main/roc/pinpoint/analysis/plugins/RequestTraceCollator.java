package roc.pinpoint.analysis.plugins;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;
import roc.pinpoint.tracing.Observation;

/**
 * RequestTraceCollator collects observations by request id.
 * @author emrek
 *
 */
public class RequestTraceCollator implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for observations in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "observations" ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection. this plugin will place requests in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "requesttraces" )
    };

    private RecordCollection inputRecordCollection;
    private RecordCollection outputRecordCollection;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {

        inputRecordCollection = (RecordCollection)
	    args.get(INPUT_COLLECTION_NAME_ARG);
        outputRecordCollection = (RecordCollection)
	    args.get(OUTPUT_COLLECTION_NAME_ARG);

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

	synchronized( outputRecordCollection ) {
	    while (iter.hasNext()) {
		Record obsRecord = (Record) iter.next();
		Observation obs = (Observation) obsRecord.getValue();
		Record requestTraceRecord =
		    outputRecordCollection.getRecord(obs.requestId);

		if (requestTraceRecord == null) {
		    requestTraceRecord = new Record();
		    requestTraceRecord.setValue(new TreeSet());
		}

		SortedSet requestTrace = (SortedSet)
		    requestTraceRecord.getValue();

		requestTrace.add(obs);

		outputRecordCollection.setRecord(obs.requestId, requestTraceRecord);
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
