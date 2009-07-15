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
package roc.pinpoint.analysis.plugins2.observations;

import java.util.*;
import java.io.IOException;
import swig.util.XMLException;
import roc.pinpoint.tracing.*;
import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;

public class ParseAndSeparateStatisticsObservations implements Plugin, RecordCollectionListener {


    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String COMPONENT_DEF_ARG = "componentDefinition";
    public static final String NS_OUTPUT_COLLECTION_NAME_ARG = "nsOutputCollection";
    public static final String PREPEND_ARG = "prependName";
    public static final String INPUT_RC_NS_NAME_ARG = "inputCollectionNSName";
    public static final String OUTPUT_RC_NS_NAME_ARG = "outputCollectionNSName";
    public static final String RC_ATTRS_ARG = "collectionAttrs";
    public static final String NS_ARGS_ARG = "nsArgs";
    public static final String NS_CONFIGURATION_XML = "namespaceConfig";



    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for observations in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( COMPONENT_DEF_ARG,
		       "define component by given comma-seperated attributes.",
		       PluginArg.ARG_LIST,
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
    private List componentDefinition;

    private RecordCollection nsOutputRecordCollection;

    String prepend;
    String configfile;

    String inRcNsName;
    String outRcNsName;
    Map rcAttrs;
    Map nsArgs;
    
    AnalysisEngine engine;


    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {
	this.engine = engine;
        inputRecordCollection = (RecordCollection)
	    args.get(INPUT_COLLECTION_NAME_ARG);
	componentDefinition = (List)
	    args.get(COMPONENT_DEF_ARG );
	nsOutputRecordCollection = (RecordCollection) args.get(NS_OUTPUT_COLLECTION_NAME_ARG);

	prepend = (String)args.get( PREPEND_ARG );
	configfile = (String)args.get( NS_CONFIGURATION_XML );

	inRcNsName = (String)args.get( INPUT_RC_NS_NAME_ARG );
	outRcNsName = (String)args.get( OUTPUT_RC_NS_NAME_ARG );
	rcAttrs = (Map)args.get( RC_ATTRS_ARG );
	nsArgs = (Map)args.get( NS_ARGS_ARG );

        inputRecordCollection.registerListener(this);
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        inputRecordCollection.unregisterListener(this);
    }

    public void addedRecord( String collectionName, Record obsRecord ) {

	    Observation obs = (Observation) obsRecord.getValue();
	    
	    Map comp = IdentifiableHelper.ReduceMap( obs.originInfo, 
					    componentDefinition );

	    Iterator statsKeys = obs.rawDetails.keySet().iterator();
	    while( statsKeys.hasNext() ) {
		String k = (String)statsKeys.next();
		Object o = obs.rawDetails.get( k );
		if( o instanceof Number ) {
		    try {
			addStatistic( comp, k, (Number)o );
		    }
		    catch( Exception ignore ) {
			ignore.printStackTrace();
		    }
		}
	    }		

    }

    public RecordCollection getCollectionForStatistic( String id )
	throws IOException, XMLException, AnalysisException {

	String rcName = prepend + id;

	RecordCollection rc = engine.getRecordCollection( rcName );
	if( rc != null ) {
	    return rc;
	}

	// else create the record collection and namespace

	rc = engine.createRecordCollection( rcName, rcAttrs );

	// special case attribu
	rcAttrs.put( "id", id );
	nsArgs.put( "id", id );
	AnalysisEngine ns = engine.createNameSpace( rcName, nsArgs );
	ns.bindRecordCollection( inRcNsName, rc );
	ns.bindRecordCollection( outRcNsName, nsOutputRecordCollection );
	ns.loadXMLConfiguration( configfile );
	
	return rc;
    }

    public void addStatistic( Map comp, String k, Number n )
	throws IOException, XMLException, AnalysisException {
	
	RecordCollection rc = getCollectionForStatistic( k );

	Record rec = rc.getRecord( comp );
	if( rec == null ) {
	    Statistics s = new Statistics();
	    rec = new Record( s );
	}
	Statistics s = (Statistics)rec.getValue();

	// other option is to *replace* existing number with the new one...
	s.addValue( n.doubleValue(), null );
	rc.setRecord( comp, rec );
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
