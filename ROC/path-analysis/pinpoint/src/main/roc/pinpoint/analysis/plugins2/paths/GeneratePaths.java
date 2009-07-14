package roc.pinpoint.analysis.plugins2.paths;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

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
public class GeneratePaths implements Plugin, RecordCollectionListener {

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
    public void addedRecords(String collectionName, List items) {
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            Record rec = (Record) iter.next();
            SortedSet requestTrace = (SortedSet) rec.getValue();

	    Path path = new Path();
	    Path.PathNode pathNode = null;

            Iterator obsIter = requestTrace.iterator();
            int lastseqnum = -1;
            while (obsIter.hasNext()) {
                Observation obs = (Observation) obsIter.next();

		// This request trace has presumably been confirmed already,
		//     but we'll check anyway.
		if (obs.sequenceNum != lastseqnum + 1 ) {
		    break;
		}

		if( obs.eventType == Observation.EVENT_COMPONENT_USE ) {
		    lastseqnum = obs.sequenceNum;

		    String stage = (String) obs.attributes.get("stage");
		    if ("METHODCALLBEGIN".equals(stage)) {

			Component comp = 
			    new Component( Component.ReduceMap( obs.originInfo, 
								definingAttributes ));

			String requestType = 
			    (String)obs.originInfo.get( "requestclassifier" );
			if( requestType != null ) {
			    path.addRequestType( requestType );
			}


			if( pathNode == null ) {
			    pathNode = path.createRoot( comp );
			}
			else {
			    pathNode = pathNode.addCallee( comp );
			}

		    }
		    else if ("METHODCALLEND".equals(stage)) {
			// at each METHOD_END, then pop the stack.
			pathNode = pathNode.getCaller();
		    }
		}
		else if( obs.eventType == Observation.EVENT_ERROR ) {
		    path.addError( obs );
		}
            }

	    Record pathRecord = new Record( path );
	    outputCollection.setRecord( path, pathRecord );
        }
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
