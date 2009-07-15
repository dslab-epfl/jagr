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

import java.util.Iterator;
import java.util.LinkedList;
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
import roc.pinpoint.analysis.structure.IdentifiableHelper;
import roc.pinpoint.tracing.Observation;


/**
 * This plugin checks the Records in the inputCollection, where each
 * record in the input collection contains a set of observations for a
 * request trace.  When this plugin verifies that the input record
 * contains a complete request trace, it moves it to the output
 * collection.  Otherwise, it leaves the record in the input collection,
 * and will scan it again if it is modified.
 *
 *
 */
public class RequestTraceVerifier implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String DEFINING_ATTRIBUTES_ARG = "definingAttributes";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for observations in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.  this plugin will place requests in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( DEFINING_ATTRIBUTES_ARG,
		       "comma-separated component 'defining attributes'. the plugin uses these attributes to define where to separate or aggregate links the request traces take.",
		       PluginArg.ARG_LIST,
		       true,
		       null )
    };

    private RecordCollection inputRecordCollection;
    private RecordCollection outputRecordCollection;

    private List definingAttributes;

    private Timer timer;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {

        inputRecordCollection = (RecordCollection) args.get(INPUT_COLLECTION_NAME_ARG);
        outputRecordCollection = (RecordCollection) args.get(OUTPUT_COLLECTION_NAME_ARG);
        inputRecordCollection.registerListener(this);

	definingAttributes = (List)args.get(DEFINING_ATTRIBUTES_ARG );
	timer = new Timer();
	timer.schedule( new MyCheckReady(), 0, 1000 );
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        inputRecordCollection.unregisterListener(this);
	timer.cancel();
    }


    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(
     *  String,  List)
     */
    public void addedRecord(String collectionName, Record rec) {
            SortedSet requestTrace = (SortedSet) rec.getValue();

            // keep track of the current component in a stack.
            List stackCurrComponent = new LinkedList();

            Iterator obsIter = requestTrace.iterator();
            int lastseqnum = -1;
	    boolean traceComplete = true;

	    String requestId = null;

            while (obsIter.hasNext() && traceComplete) {
                Observation obs = (Observation) obsIter.next();

		if( requestId == null ) {
		    requestId = obs.requestId;
		} else if( !requestId.equals( obs.requestId )) {
		    traceComplete = false;
		    System.err.println( "RequestTraceVerifier.addedRecords():" 
					+ "Warning: requestid doesn't match! " 
					+ "( " + requestId + " != " 
					+ obs.requestId + " )" );
		    break;
		}
		    
		if (obs.sequenceNum != lastseqnum + 1 ) {
		    traceComplete = false;
		    break;
		}

		lastseqnum = obs.sequenceNum;

		Map attributesmap = obs.attributes;
		String stage = (String)((attributesmap!=null)?
					attributesmap.get("stage"):
					null);
		if ("METHODCALLBEGIN".equals(stage)) {
		    // at each METHOD_BEGIN, push new component 
		    //      info onto the stack
		    try {
			pushComponent( stackCurrComponent, obs.originInfo );
			
		    }
		    catch( NullPointerException ex ) {
			ex.printStackTrace();
			System.err.println( "OBSERVATION WAS: " + obs.toString() );
		    }
		    
		}
		else if ("METHODCALLEND".equals(stage)) {
		    // at each METHOD_END, then pop the stack.
		    boolean success = popComponent( stackCurrComponent, 
						    obs.originInfo );
		    if( !success ) {
			// trace not finishe
			traceComplete = false;
			break;
		    }
		}
	    }

	    if( requestTrace.size() == 0 )
		traceComplete = false;

	    if( stackCurrComponent.size() > 0 )
		traceComplete = false;
	    
	    if( traceComplete ) {
		//new Exception().printStackTrace();
		inputRecordCollection.removeRecord( requestId );
		outputRecordCollection.setRecord( requestId, 
					    new Record( requestTrace ));
	    }

    }


    void pushComponent( List stack, Map allCompAttrs ) {
	Map comp = IdentifiableHelper.ReduceMap( allCompAttrs, definingAttributes );
	stack.add( comp );
    }


    boolean popComponent( List stack, Map allCompAttrs ) {
	if( stack.size() <= 0 )
	    return false;

	Map comp = IdentifiableHelper.ReduceMap( allCompAttrs, definingAttributes );
	Map stackComponent = (Map) stack.remove( stack.size() - 1);

	return comp.equals(stackComponent);
    }

    
    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(
     *  String,  List)
     */
    public void removedRecords(String collectionName, List items) {
        // do nothing
    }

    class MyCheckReady extends TimerTask {

	public void run() {
	    //System.err.println( "RequestTraceVerifier.TimerTask" );
	    String isReady = (String)inputRecordCollection.getAttribute( "isReady" );
	    if(( isReady == null ) || (!isReady.equals( "true" ))) {
		// not ready
		return;
	    }

	    outputRecordCollection.setAttribute( "isReady", "true" );
	    this.cancel();
	    timer.cancel();
	}
    }

}
