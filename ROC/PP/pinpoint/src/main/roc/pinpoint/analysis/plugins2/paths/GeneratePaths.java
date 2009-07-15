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
package roc.pinpoint.analysis.plugins2.paths;

// marked for release 1.0

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;
import roc.pinpoint.analysis.plugins2.observations.sql.SqlParserException;
import roc.pinpoint.analysis.plugins2.observations.sql.SqlStatement;
import roc.pinpoint.analysis.structure.*;
import roc.pinpoint.tracing.Observation;

import org.apache.log4j.Logger;

/**
 * This plugin generates rpc.pinpoint.analysis.structure.Path
 * instances from a complete request trace, represented by a Set of
 * Observations.
 * 
 * The input collection should be formatted as one Record per request
 * trace, with a complete set of observations for that request trace.
 * 
 * @author emrek
 *  
 */
public class GeneratePaths implements Plugin, RecordCollectionListener {

    static Logger log = Logger.getLogger( "GeneratePaths" );

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String DEFINING_ATTRIBUTES_ARG = "definingAttributes";
    public static final String IGNORE_ERROR_REPORTS_ARG = "ignoreErrorReports";

    PluginArg[] args =
        {
            new PluginArg(
                INPUT_COLLECTION_NAME_ARG,
                "input collection.  this plugin will look for requesttraces in the record collection specified by this argument",
                PluginArg.ARG_RECORDCOLLECTION,
                true,
                null),
            new PluginArg(
                OUTPUT_COLLECTION_NAME_ARG,
                "output collection. this plugin will place the generated links into the record collection specified by this argument",
                PluginArg.ARG_RECORDCOLLECTION,
                true,
                null),
            new PluginArg(
                DEFINING_ATTRIBUTES_ARG,
                "comma-separated component 'defining attributes'. the plugin uses these attributes to define where to separate or aggregate links the request traces take.",
                PluginArg.ARG_LIST,
                true,
                null),
            new PluginArg(
                IGNORE_ERROR_REPORTS_ARG,
                "if true, reports of errors (e.g., exceptions) will be ignored",
                PluginArg.ARG_BOOLEAN,
                false,
                "true")};

    private RecordCollection inputCollection;
    private RecordCollection outputCollection;

    private Collection definingAttributes;
    private boolean ignoreErrors;
    
    private AnalysisEngine engine;

    private Timer timer;

    public PluginArg[] getPluginArguments() {
        return args;
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#start(String, Map,
     *      AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {
        this.engine = engine;

        inputCollection =
            (RecordCollection) args.get(INPUT_COLLECTION_NAME_ARG);
        outputCollection =
            (RecordCollection) args.get(OUTPUT_COLLECTION_NAME_ARG);

        definingAttributes = (List) args.get(DEFINING_ATTRIBUTES_ARG);
        ignoreErrors = ((Boolean)args.get(IGNORE_ERROR_REPORTS_ARG)).booleanValue();
        
        inputCollection.registerListener(this);
        timer = new Timer();
        timer.schedule(new MyCheckReady(), 0, 1000);
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        inputCollection.unregisterListener(this);
        timer.cancel();
    }

    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(String,
     *      List)
     */
    public void addedRecord(String collectionName, Record rec) {

            SortedSet requestTrace = (SortedSet) rec.getValue();

            Path path = new Path();
            Path.PathNode pathNode = null;

            Iterator obsIter = requestTrace.iterator();
            int lastseqnum = -1;
            while (obsIter.hasNext()) {
                Observation obs = (Observation) obsIter.next();

                if (obs.sequenceNum != lastseqnum + 1) {
                    // request trace is not complete
                    break;
                }

                path.setRequestId(obs.requestId);

                if (obs.eventType == Observation.EVENT_COMPONENT_USE) {
                    lastseqnum = obs.sequenceNum;

                    String stage = (String) obs.attributes.get("stage");
                    if ("METHODCALLBEGIN".equals(stage)
                        || "METHODCALLATOMIC".equals(stage)) {

                        Path.PathNode newNode = processComponentCall(path, pathNode, obs);

                        if ("METHODCALLBEGIN".equals(stage)) {
                            pathNode = newNode;
                        }
                        
                    }
                    else if ("METHODCALLEND".equals(stage)) {
                        // at each METHOD_END, then pop the stack.
                        pathNode = pathNode.getCaller();
                    }
                }
                else if (obs.eventType == Observation.EVENT_ERROR) {
                    if( !ignoreErrors ) {
                        path.addError(obs);
                    }
                }
                else if (obs.eventType == Observation.EVENT_DATABASE_USE) {
                    processDatabaseQuery(path,pathNode,obs);
                }
                else if (obs.eventType == Observation.EVENT_LINK) {
                    path.addPathAssociation(
                        (String) obs.originInfo.get("linktoreqid"));
                }
            }

            Record pathRecord = new Record(path);
            outputCollection.setRecord(path, pathRecord);

    }

    /**
     * @param obs
     */
    private void processDatabaseQuery(Path path, Path.PathNode pathNode,
            				Observation obs) {
        String sql = (String) obs.originInfo.get("name");

        try {

            if (SqlStatement.IsQuery(sql)) {
                // parse the sql, pull out tables accessed
                SqlStatement statement;
                statement = new SqlStatement(sql);
                // add tables as observations back to
                // recordCollection
                Iterator tableIter = statement.getTables().iterator();
                while (tableIter.hasNext()) {
                    String tableName = (String) tableIter.next();
                    Map tableOriginInfo = new HashMap(obs.originInfo);
                    tableOriginInfo.put("name", tableName);
                    tableOriginInfo.put(
                        "dataaccesstype",
                        statement.isRead() ? "READ" : "WRITE");
		    Map attrs = (obs.attributes==null)?
			(new HashMap()):
			(new HashMap(obs.attributes));
                    attrs.put("stage", "METHODCALLATOMIC");

                    // transform database observation into component call observation
                    Component comp = new Component( IdentifiableHelper.ReduceMap(tableOriginInfo,definingAttributes));
                    processComponentCall( path, pathNode, comp );
                }
            }
            else {
                log.warn( "SqlParserPlugin GOT UNRECOGNIZED SQL: " + sql );
            }
            
        }
        catch (SqlParserException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param path
     * @param pathNode
     * @param obs
     * @param stage
     */
    private Path.PathNode processComponentCall(
        Path path,
        Path.PathNode pathNode,
        Observation obs) {
        Component comp =
            new Component(
                IdentifiableHelper.ReduceMap(obs.originInfo, definingAttributes));

        String requestType = (String) obs.originInfo.get("requestclassifier");
        if (requestType != null) {
            path.addRequestType(requestType);
        }

        return processComponentCall(path, pathNode, comp);
    }

    /**
     * @param path
     * @param pathNode
     * @param comp
     * @return
     */
    private Path.PathNode processComponentCall(Path path, Path.PathNode pathNode, Component comp) {
        Path.PathNode newNode = null;

        if (pathNode == null) {
            newNode = path.createRoot(comp);
        }
        else {
            newNode = pathNode.addCallee(comp);
        }

        return newNode;
    }

    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(
     *      String, List)
     */
    public void removedRecords(String collectionName, List items) {
        // ignore...
    }

    class MyCheckReady extends TimerTask {

        public void run() {
	    //System.err.println( "GeneratePaths.TimerTask" );
            String isReady = (String) inputCollection.getAttribute("isReady");
            if ((isReady == null) || (!isReady.equals("true"))) {
                // not ready
                return;
            }

            outputCollection.setAttribute("isReady", "true");
            timer.cancel();
        }
    }

}
