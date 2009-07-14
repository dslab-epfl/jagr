package roc.pinpoint.analysis.plugins2.anomalies;

import java.util.*;

import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;

/**
 * rank components by their distance from the target component
 * @author emrek@cs.stanford.edu
 *
 */
public class HistoricalAnomalies implements Plugin {

    public static final String INPUT_COLLECTION_ARG = "inputCollection";
    public static final String HISTORICAL_COLLECTION_ARG = "historicalCollection";
    public static final String OUTPUT_COLLECTION_ARG = "outputCollection";
    public static final String DETECTION_PERIOD_ARG = "detectionPeriod";
    public static final String DEVIATION_ARG = "deviation";
    public static final String DEFINING_ATTRIBUTES_ARG = "definingAttributes";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_ARG,
		       "input collection.  this plugin will look for (Identifiable) possible anomalies in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( HISTORICAL_COLLECTION_ARG,
		       "historical collection.  this plugin will look for historical comparisons (Deviants) in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_ARG,
		       "output collection. this plugin will place anomalies in this record collection",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( DEFINING_ATTRIBUTES_ARG,
		       "comma-separated component 'defining attributes'. the plugin uses these attributes to define logical components.",
		       PluginArg.ARG_LIST,
		       true,
		       null ),
	new PluginArg( DETECTION_PERIOD_ARG,
		       "detection period.  this plugin will run the anomaly detection every period...  unit is milliseconds.",
		       PluginArg.ARG_INTEGER,
		       false,
		       "30000" ),
	new PluginArg( DEVIATION_ARG,
		       "unacceptable deviation threshold",
		       PluginArg.ARG_DOUBLE,
		       false,
		       "2" )
    };

    private RecordCollection inputCollection;
    private RecordCollection historicalCollection;
    private RecordCollection outputCollection;

    private Collection definingAttributes;

    private long detectionPeriod;
    private double deviation;

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
        historicalCollection = (RecordCollection) args.get(HISTORICAL_COLLECTION_ARG);
        outputCollection = (RecordCollection) args.get(OUTPUT_COLLECTION_ARG);
 	detectionPeriod = ((Integer)args.get(DETECTION_PERIOD_ARG)).intValue();
	deviation = ((Double)args.get(DEVIATION_ARG)).doubleValue();
	definingAttributes = (List)args.get( DEFINING_ATTRIBUTES_ARG );

	this.engine = engine;
        timer = new Timer();
        timer.schedule(new MyAnomalySearchingTask(), 0, detectionPeriod);
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        timer.cancel();
    }
    

    class MyAnomalySearchingTask extends TimerTask {

	public void run() {
	    System.err.println( "HistoricalAnomalies Plugin pass starting..." );

	    Set results = new TreeSet();

	    synchronized( inputCollection ) {
		Map records = inputCollection.getAllRecords();
		int size = records.size();

		outputCollection.clearAllRecords();

		Iterator iter = records.keySet().iterator();
		while( iter.hasNext() ) {
		    Object key = iter.next();
		    
		    Record rec = (Record)records.get( key );
		    Identifiable test = (Identifiable)rec.getValue();

		    Map id = test.getId();
		    Map logicalId = Component.ReduceMap( id, definingAttributes );
		    
		    Record deviantRec = historicalCollection.getRecord( logicalId );
		    if( deviantRec == null ) { 
			System.err.println( "No GrossComponentBehavior found for " + logicalId );
			results.add( new RankedObject( 100, test ));
		    }
		    else {
			Deviants deviantContainer = (Deviants)deviantRec.getValue();
			
			deviantContainer.setAcceptableDeviation( deviation );
		    
			if( deviantContainer.isDeviant( test )) {
			    double dev = deviantContainer.getDeviation( test );
			    
			    //System.err.println( "adding component " + test.getId() + " with deviancy " + dev );
			    results.add( new RankedObject( dev, test ));
			}
		    }
		}
	    }

	    outputCollection.clearAllRecords();
	    Record r = new Record( results );
	    outputCollection.setRecord( "deviants", r );

	    System.err.println( "HistoricalAnomalies Plugin pass finished." );
	}

    }

    


}
