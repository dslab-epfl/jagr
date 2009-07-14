package roc.pinpoint.analysis.plugins2.paths;

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
import roc.pinpoint.analysis.structure.Component;
import roc.pinpoint.tracing.Observation;


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
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        inputRecordCollection.unregisterListener(this);
    }


    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(
     *  String,  List)
     */
    public void addedRecords(String collectionName, List items) {
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            Record rec = (Record) iter.next();
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

		String stage = (String) obs.attributes.get("stage");
		if ("METHODCALLBEGIN".equals(stage)) {
		    // at each METHOD_BEGIN, push new component 
		    //      info onto the stack
		    pushComponent( stackCurrComponent, obs.originInfo );
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
    }


    void pushComponent( List stack, Map allCompAttrs ) {
	Map comp = Component.ReduceMap( allCompAttrs, definingAttributes );
	stack.add( comp );
    }


    boolean popComponent( List stack, Map allCompAttrs ) {
	if( stack.size() <= 0 )
	    return false;

	Map comp = Component.ReduceMap( allCompAttrs, definingAttributes );
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

}
