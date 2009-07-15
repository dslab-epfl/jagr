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
package roc.pinpoint.analysis.plugins2.ranked;

import java.util.*;

import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;

/**
 *
 * @author emrek@cs.stanford.edu
 *
 */
public class MergeRankedSets implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_ARG = "outputCollection";
    public static final String OUTPUTID_ARG = "outputId";


    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_ARG,
		       "input collection.  this plugin will look for RankedObjects in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_ARG,
		       "output collection. this plugin will place the merged anomalies in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUTID_ARG,
		       "the id to give the output record being created",
		       PluginArg.ARG_STRING,
		       false,
		       "output" )
    };

    private RecordCollection inputCollection;
    private RecordCollection outputCollection;
    private String outputId;

    private AnalysisEngine engine;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {
	inputCollection = (RecordCollection) args.get(INPUT_COLLECTION_ARG);
        outputCollection = (RecordCollection) args.get(OUTPUT_COLLECTION_ARG);
	outputId = (String)args.get( OUTPUTID_ARG );

	inputCollection.registerListener( this );
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
	inputCollection.unregisterListener( this );
    }
    
    public void addRankedObjects( Set ranked, Map merged ) {
	Iterator iter = ranked.iterator();
	while( iter.hasNext() ) {
	    RankedObject ro = (RankedObject)iter.next();
	    Identifiable id = (Identifiable)ro.getValue();

	    Object key = id.getId();  // merge/normalize ids
	    //Object key = id;  // dont merge/normalize ids

	    MyHelper myh = (MyHelper)merged.get( key );
	    if( myh == null ) {
		myh = new MyHelper();
		myh.id = id;
		merged.put( key, myh );
	    }
	    myh.totalRank += ro.getRank();
	    myh.count++;
	}
    }


    public SortedSet createMergedSet( Map merged ) {
	Iterator iter = merged.keySet().iterator();
	TreeSet ret = new TreeSet();

	while( iter.hasNext() ) {
	    Object key = iter.next();
	    MyHelper myh = (MyHelper)merged.get( key );
	    RankedObject ro = new RankedObject( myh.totalRank / (double)myh.count,
						myh.id );
	    ret.add( ro );
	}

	return ret;
    }

    public void updateMergedSet() {
	Map merged = new HashMap();
	SortedSet out;

	synchronized( inputCollection ) {
	    Map records = inputCollection.getAllRecords();
	    Iterator iter = records.values().iterator();
	    while( iter.hasNext() ) {
		Record rec = (Record)iter.next();
		Set s = (Set)rec.getValue();
		addRankedObjects( s, merged );
	    }

	    out = createMergedSet( merged );
	}

	Record outputRec = new Record( out );
	outputCollection.setRecord( outputId, outputRec );

	outputCollection.setAttribute( "isReady",
				       inputCollection.getAttribute( "isReady" ));
    }

    class MyHelper {
	int count = 0;
	double totalRank = 0;
	Object id;
    }

    public void addedRecord( String collectionname, Record ignore ) {
	// ignore the items, just iterate over the whole record each
	//  there's a change.

	updateMergedSet();
    }


    public void removedRecords( String collectionname, List items ) {
	//updateMergedSet();
    }


}
