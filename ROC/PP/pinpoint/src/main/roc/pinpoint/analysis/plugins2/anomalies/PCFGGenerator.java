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
import java.util.Timer;
import java.util.TimerTask;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;

import roc.pinpoint.analysis.pcfg.*;
import roc.pinpoint.analysis.structure.Path;

/**
 * This plugin generates a PCFG based on the paths in the inputCollection.
 * As each new Path comes into the input collection, it updates the PCFG
 * in the output collection.
 *
 * the numPathsPerPCFG argument is currently ignored.
 *
 * @author emrek
 *
 */
public class PCFGGenerator implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_ARG = "outputCollection";
    public static final String NUM_PATHS_ARG = "numPathsPerPCFG";

    PluginArg[] args =
        {
            new PluginArg(
                INPUT_COLLECTION_ARG,
                "input collection.  this plugin generate a PCFG based on the paths in the input collection",
                PluginArg.ARG_RECORDCOLLECTION,
                true,
                null),
            new PluginArg(
                OUTPUT_COLLECTION_ARG,
                "output collection. this plugin will place the created PCFG into this record collection",
                PluginArg.ARG_RECORDCOLLECTION,
                true,
                null),
            new PluginArg(
                NUM_PATHS_ARG,
                "How many paths to wait for before generating a PCFG",
                PluginArg.ARG_INTEGER,
                false,
                "1")};

    RecordCollection inputCollection;
    RecordCollection outputCollection;

    int numPathsPerPCFG;

    Grammar currentPCFG;
    int currentCount;

    AnalysisEngine engine;

    Timer timer;

    public PCFGGenerator() {
	timer = new Timer();
    }

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
        numPathsPerPCFG = ((Integer) args.get(NUM_PATHS_ARG)).intValue();

        this.engine = engine;
        currentCount = 0;
        currentPCFG = null;

	inputCollection.registerListener( this );

	timer.schedule( new MyCheckReady(), 0, 1000 );
    }

    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() throws PluginException {
	inputCollection.unregisterListener( this );
	timer.cancel();
    }

    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(java.lang.String, java.util.List)
     */
    public void addedRecord(String collectionName, Record rec) {
        if (currentPCFG == null) {
            currentPCFG = new Grammar();
        }

        Path p = (Path) rec.getValue();
        currentPCFG.addPath(p);
        currentCount++;

        if (currentCount > numPathsPerPCFG) {
            Record outrec = new Record(currentPCFG);
            outputCollection.setRecord("PCFG", outrec);
	    //            currentPCFG = null;
        }
    }

    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(java.lang.String, java.util.List)
     */
    public void removedRecords(String collectionName, List items) {
    }

    class MyCheckReady extends TimerTask {

	public void run() {
	    //System.err.println( "PCFGGenerator.TimerTask" );

	    String isReady = (String)inputCollection.getAttribute( "isReady" );
	    if(( isReady == null ) || (!isReady.equals( "true" ))) {
		// System.err.println( "PCFGGenerator: input isn't ready yet" );
		// not ready
		return;
	    }

	    System.err.println( "PCFGGenerator: input IS READY" );

	    outputCollection.setAttribute( "isReady", "true" );
	    timer.cancel();
	}
    }

}
