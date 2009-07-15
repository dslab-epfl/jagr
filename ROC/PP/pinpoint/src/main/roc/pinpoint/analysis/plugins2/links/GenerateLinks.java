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
package roc.pinpoint.analysis.plugins2.links;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;
import roc.pinpoint.analysis.structure.*;
import roc.pinpoint.tracing.Observation;

/**
 * this plugin takes request traces, and generates links among components...
 * @author emrek
 *
 */
public class GenerateLinks implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String DEFINING_ATTRIBUTES_ARG = "definingAttributes";

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
		       null )
    };

    private RecordCollection inputCollection;
    private RecordCollection outputCollection;

    private Collection definingAttributes;

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

	definingAttributes = (List)args.get( DEFINING_ATTRIBUTES_ARG );

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

            SortedSet requestTrace = (SortedSet) rec.getValue();

            // keep track of the current component in a stack.
            List stackCurrComponent = new LinkedList();

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
			Component comp = 
			    new Component( IdentifiableHelper.ReduceMap( obs.originInfo, 
								definingAttributes ));
			
			if (!stackCurrComponent.isEmpty()) {
			    addLink( (Component)stackCurrComponent.get(stackCurrComponent.size() - 1),
				    comp,
				    obs.requestId );
			}

			stackCurrComponent.add( comp );
		    }
		    else if ("METHODCALLEND".equals(stage)) {
			// at each METHOD_END, then pop the stack.
			stackCurrComponent.remove(stackCurrComponent.size() - 1);
		    }
		}

            }

    }



    void addLink( Component srcComponent, Component sinkComponent,
		  Object requestId ) {

	Link l = new Link( srcComponent, sinkComponent );

        Record rec = outputCollection.getRecord( l );
        if (rec == null) {
            rec = new Record( l );
	}

        Link link = (Link) rec.getValue();
        link.addValue( 1, requestId );

        outputCollection.setRecord( link, rec);
    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(
     *   String,  List)
     */
    public void removedRecords(String collectionName, List items) {
        // ignore...
    }

}
