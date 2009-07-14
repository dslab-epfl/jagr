package roc.pinpoint.analysis.plugins2.observations.sql;

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

/**
 * This plugin parses the sql statements found in observations, and generates
 * observations of state accesses
 * @author emrek
 *
 */
public class SqlParserPlugin implements Plugin, RecordCollectionListener {

    public static final String COLLECTION_NAME_ARG = "collectionName";

    PluginArg[] args = {
	new PluginArg( COLLECTION_NAME_ARG,
		       "collection of observations",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "observations" )
    };
		       


    private AnalysisEngine engine;
    private RecordCollection recordCollection;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String name, Map args, AnalysisEngine engine) {

        this.engine = engine;
        recordCollection = (RecordCollection) args.get(COLLECTION_NAME_ARG);
        recordCollection.registerListener(this);
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        recordCollection.unregisterListener(this);
    }

    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(
     * String, List)
     */
    public void addedRecords(String collectionName, List items) {

        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            Record rec = (Record) iter.next();
            Observation obs = (Observation) rec.getValue();
            if (obs.eventType == Observation.EVENT_DATABASE_USE) {
                String sql = (String) obs.originInfo.get("name");

                try {
                    if (SqlStatement.IsQuery(sql)) {
                        // parse the sql, pull out tables accessed
                        SqlStatement statement = new SqlStatement(sql);

                        // add tables as observations back to recordCollection
                        Iterator tableIter = statement.getTables().iterator();
                        while (tableIter.hasNext()) {
                            String tableName = (String) tableIter.next();
                            Map tableOriginInfo = new HashMap(obs.originInfo);
                            tableOriginInfo.put("name", tableName);
                            tableOriginInfo.put(
                                "dataaccesstype",
                                statement.isRead() ? "READ" : "WRITE");
                            Observation tableObs =
                                new Observation(
                                    Observation.EVENT_COMPONENT_USE,
                                    obs.requestId,
                                    obs.sequenceNum,
                                    tableOriginInfo,
                                    obs.rawDetails,
                                    obs.attributes);
			    synchronized( recordCollection ) {
				recordCollection.setRecord( tableObs.requestId
							    + obs.sequenceNum
							    + tableName,
							    new Record(tableObs));
			    }
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(
     * String, List)
     */
    public void removedRecords(String collectionName, List items) {
        // do nothing
    }

}
