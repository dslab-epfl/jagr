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
package roc.pinpoint.analysis.plugins2.correlation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.dtree.DTree;
import roc.pinpoint.analysis.dtree.DTreeRule;
import roc.pinpoint.analysis.structure.Path;
import roc.pinpoint.analysis.structure.RankedObject;

/**
 * This plugin uses decision-tree learning to find the correlation
 * between many failing paths.  It waits for the inputCollection to be
 * complete before it will read a set of Paths from its
 * inputCollection and runs them through the decision-tree.
 * 
 * In addition to outputting the results of the decision-tree analysis
 * into the outputCollection, the decision-tree learning process spits
 * out a lot of stdout that may be useful.
 *
 * As an optimization/hack for off-line analysis, this plugin will
 * call System.exit() once it is finished running.  
 *
 *
 * @author emrek
 *
 */
public class DTreeCorrelation implements Plugin {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String DIAGNOSE_PERIOD_ARG = "diagnosePeriod";
    public static final String ONLINE_ARG = "online";

    PluginArg[] args =
        {
            new PluginArg(
                INPUT_COLLECTION_NAME_ARG,
                "input collection.  this plugin will look for paths in the record collection specified by this argument",
                PluginArg.ARG_RECORDCOLLECTION,
                true,
                null),
            new PluginArg(
                OUTPUT_COLLECTION_NAME_ARG,
                "output collection.  this plugin will place requests in this record collection",
                PluginArg.ARG_RECORDCOLLECTION,
                true,
                null),
            new PluginArg(
                DIAGNOSE_PERIOD_ARG,
                "how often to look through faulty paths and diagnose...",
                PluginArg.ARG_INTEGER,
                true,
                "30000"),
	    new PluginArg( ONLINE_ARG,
		       "set to 'true' to work online",
		       PluginArg.ARG_BOOLEAN,
		       false,
		       "false" )
	};

    private RecordCollection inputCollection;
    private RecordCollection outputCollection;
    private Timer timer;
    private AnalysisEngine engine;
    private int diagnosisPeriod;
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
        inputCollection =
            (RecordCollection) args.get(INPUT_COLLECTION_NAME_ARG);
        outputCollection =
            (RecordCollection) args.get(OUTPUT_COLLECTION_NAME_ARG);
        diagnosisPeriod = ((Integer) args.get(DIAGNOSE_PERIOD_ARG)).intValue();
	online = ((Boolean)args.get( ONLINE_ARG )).booleanValue();

        this.engine = engine;
        timer = new Timer();
        timer.schedule(new MyCorrelationTask(), 0, diagnosisPeriod);
    }

    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() throws PluginException {
	timer.cancel();
    }

    class MyCorrelationTask extends TimerTask {

        public void run() {

      	    System.err.println( "dtree correlation run" );

            String inIsReady = (String)inputCollection.getAttribute( "isReady" );
            if( !online && ((inIsReady == null) || (!inIsReady.equals( "true" )))) {
                System.out.println( "DTreeCorrelation: inputCollection is not ready yet" );
                return;
            }


            Set paths = new HashSet();
        
	    synchronized( inputCollection ) {
		Map records = inputCollection.getAllRecords();
		int size = records.size();
		
                Iterator iter = records.values().iterator();
                while( iter.hasNext()) {
		    Record rec = (Record)iter.next();
		    Collection c = (Collection)rec.getValue();

		    Iterator iter2 = c.iterator();
		    while( iter2.hasNext() ) {
			RankedObject ro = (RankedObject)iter2.next();
			Path p = (Path)ro.getValue();
			
			paths.add(p);
		    }
		}
	    }	

	    if( paths.size() != 0 ) {
		System.err.println( "dtree correlation generating rule" );
		
		Set rules = DTree.DiagnosePathFailures( paths );
	    
		String tempDiagnosis = "";
		if( rules != null ) {
		    Iterator iter = rules.iterator();
		    while( iter.hasNext() ) {
			DTreeRule r = (DTreeRule)iter.next();
			tempDiagnosis += r.toString() + "\n";
		    }
		}
		else {
		    tempDiagnosis = "no diagnosis found";
		}
	    
		Record rec = new Record( tempDiagnosis );
		outputCollection.setRecord( "diagnosis", rec );

		System.err.println( "dtree correlation done generating rule: " + tempDiagnosis );
	    }

	    outputCollection.setAttribute( "isReady", "true" );

	    if( !online ) {
		timer.cancel(); // now that collection is ready, no need to keep running
	    }

	    System.err.println( "dtree correlation run finished" );
        }

    }
}




