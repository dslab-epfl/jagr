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
public class FilterRanked implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_ARG = "outputCollection";
    public static final String NUM_CUTOFF_ARG = "numCutoff";
    public static final String FILTER_CUTOFF_ARG = "filterCutoff";
    public static final String MIN_TIME_ELAPSED_ARG = "minTime";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_ARG,
		       "input collection.  this plugin will look for RankedObjects in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_ARG,
		       "output collection. this plugin will place the filtered anomalies in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( NUM_CUTOFF_ARG,
		       "Take at most the top NUM ranked components",
		       PluginArg.ARG_INTEGER,
		       true,
		       null ),
	new PluginArg( FILTER_CUTOFF_ARG,
		       "Ignore all components ranked lower than this cutoff",
		       PluginArg.ARG_DOUBLE,
		       true,
		       null ),
	new PluginArg( MIN_TIME_ELAPSED_ARG,
		       "Ignore all components until at least this many ms have passed",
		       PluginArg.ARG_INTEGER,
		       false,
		       "0" )
    };

    private RecordCollection inputCollection;
    private RecordCollection outputCollection;
    private int numCutoff;
    private RankedObject filterCutoff;

    private long minTimeElapsed;
    private long ignoreAllUntilTime = 0;

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
	numCutoff = ((Integer)args.get( NUM_CUTOFF_ARG )).intValue();
	minTimeElapsed = ((Integer)args.get( MIN_TIME_ELAPSED_ARG )).intValue();

	double dFilterCutoff = ((Double)args.get( FILTER_CUTOFF_ARG )).doubleValue();
	filterCutoff = new RankedObject( dFilterCutoff, null );

	inputCollection.registerListener( this );
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
	inputCollection.unregisterListener( this );
    }
    
    public SortedSet filterRankedSet( SortedSet s ) {

	long currentTime = System.currentTimeMillis();
	
	if( ignoreAllUntilTime == 0 ) {
	    ignoreAllUntilTime = currentTime + minTimeElapsed;
	}

	if( currentTime < ignoreAllUntilTime ) {
	    // ignore everything
	    return new TreeSet();
	}

	//	System.err.println( "filterRankedSet: input = " + s );

	SortedSet ret = s.tailSet( filterCutoff );
	
	/*** BEGIN HACK ***/
	// TODO fix this right in some other plugin
	// only report failures in Petstore EJBs... that begin with "The"
	/*
	Iterator iter = ret.iterator();
	while( iter.hasNext() ) {
	    RankedObject ro = (RankedObject)iter.next();
	    ComponentBehavior cb = (ComponentBehavior)ro.getValue();
	    Component c = cb.getComponent();
	    String name = (String)c.getId().get( "name" );
	    if( !name.startsWith( "The" )) {
		iter.remove();
	    }
	}
	*/
	/*** END HACK ***/

	while( ret.size() > numCutoff ) {
	    ret.remove( ret.last() );
	}

	//	System.err.println( "filterRankedSet: output = " + ret );

	return ret;
    }

    public void filterAndForwardRankedSet( Record rec ) {

	SortedSet s = (SortedSet)rec.getValue();
	
	SortedSet out = filterRankedSet( s );

	if( out.size() > 0 ) {
	    // forward non-empty sets
	    Record outRec = new Record( out );
	    outputCollection.setRecord( "filteredset", outRec );
	}

    }

    public void addedRecord( String collectionname, Record rec ) {
        filterAndForwardRankedSet( rec );
	

    }


    public void removedRecords( String collectionname, List items ) {
	//updateMergedSet();
    }


}
