package roc.pinpoint.analysis.plugins;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;
import roc.pinpoint.tracing.Observation;

/**
 * this plugin collects observations into sets, categorized by attributes of the
 * originInfo (for example, where all observations where java.version=1.4, where
 * name=" classX", etc) or by a single, specified attribute of origininfo.
 * 
 * @author emrek
 *
 */
public class ComponentTraceCollator
    implements Plugin, RecordCollectionListener {


    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String ORIGIN_INDEX_KEY_ARG = "originIndexKey";


    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for observations in the  record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "observations" ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.  this plugin will place the collected sets of observations in the  record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "componenttraces" ),
	new PluginArg( ORIGIN_INDEX_KEY_ARG,
		       "origin index key .  this plugin will categorize observations by the named attribute.  If the name is '',  then the component will categorize by all attributes separately.",
		       PluginArg.ARG_STRING,
		       true,
		       "" )
    };



    private RecordCollection inputRecordCollection;
    private RecordCollection outputRecordCollection;

    private String originIndexKey;


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
        originIndexKey = (String) args.get(ORIGIN_INDEX_KEY_ARG);

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
     * add observation into the collection of observations for componentid
     * @param componentId category id
     * @param obs observation
     */
    public void addComponentTrace(String componentId, Observation obs) {
        Record compTraceRecord = outputRecordCollection.getRecord(componentId);

        if (compTraceRecord == null) {
            compTraceRecord = new Record(new HashSet());
        }

        Set componentTrace = (Set) compTraceRecord.getValue();

        componentTrace.add(obs);

        outputRecordCollection.setRecord(componentId, compTraceRecord);
    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(String,
     *  List)
     */
    public void addedRecords(String collectionName, List items) {

        Iterator iter = items.iterator();

        while (iter.hasNext()) {
            Record observationRecord = (Record) iter.next();

            Observation obs = (Observation) observationRecord.getValue();

            if ((obs.eventType == Observation.EVENT_NULL)
                || (obs.eventType == Observation.EVENT_DATABASE_USE)) {
                // don't index direct observations of database use (e.g., sql
                //  statements.  wait for an sql parser to componentize the
                //  sql query into the tables that were accessed.
                continue;
            }

            if ("".equals(originIndexKey)) {
                // index all origin attributes
                Iterator keyIter = obs.originInfo.keySet().iterator();
                while (keyIter.hasNext()) {
                    String key = (String) keyIter.next();
		    Object o = obs.originInfo.get(key);
                    addComponentTrace(
                        key + "_" + (obs.originInfo.get(key)).toString(),
                        obs);
                }
            }
            else {
                // only index the attribute named in originIndexKey
                String value = (String) obs.originInfo.get(originIndexKey);
                addComponentTrace(originIndexKey + "_" + value, obs);
            }
        }
    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecord
     * (String, List)
     */
    public void removedRecords(String collectionName, List items) {
        // do nothing
    }

}
