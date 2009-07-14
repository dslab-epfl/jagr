package roc.pinpoint.analysis.plugins2.observations;

import java.util.*;
import roc.pinpoint.tracing.*;
import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;

public class SortObservations implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String SORT_BY_ARG = "sortBy";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for observations in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection. this plugin will place requests in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( SORT_BY_ARG,
		       "sort observations by given comma-separated attribute.  null means sort by requestid",
		       PluginArg.ARG_LIST,
		       false,
		       "" )
    };

    private RecordCollection inputRecordCollection;
    private RecordCollection outputRecordCollection;
    private Collection sortBy;

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
	sortBy = (Collection)args.get( SORT_BY_ARG );

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

		Object idx = null;
		
		if( sortBy.size() == 0 ) {
		    idx = obs.requestId;
		}
		else {
		    idx = Component.ReduceMap( obs.originInfo, sortBy );
		}

		Record destRecord =
		    outputRecordCollection.getRecord( idx );

		if (destRecord == null) {
		    destRecord = new Record();
		    // if we're sorting them by requestid 
		    //      keep observations in order with a TreeSet
		    Set set = ( sortBy.size() == 0 )
			? ( (Set)new TreeSet() )
			: ( (Set)new HashSet() );

		    destRecord.setValue( set );
		}

		Set observationsSet = (Set)destRecord.getValue();

		observationsSet.add( obs );
		outputRecordCollection.setRecord(idx, destRecord);
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
