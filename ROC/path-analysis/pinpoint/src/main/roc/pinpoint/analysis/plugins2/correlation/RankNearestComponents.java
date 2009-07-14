package roc.pinpoint.analysis.plugins2.correlation;

import java.util.*;

import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;
import roc.pinpoint.tracing.Observation;

/**
 * rank components by their distance from the target component
 * @author emrek@cs.stanford.edu
 *
 */
public class RankNearestComponents implements Plugin {


    public static final String INPUT_COLLECTION_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_ARG = "outputCollection";
    public static final String TARGET_ID_ARG = "targetId";
    public static final String RANK_PERIOD_ARG = "rankPeriod";
    public static final String SENSITIVITY_ARG = "sensitivity";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_ARG,
		       "input collection.  this plugin will look for observations in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_ARG,
		       "output collection. this plugin will place requests in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( TARGET_ID_ARG,
		       "comma-separated key-value pairs, identifying target component",
		       PluginArg.ARG_MAP,
		       true,
		       null ),
	new PluginArg( RANK_PERIOD_ARG,
		       "ranking period.  this plugin will run the ranking algorithm once every period...  unit is milliseconds.",
		       PluginArg.ARG_INTEGER,
		       false,
		       "30000" ),
	new PluginArg( SENSITIVITY_ARG,
		       "sensitivity threshold (when to stop ranking)",
		       PluginArg.ARG_DOUBLE,
		       false,
		       "0.5" )
    };

    private RecordCollection inputCollection;
    private RecordCollection outputCollection;

    private String clusterAttribute;
    private Map targetId;
    private long rankPeriod;

    private double sensitivity;

    private Timer timer;

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
        targetId = (Map)args.get( TARGET_ID_ARG );
	rankPeriod = ((Integer)args.get(RANK_PERIOD_ARG)).intValue();
	sensitivity = ((Double)args.get(SENSITIVITY_ARG)).doubleValue();

	this.engine = engine;
        timer = new Timer();
        timer.schedule(new MyRankingTask(), 0, rankPeriod);
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        timer.cancel();
    }
    

    class MyRankingTask extends TimerTask {

	public void run() {
	    System.err.println( "Rank Nearest Component Plugin pass..." );


	    SortedSet results = new TreeSet();
		
	    synchronized( inputCollection ) {
		Map records = inputCollection.getAllRecords();
		int size = records.size();

		Distanceable target = null;

		// step 1. find target!

		Iterator iter = records.keySet().iterator();
		while( iter.hasNext() ) {
		    Object key = iter.next();
		    
		    Record rec = (Record)records.get( key );
		    Identifiable i = (Identifiable)rec.getValue();
		    if( i.matchesId( targetId )) {
			target = (Distanceable)i;
			break;
		    }
		}

		if( target == null ) {
		    System.err.println( "Rank Nearest Component ABORTING! Target not found: " + targetId.toString() );
		}

		// step 2. rank all objects based on their distance from target
		iter = records.keySet().iterator();
		while( iter.hasNext() ) {
		    Object key = iter.next();
		
		    Record rec = (Record)records.get( key );
		    Distanceable other = (Distanceable)rec.getValue();
		    double distance = target.getDistance( other );
		
		    RankedObject ro = new RankedObject( distance, other );
		    results.add( ro );
		}

		// the sorted elements are in 'results'. output this to
		// the record collection.
	    
	    }// end synchronized

	    outputCollection.clearAllRecords();
	    Record r = new Record( results );
	    outputCollection.setRecord( "distanceResults", r );
	}

    }


}
