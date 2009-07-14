package roc.pinpoint.analysis.plugins;

import java.util.List;
import java.util.Map;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;

/**
 * this simple plugin increments a counter for every record added to a record
 * collection
 * @author emrek
 *
 */
public class SimpleCounter implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin counts the records in this collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "observations" )
    };

    private RecordCollection recordCollection;
    private int totalCount = 0;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {
        recordCollection = (RecordCollection) args.get(INPUT_COLLECTION_NAME_ARG);
        recordCollection.registerListener(this);
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        recordCollection.unregisterListener(this);
    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(
     *   String,  List)
     */
    public void addedRecords(String collectionName, List items) {
        totalCount += items.size();
        System.out.println(
            "total of "
                + totalCount
                + " items have entered the collection '"
                + collectionName
                + "'");
    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(
     *   String,  List)
     */
    public void removedRecords(String collectionName, List items) {
        // do nothing
    }

}
