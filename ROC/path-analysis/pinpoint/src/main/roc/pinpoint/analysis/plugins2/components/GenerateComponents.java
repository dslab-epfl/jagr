package roc.pinpoint.analysis.plugins2.components;

import java.util.*;
import roc.pinpoint.tracing.*;
import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;

public class GenerateComponents implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String COMPONENT_ATTR_ARG = "componentDefinition";
  
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
	new PluginArg( COMPONENT_ATTR_ARG,
		       "define components based on the given comma-separated list of attributes",
		       PluginArg.ARG_LIST,
		       true,
		       null )
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
	sortBy = (Collection)args.get( COMPONENT_ATTR_ARG );

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


		if( obs.eventType == Observation.EVENT_COMPONENT_USE ) {
		    Map attrs =  Component.ReduceMap( obs.originInfo, sortBy );
		    Component c = new Component( attrs );

		    Record destRecord =
			outputRecordCollection.getRecord( c.getId() );

		    if (destRecord == null) {
			destRecord = new Record();
			destRecord.setValue( c );
			outputRecordCollection.setRecord(c.getId(), destRecord);
		    }
		}
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
