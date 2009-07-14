package roc.pinpoint.analysis.plugins;

import java.util.HashMap;
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
 * This plugin calculates the state dependencies different kinds of requests
 * have..
 * @author emrek
 *
 */
public class StateDependencyAnalyzer
    implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for request traces in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "requesttraces" ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.  this plugin will place state dependency information in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "statedependencies" )
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

    void getStateDependenciesFromRequestInfo(
        Set requestTrace,
        Map requestClassifiers,
        Set currWriteDependencies,
        Set currReadDependencies) {
        // iterate over all observations made in this request
        //    and 
        Iterator observationIter = requestTrace.iterator();
        while (observationIter.hasNext()) {
            Observation obs = (Observation) observationIter.next();

            String requestclassifier =
                (String) obs.originInfo.get("requestclassifier");

            boolean alreadyProcessed =
                obs.attributes.containsKey("STATEDEP_PROCESSED");

            if (requestclassifier != null) {
                requestClassifiers.put(
                    requestclassifier,
                    new Boolean(alreadyProcessed));
                // we need to remember whether we've seen this requestid
                //   before so we don't artificially inflate the 
                //   requestcount for this type.  *but* we still need
                //   to remember the request id so that we
                //   can add new state ids to the correct statedependency
                //   structures...
            }

	    /** todo fix the 'already processed' thing.  it's just not working.
            if (alreadyProcessed) {
                continue;
            }
	    **/

            obs.attributes.put("STATEDEP_PROCESSED", "");
            String table = (String) obs.originInfo.get("name");
            String accesstype = (String) obs.originInfo.get("dataaccesstype");
            if ("READ".equals(accesstype)) {
                //System.err.println("read dep: " + table);
                currReadDependencies.add(table);
            }
            else if ("WRITE".equals(accesstype)) {
                //System.err.println("write dep: " + table);
                currWriteDependencies.add(table);
            }

        }
    }

    void outputStateDependencies(
        Map requestClassifiers,
        Set currWriteDependencies,
        Set currReadDependencies) {

        // add the state dependencies to the output record collection

        Iterator requestClassifiersIter =
            requestClassifiers.keySet().iterator();
        while (requestClassifiersIter.hasNext()) {
            String requestClassifier = (String) requestClassifiersIter.next();
            Record rec = outputRecordCollection.getRecord(requestClassifier);
            StateDependency statedep;
            if (rec == null) {
                statedep = new StateDependency(requestClassifier);
                rec = new Record(statedep);
            }
            else {
                statedep = (StateDependency) rec.getValue();
            }

            if (requestClassifiers
                .get(requestClassifier)
                .equals(Boolean.FALSE)) {
                statedep.incrementTotalOccurences();
            }

            // record write dependencies
            Iterator writeIter = currWriteDependencies.iterator();
            while (writeIter.hasNext()) {
		String dep = (String)writeIter.next();
		System.err.println( "write dep: " + requestClassifier 
				    + "/" + dep );
                statedep.incrementStateAccess(dep, false);
            }

            // record read dependencies
            Iterator readIter = currReadDependencies.iterator();
            while (readIter.hasNext()) {
		String dep = (String)readIter.next();
		System.err.println( "dep: " + requestClassifier 
				    + "/" + dep );
                statedep.incrementStateAccess(dep, true);
            }

            outputRecordCollection.setRecord(requestClassifier, rec);
        }

    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#
     * addedRecords(String, List)
     */
    public void addedRecords(String collectionName, List items) {
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            Record rec = (Record) iter.next();

            if (!(rec.getValue() instanceof Set)) {
                System.err.println(
                    "StateDependencyAnalyzer:"
                        + " OOPS! record is not a Set.  it is "
                        + rec.getValue().getClass().toString());
            }

            Set requestTrace = (Set) rec.getValue();

            Map requestClassifiers = new HashMap();
            Set currWriteDependencies = new HashSet();
            Set currReadDependencies = new HashSet();

            getStateDependenciesFromRequestInfo(
                requestTrace,
                requestClassifiers,
                currWriteDependencies,
                currReadDependencies);

            if (!requestClassifiers.isEmpty()) {

                outputStateDependencies(
                    requestClassifiers,
                    currWriteDependencies,
                    currReadDependencies);
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
