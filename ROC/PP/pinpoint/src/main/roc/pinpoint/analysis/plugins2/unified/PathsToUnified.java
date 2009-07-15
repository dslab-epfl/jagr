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
package roc.pinpoint.analysis.plugins2.unified;

import java.util.*;
import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;

public class PathsToUnified implements Plugin, RecordCollectionListener {


    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";

    private static final Map START_ID =
	Collections.singletonMap( "name", "ROOT" );

    PluginArg[] args = {
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.  this plugin will load query log records into a record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will load query log records into a record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null )
    };



    RecordCollection inputCollection;
    RecordCollection outputCollection;

    public PluginArg[] getPluginArguments() {
        return args;
    }

    public void start( String id, Map args, AnalysisEngine engine ) 
        throws PluginException {

        inputCollection = (RecordCollection)
            args.get( INPUT_COLLECTION_NAME_ARG );
        outputCollection = (RecordCollection)
            args.get( OUTPUT_COLLECTION_NAME_ARG );
    
        inputCollection.registerListener( this );
    }


    public void stop() {
        inputCollection.unregisterListener( this );
    }
    
    public void addedRecord( String collectionName, Record rec ) {
        Path path = (Path) rec.getValue();
        processPath( path );
    }

    Component getTransitionComponent( String requestid, int counter ) {
        Map transitionId = new HashMap(2);
        transitionId.put( "requestid", requestid );
        transitionId.put( "transitionid", new Integer(counter) );
        
        Component transitionComp = new Component( transitionId );        

        return transitionComp;
    }

    int processPath( Path path ) {
        String requestId = path.getRequestId();
        int counter = 0;

        Component transitionComp = getTransitionComponent( requestId, counter++ );

        addUnifiedNodeBehavior( new Component( START_ID ),
                                transitionComp );

        Path.PathNode rootNode = path.getRootNode();
        
        Component rootComp = rootNode.getComponent();
        addUnifiedNodeBehavior( transitionComp, rootComp );
        counter = processPathNode( rootNode, requestId, counter ); 
                
        return counter;
    }
                     

    int processPathNode( Path.PathNode currNode,
                         String requestId,
                         int counter ) {

        Component transitionComp = getTransitionComponent( requestId, counter++ );
        Component currComp = currNode.getComponent();

        addUnifiedNodeBehavior( currComp, transitionComp );

        List callees = currNode.getCallees();
        Iterator iter = callees.iterator();
        while( iter.hasNext() ) {
            Path.PathNode next = (Path.PathNode)iter.next();

            Component callee = next.getComponent();
            addUnifiedNodeBehavior( transitionComp, callee );

            counter = processPathNode( next, requestId, counter );
        }
        
        return counter;    
    }

    void addUnifiedNodeBehavior( Component caller, Component callee ) {

        Record srcRec = getComponentBehaviorRecord( caller );
        ComponentBehavior srcCB = (ComponentBehavior)srcRec.getValue();
        srcCB.addLinkToSink( callee );
        outputCollection.setRecord( caller, srcRec );
    
	Record sinkRec = getComponentBehaviorRecord( callee );
	ComponentBehavior sinkCB = (ComponentBehavior)sinkRec.getValue();
	sinkCB.addLinkToSrc( caller );
	outputCollection.setRecord( callee, sinkRec );
    }

    Record getComponentBehaviorRecord( Component comp ) {
        Record ret = outputCollection.getRecord( comp );
        if (ret == null) {
	    ComponentBehavior cb = new ComponentBehavior( comp );
	    ret = new Record( cb );
	}    
	return ret;
    }


    public void removedRecords( String collectionName, List items ) {
        // do nothing
    }
    

}
