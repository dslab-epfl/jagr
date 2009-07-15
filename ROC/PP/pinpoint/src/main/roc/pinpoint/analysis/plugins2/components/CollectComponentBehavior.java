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

import java.util.Collections;
import java.util.Collection;
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
import roc.pinpoint.analysis.structure.*;
import roc.pinpoint.tracing.Observation;

/**
 * this plugin takes request traces (the sorted observations---not
 * instances of the Path class), and aggregates the ComponentBehavior
 * for each component it sees in the request traces.  Basically, it
 * creates the Link objects that connect ComponentBehaviors together.
 * and counts how often each link is traversed.
 * The output collection contains one Record per component behavior,
 * keyed by the ID of the component.
 *
 * @author emrek
 *
 */
public class CollectComponentBehavior implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String DEFINING_ATTRIBUTES_ARG = "definingAttributes";
    public static final String ONLINE_ARG = "online";

    private static final Map ROOT_ID =
	Collections.singletonMap( "name", "ROOT" );


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
		       null ),
	new PluginArg( DEFINING_ATTRIBUTES_ARG,
		       "comma-separated component 'defining attributes'. the plugin uses these attributes to define where to separate or aggregate links the request traces take.",
		       PluginArg.ARG_LIST,
		       true,
		       null ),
	new PluginArg( ONLINE_ARG,
		       "set to 'true' to work online",
		       PluginArg.ARG_BOOLEAN,
		       false,
		       "false" )	
    };

    private RecordCollection inputCollection;
    private RecordCollection outputCollection;

    private Collection definingAttributes;

    private AnalysisEngine engine;

    private Timer timer;
    private boolean online;


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

	definingAttributes = (List)args.get( DEFINING_ATTRIBUTES_ARG );
	online = ((Boolean)args.get( ONLINE_ARG )).booleanValue();

        inputCollection.registerListener(this);


	timer = new Timer();
	timer.schedule( new MyCheckReady(), 0, 1000 );
	
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        inputCollection.unregisterListener(this);
	timer.cancel();
    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(String,
     * List)
     */
    public void addedRecord(String collectionName, Record rec) {
            SortedSet requestTrace = (SortedSet) rec.getValue();

            // keep track of the current component in a stack.
            List stackCurrComponent = new LinkedList();
	    // keep stack track of component-related observations (to get timing info)
	    List stackObservations = new LinkedList();


	    Component root = 
		new Component( ROOT_ID );
	    stackCurrComponent.add( root );

            Iterator obsIter = requestTrace.iterator();
            int lastseqnum = -1;
            while (obsIter.hasNext()) {
                Observation obs = (Observation) obsIter.next();

		// This request trace has presumably been confirmed already,
		//     but we'll check anyway.
		if (obs.sequenceNum != lastseqnum + 1 ) {
		    break;
		}
		else {
		    lastseqnum = obs.sequenceNum;

		    String stage = (String) obs.attributes.get("stage");
		    if ("METHODCALLBEGIN".equals(stage)) {
			// at each METHOD_BEGIN, add a link from 
			//   curr component to the next.
			Component callee = 
			    new Component( IdentifiableHelper.ReduceMap( obs.originInfo, 
								definingAttributes ));
		      
			stackCurrComponent.add( callee );
			stackObservations.add( obs );
		    }
		    else if ("METHODCALLATOMIC".equals(stage)) {
			Component callee = 
			    new Component( IdentifiableHelper.ReduceMap( obs.originInfo, 
								definingAttributes ));
			if (!stackCurrComponent.isEmpty()) {
			    addComponentBehavior( (Component)stackCurrComponent.get(stackCurrComponent.size() - 1),
						  callee,
						  1.0,  // we have no timing info for these...
						  obs.requestId );
			}		      
			//stackCurrComponent.add( callee );			
		    }
		    else if ("METHODCALLEND".equals(stage)) {
			// at each METHOD_END, then pop the stack.
			Component callee = (Component)stackCurrComponent.remove(stackCurrComponent.size() - 1);
			Observation beginObs = (Observation)stackObservations.remove(stackObservations.size() - 1 );
			if( !stackCurrComponent.isEmpty() ) {
			    Component caller = (Component)stackCurrComponent.get(stackCurrComponent.size() - 1);

			    double weight = obs.originTimestamp - beginObs.originTimestamp;
			    addComponentBehavior( caller,
						  callee,
						  weight,
						  obs.requestId );
			}

		    }
		}

            }

    }


    Record getComponentBehaviorRecord( Component comp ) {
        Record ret = outputCollection.getRecord( comp );
        if (ret == null) {
	    ComponentBehavior cb = new ComponentBehavior( comp );
	    ret = new Record( cb );
	}    
	return ret;
    }


    void addComponentBehavior( Component srcComponent, Component sinkComponent,
			       double weight,
			       Object requestId ) {

	if( !srcComponent.matchesId( ROOT_ID )) {
	    Record srcRec = getComponentBehaviorRecord( srcComponent );
	    ComponentBehavior srcCB = (ComponentBehavior)srcRec.getValue();
	    srcCB.addLinkToSink( sinkComponent, weight, requestId );
	    outputCollection.setRecord( srcComponent, srcRec );
	}

	Record sinkRec = getComponentBehaviorRecord( sinkComponent );
	ComponentBehavior sinkCB = (ComponentBehavior)sinkRec.getValue();
	sinkCB.addLinkToSrc( srcComponent, weight, requestId );
	outputCollection.setRecord( sinkComponent, sinkRec );
    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(
     *   String,  List)
     */
    public void removedRecords(String collectionName, List items) {
        // ignore...
    }

    class MyCheckReady extends TimerTask {

	public void run() {
	    //System.err.println( "CollectComponentBehavior.TimerTask" );
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
