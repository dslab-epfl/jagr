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

// marked for release 1.0

import java.util.*;
import roc.pinpoint.tracing.*;
import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;

/**
 * This plugin sorts observations by an attribute (by requestid, by default )
 * The output collection contains one Record per request id, and these
 * records hold a Set of all the observations with the same request id.
 *
 */
public class SortObservations implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String SORT_BY_ARG = "sortBy";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for observations in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection. this plugin will place requests in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( SORT_BY_ARG,
		       "sort observations by given comma-separated attribute.  null means sort by requestid",
		       PluginArg.ARG_LIST,
		       false,
		       "" )
    };

    private RecordCollection inputRecordCollection;
    private RecordCollection outputRecordCollection;
    private Collection sortBy;

    private Timer timer;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {
        inputRecordCollection = (RecordCollection)
	    args.get(INPUT_COLLECTION_NAME_ARG);
        outputRecordCollection = (RecordCollection)
	    args.get(OUTPUT_COLLECTION_NAME_ARG);
	sortBy = (Collection)args.get( SORT_BY_ARG );

        inputRecordCollection.registerListener(this);
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
    public void addedRecord(String collectionName, Record obsRecord) {
	synchronized( outputRecordCollection ) {
		Observation obs = (Observation) obsRecord.getValue();

		Object idx = null;
		
		if( sortBy.size() == 0 ) {
		    idx = obs.requestId;
		    if( obs.requestId == null ) {
			return;
		    }
		}
		else {
		    idx = IdentifiableHelper.ReduceMap( obs.originInfo, sortBy );
		}

		Record destRecord =
		    outputRecordCollection.getRecord( idx );

		if (destRecord == null) {
		    destRecord = new Record();
		    // if we're sorting them by requestid 
		    //      keep observations in order with a TreeSet
		    Set set = ( sortBy.size() == 0 )
			? ( (Set)new TreeSet() )
			: ( (Set)new HashSet() );

		    destRecord.setValue( set );
		}

		Set observationsSet = (Set)destRecord.getValue();

		observationsSet.add( obs );
		outputRecordCollection.setRecord(idx, destRecord);

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
	    //System.err.println( "SortObservations.TimerTask" );
	    String isReady = (String)inputRecordCollection.getAttribute( "isReady" );
	    if(( isReady == null ) || (!isReady.equals( "true" ))) {
		// not ready
		return;
	    }

	    outputRecordCollection.setAttribute( "isReady", "true" );
	    timer.cancel();
	}
    }


}
