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

import java.io.IOException;
import swig.util.XMLException;
import java.util.*;
import roc.pinpoint.analysis.*;
import roc.pinpoint.tracing.*;



public class SortRequestTracesByType implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String NS_OUTPUT_COLLECTION_NAME_ARG = "nsOutputCollection";
    public static final String PREPEND_ARG = "prependName";
    public static final String INPUT_RC_NS_NAME_ARG = "inputCollectionNSName";
    public static final String OUTPUT_RC_NS_NAME_ARG = "outputCollectionNSName";
    public static final String RC_ATTRS_ARG = "collectionAttrs";
    public static final String NS_ARGS_ARG = "nsArgs";
    public static final String NS_CONFIGURATION_XML = "namespaceConfig";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for paths in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( NS_OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.  this plugin will bind this record collection as the output of the namespace",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( PREPEND_ARG,
		       "prepend this name to newly created RecordCollections and Namespaces",
		       PluginArg.ARG_STRING,
		       true,
		       null ),
	new PluginArg( INPUT_RC_NS_NAME_ARG,
		       "bind new record collection to this name in a new namespace",
		       PluginArg.ARG_STRING,
		       true,
		       null ),
	new PluginArg( OUTPUT_RC_NS_NAME_ARG,
		       "bind new record collection to this name in a new namespace",
		       PluginArg.ARG_STRING,
		       true,
		       null ),
	new PluginArg( RC_ATTRS_ARG,
		       "pass these attrs to newly created record collections",
		       PluginArg.ARG_MAP,
		       false,
		       "" ),
	new PluginArg( NS_ARGS_ARG,
		       "pass these arguments to newly create namespaces",
		       PluginArg.ARG_MAP,
		       false,
		       "" ),		       
	new PluginArg( NS_CONFIGURATION_XML,
		       "load this config file to initialize the new per-requesttype namespace",
		       PluginArg.ARG_STRING,
		       true,
		       null )
    };

    private RecordCollection inputRecordCollection;
    private RecordCollection nsOutputRecordCollection;

    String prepend;
    String configfile;

    String inRcNsName;
    String outRcNsName;
    Map rcAttrs;
    Map nsArgs;
    
    AnalysisEngine engine;

    Set createdCollectionNames;

    Timer timer;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {

	this.engine = engine;

        inputRecordCollection = (RecordCollection) args.get(INPUT_COLLECTION_NAME_ARG);
        nsOutputRecordCollection = (RecordCollection) args.get(NS_OUTPUT_COLLECTION_NAME_ARG);

	prepend = (String)args.get( PREPEND_ARG );
	configfile = (String)args.get( NS_CONFIGURATION_XML );

	inRcNsName = (String)args.get( INPUT_RC_NS_NAME_ARG );
	outRcNsName = (String)args.get( OUTPUT_RC_NS_NAME_ARG );
	rcAttrs = (Map)args.get( RC_ATTRS_ARG );
	nsArgs = (Map)args.get( NS_ARGS_ARG );

        inputRecordCollection.registerListener(this);

	createdCollectionNames = new HashSet();

	timer = new Timer();
	timer.schedule( new MyCheckReady(), 0, 1000 );	
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        inputRecordCollection.unregisterListener(this);
    }


    public synchronized RecordCollection getCollectionForRequestType( String id )
	throws IOException, XMLException, AnalysisException {

	String rcName = prepend + id;

	RecordCollection rc = engine.getRecordCollection( rcName );
	if( rc != null ) {
	    return rc;
	}

	// else create the record collection and namespace

	rc = engine.createRecordCollection( rcName, rcAttrs );

	createdCollectionNames.add( rcName );

	// special case attribu
	rcAttrs.put( "id", id );
	nsArgs.put( "id", id );
	AnalysisEngine ns = engine.createNameSpace( rcName, nsArgs );
	ns.bindRecordCollection( inRcNsName, rc );
	ns.bindRecordCollection( outRcNsName, nsOutputRecordCollection );
	ns.loadXMLConfiguration( configfile );
	
	return rc;
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

	    String requestType = null;
	    String requestId = null;

            while (obsIter.hasNext() && (requestType == null) ) {
                Observation obs = (Observation) obsIter.next();
		requestId = obs.requestId;
		requestType = (String)obs.originInfo.get( "requestclassifier" );
	    }

	    if( requestType == null )
                return;

	    try {
		RecordCollection rc = getCollectionForRequestType( requestType );
		Record outrec = new Record( requestTrace );
		rc.setRecord( requestId, outrec );
	    }
	    catch( Exception e ) {
		e.printStackTrace();
		System.err.println( "Continuing..." );
	    }


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
	    //System.err.println( "SortRequestTracesByType.TimerTask" );
	    String isReady = (String)inputRecordCollection.getAttribute( "isReady" );
	    if(( isReady == null ) || (!isReady.equals( "true" ))) {
		// not ready
		return;
	    }

	    Iterator iter = createdCollectionNames.iterator();
	    while( iter.hasNext() ) {
		String rcName = (String)iter.next();
		RecordCollection rc = engine.getRecordCollection( rcName );
		rc.setAttribute( "isReady", "true" );
	    }

	    timer.cancel();
	}
    }

}
