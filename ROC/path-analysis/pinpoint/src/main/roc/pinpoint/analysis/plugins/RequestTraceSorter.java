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
 * sorts requests by requestsclassifier 
 *
 */
public class RequestTraceSorter implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for observations in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "requeststraces" ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection. this plugin will place requests in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "sortedrequests" )
    };

    private RecordCollection inputRecordCollection;
    private RecordCollection outputRecordCollection;

    private AnalysisEngine engine;
  
    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String name, Map args, AnalysisEngine engine) {

        this.engine = engine;

        inputRecordCollection =
            (RecordCollection) args.get(INPUT_COLLECTION_NAME_ARG);
        outputRecordCollection =
            (RecordCollection) args.get(OUTPUT_COLLECTION_NAME_ARG);
    
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
     * @see roc.pinpoint.analysis.RecordCollectionListener#
     * addedRecords(String, List)
     */
    public void addedRecords(String collectionName, List items) {    

	Iterator iter = items.iterator();
	while( iter.hasNext() ) {
	    Record rec = (Record)iter.next();
	    Set requestTrace = (Set)rec.getValue();

	    Set requestClassifiers = new HashSet();

	    Iterator observationsIter =
		requestTrace.iterator();
	    while( observationsIter.hasNext() ) {
		Observation obs = (Observation)observationsIter.next();
		
		String classifier = 
		    (String)obs.originInfo.get( "requestclassifier" );
		if( classifier != null )
		    requestClassifiers.add( classifier );
	    }

	    Iterator classifierIter = 
		requestClassifiers.iterator();
	    while( classifierIter.hasNext() ) {
		String classifier = (String)classifierIter.next();

		Record requestsRecord = 
		    (Record)outputRecordCollection.getRecord( classifier );
		Set requestSet = (Set)requestsRecord.getValue();
		
		requestSet.add( requestTrace );
		outputRecordCollection.setRecord( classifier, requestsRecord );
	    }

	}
    }

    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(
     *  String,  List)
     */
    public void removedRecords(String collectionName, List items) {
        // ignore...
    }

}

