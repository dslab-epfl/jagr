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
package roc.pinpoint.analysis.plugins2.anomalies;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;
import roc.pinpoint.analysis.pcfg.Grammar;
import roc.pinpoint.analysis.structure.Path;
import roc.pinpoint.analysis.structure.RankedObject;
import roc.pinpoint.tracing.Observation;

/**
 * This plugin compares the paths in the input collection against the
 * normal behavior represented in the pcfgCollection.  The outputcollection
 * holds a SortedSet of the paths processed to-date, and their scores.
 *
 * @author emrek
 *
 */
public class PCFGDetector implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_ARG = "inputCollection";
    public static final String REF_COLLECTION_ARG = "pcfgCollection";
    public static final String OUTPUT_COLLECTION_ARG = "outputCollection";
    
    public static final String SENSITIVITY_ARG = "sensitivity";
    public static final String ONLINE_ARG = "online";

    PluginArg[] args =
    {
	new PluginArg(
		      INPUT_COLLECTION_ARG,
		      "input collection.  this plugin compares the paths in the input collection against a PCFG",
		      PluginArg.ARG_RECORDCOLLECTION,
		      true,
		      null),
	new PluginArg(
		      OUTPUT_COLLECTION_ARG,
		      "output collection. this plugin will place anomalous paths into this record collection",
		      PluginArg.ARG_RECORDCOLLECTION,
		      true,
		      null),
	new PluginArg(
		      REF_COLLECTION_ARG,
		      "output collection. this plugin will look for the reference PCFG in this record collection",
		      PluginArg.ARG_RECORDCOLLECTION,
		      true,
		      null),
	new PluginArg( SENSITIVITY_ARG,
		       "threshold for marking a path as anomalous: higher is less sensitive and marks fewer paths as bad; lower is more sensitive and marks more paths as bad",
		       PluginArg.ARG_DOUBLE,
		       true,
		       null ),
	new PluginArg( ONLINE_ARG,
		       "set to 'true' to work online",
		       PluginArg.ARG_BOOLEAN,
		       false,
		       "false" )
    };
    
    RecordCollection inputCollection;
    RecordCollection refCollection;
    RecordCollection outputCollection;
    
    double sensitivity;

    AnalysisEngine engine;
    
    private Timer timer;
    private boolean online;
    
    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.Plugin#getPluginArguments()
     */
    public PluginArg[] getPluginArguments() {
	return args;
    }
    
    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.Plugin#start(java.lang.String, java.util.Map, roc.pinpoint.analysis.AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine)
	throws PluginException {
	inputCollection = (RecordCollection) args.get(INPUT_COLLECTION_ARG);
	outputCollection = (RecordCollection) args.get(OUTPUT_COLLECTION_ARG);
	refCollection = (RecordCollection) args.get(REF_COLLECTION_ARG);
	
	sensitivity = ((Double)args.get(SENSITIVITY_ARG)).doubleValue();
	online = ((Boolean)args.get( ONLINE_ARG )).booleanValue();

	this.engine = engine;
	
	inputCollection.registerListener( this );

	timer = new Timer(true);
	timer.schedule( new MyCheckReady(), 0, 1000 );
    }
    
    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() throws PluginException {
	inputCollection.unregisterListener( this );
	timer.cancel();
    }
    

    Grammar getGrammar( Set requesttypes ) {
	Grammar ret;

	Record wa = null;

	Iterator iter = requesttypes.iterator();
	while( iter.hasNext() && (wa == null) ) {
	    Object type = iter.next();

	    wa = refCollection.getRecord( type );
	}

	if( wa != null ) {
	    Map wamap = (Map)wa.getValue();
	    Record pcfgrec = (Record)wamap.get( "PCFG" );
	    ret = (Grammar)pcfgrec.getValue();
	}
	else {
	    // we're probably not using a workload-adjusted pcfg
	    Record pcfgrec = (Record)refCollection.getRecord( "PCFG" );
	    if( pcfgrec != null ) 
		ret = (Grammar)pcfgrec.getValue();
	    else {
		System.err.println( "pcfgdetector.getGrammar(): could not find pcfg for requesttype " + requesttypes );
		ret = null;
	    }
	}

	return ret;
    }

    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(java.lang.String, java.util.List)
     */
    public void addedRecord(String collectionName, Record rec) {
	    Path p = (Path) rec.getValue();

	    Grammar pcfg = getGrammar( p.getRequestTypes() );
	    if( pcfg == null ) {
		System.err.println( "****** ACK! NO PCFG FOUND!!!" + 
				    " for path id " + p.getRequestId() );
                return;
	    }

	    int branchingfactor = pcfg.getBranchingFactor();

	    System.err.println( "pcfgdetector: BEGIN checking path; requestid = " +
				p.getRequestId() );

	    double probability = pcfg.getScore(p);
	    
	    System.err.println( "pcfgdetector: " + p.getRequestId() + "; probability= " + probability );
	    
	    if( probability > sensitivity ) {
		System.err.println( "pcfgdetector: FOUND ERROR: " + p.getRequestId() );
		System.err.println( "pcfgdetector: " + p.toString() );
		Observation err = new Observation();
		err.eventType = Observation.EVENT_ERROR;
		err.rawDetails.put( "errordescr", 
				    "path shape probability fell below threshold" );
		p.addError( err );
	    }
	    else {
		System.err.println( "pcfgdetector: no error: " + p.getRequestId() );
	    }
	    
	    Record outrec = outputCollection.getRecord( "pcfganomalies" );
	    if( outrec == null ) {
		outrec = new Record( new TreeSet() );
	    }
	    
	    SortedSet s = (SortedSet)outrec.getValue();
	    
	    RankedObject ro = new RankedObject( probability, p );
	    
	    s.add( ro );
	    
	    outputCollection.setRecord( "pcfganomalies", outrec );
	    
	    System.err.println( "pcfgdetector: END checking path; requestid = " +
				p.getRequestId() );


    }
    
    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(java.lang.String, java.util.List)
     */
    public void removedRecords(String collectionName, List items) {
	
    }


    class MyCheckReady extends TimerTask {

	public void run() {
	    System.err.println( "pcfgdetector timer task" );
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
