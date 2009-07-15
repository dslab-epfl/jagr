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
package roc.pinpoint.analysis.plugins2.observations.sql;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;
import roc.pinpoint.tracing.Observation;

import org.apache.log4j.Logger;

/**
 * This plugin parses the sql statements found in observations, and generates
 * observations of DB-table accesses (represented as ATOMIC method calls)
 * to replace them.
 * 
 * @author emrek
 *
 */
public class SqlParserPlugin implements Plugin, RecordCollectionListener {

    static Logger log = Logger.getLogger( "SqlParserPlugin" );

    public static final String INPUT_COLLECTION_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_ARG = "outputCollection";
    public static final String ONLINE_ARG = "online";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_ARG,
		       "input collection of observations",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_ARG,
		       "input collection of observations",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( ONLINE_ARG,
		       "set to 'true' to work online",
		       PluginArg.ARG_BOOLEAN,
		       false,
		       "false" )
    };


    private Timer timer;

    private AnalysisEngine engine;
    private RecordCollection inputCollection;
    private RecordCollection outputCollection;
    private boolean online;


    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String name, Map args, AnalysisEngine engine) {

        this.engine = engine;
        inputCollection = (RecordCollection) args.get(INPUT_COLLECTION_ARG);
        outputCollection = (RecordCollection) args.get(OUTPUT_COLLECTION_ARG);
	online = ((Boolean)args.get( ONLINE_ARG )).booleanValue();

        inputCollection.registerListener(this);
	this.timer = new Timer();
	timer.schedule( new MyCheckReady(), 0, 1000 );
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        inputCollection.unregisterListener(this);
	timer.cancel();
    }

    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(
     * String, List)
     */
    public void addedRecord(String collectionName, Record rec) {
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
			    Map attrs = new HashMap( obs.attributes );
			    attrs.put( "stage", "METHODCALLATOMIC" );

			    
                            Observation tableObs =
                                new Observation(
                                    Observation.EVENT_COMPONENT_USE,
                                    obs.requestId,
                                    obs.sequenceNum,
                                    tableOriginInfo,
                                    obs.rawDetails,
                                    attrs);

			    String k = tableObs.requestId
				+ obs.sequenceNum
				+ tableName;

			    if( outputCollection.getRecord( k ) != null ) {
				throw new RuntimeException( "Record " + k + " already exists!!!!" );
			    }


			    outputCollection.setRecord( k,
							new Record(tableObs));
                        }
                    }
		    else {
			log.warn( "SqlParserPlugin GOT UNRECOGNIZED SQL: " + sql );
		    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
	    else { // if it's not an sql statement
		// forward the observation without modifying it.
		
		String k = obs.requestId 
		    + obs.sequenceNum 
		    + "_" 
		    + obs.collectedTimestamp;
		
		if( outputCollection.getRecord( k ) != null ) {
		    throw new RuntimeException( "Record " + k + " already exists!!!!" );
		}

		outputCollection.setRecord( k,
					    rec );
	    }


    }
    
    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(
     * String, List)
     */
    public void removedRecords(String collectionName, List items) {
        // do nothing
    }


    class MyCheckReady extends TimerTask {

	public void run() {
	    //System.err.println( "SqlParserPlugin.TimerTask" );
	    String isReady = (String)inputCollection.getAttribute( "isReady" );
	    if(( isReady == null ) || (!isReady.equals( "true" ))) {
		// not ready
		return;
	    }

	    outputCollection.setAttribute( "isReady", "true" );
	    timer.cancel();
	}
    }

}
