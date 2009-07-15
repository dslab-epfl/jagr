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
package roc.pinpoint.analysis.plugins2.components;

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
import roc.pinpoint.analysis.structure.*;

/**
 * this plugin takes request traces, and generates links among components...
 * @author emrek
 *
 */
public class GenerateFailureComponent implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for requesttraces in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection. this plugin will place the generated links into the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null )
    };

    private RecordCollection inputCollection;
    private RecordCollection outputCollection;

    private AnalysisEngine engine;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {
        this.engine = engine;

        inputCollection = (RecordCollection)
	    args.get(INPUT_COLLECTION_NAME_ARG);
        outputCollection = (RecordCollection)
	    args.get(OUTPUT_COLLECTION_NAME_ARG);

        inputCollection.registerListener(this);
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        inputCollection.unregisterListener(this);
    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(String,
     * List)
     */
    public void addedRecord(String collectionName, Record rec) {

	Record failureRecord = outputCollection.getRecord( "failure" );
	if( failureRecord == null ) {
	    Map failId = new HashMap();
	    failId.put( "name", "failure" );
	    failureRecord = new Record( new UsageTracker( new Component( failId )) );
	}
	UsageTracker failureUT = (UsageTracker)failureRecord.getValue();

        Path p = (Path) rec.getValue();

        failureUT.addUsedWith( p.getRequestId() );
        

	outputCollection.setRecord( "failure", failureRecord );
    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(
     *   String,  List)
     */
    public void removedRecords(String collectionName, List items) {
	// TODO this is a bit of a hack!
	outputCollection.removeRecord( "failure" );

	/*
	Iterator iter = items.iterator();
	
	Record failureRecord = outputCollection.getRecord( "failure" );
	if( failureRecord == null ) {
	    return;
	}
	UsageTracker failureUT = (UsageTracker)failureRecord.getValue();

	while( iter.hasNext() ) {
	    Record rec = (Record)iter.next();
	    Path p = (Path) rec.getValue();
	    failureUT.notUsedWith( p.getRequestId() );
	}

	outputCollection.setRecord( "failure", failureRecord );
	*/
    }

}
