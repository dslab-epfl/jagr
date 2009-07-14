package roc.pinpoint.analysis.plugins.correlation;

import java.util.*;

import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.RankedObject;
import roc.pinpoint.analysis.clustering.*;
import roc.pinpoint.tracing.Observation;

/**
 * rank components by their distance from the target component
 * @author emrek@cs.stanford.edu
 *
 */
public class RankNearestComponents implements Plugin {


    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String CLUSTER_ATTRIBUTE_ARG = "clusterAttribute";
    public static final String TARGET_COMPONENT_ARG = "targetComponent";
    public static final String RANK_PERIOD_ARG = "rankPeriod";
    public static final String DISTANCE_COEFF_ARG = "distancecoeff";
    public static final String SENSITIVITY_ARG = "sensitivity";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for observations in the record collection specified by this argument",
		       PluginArg.ARG_STRING,
		       true,
		       "componenttraces" ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection. this plugin will place requests in this record collection",
		       PluginArg.ARG_STRING,
		       true,
		       "correlations" ),
	new PluginArg( CLUSTER_ATTRIBUTE_ARG,
		       "cluster attribute.  this specifies what attribute of an observation is used as an 'identifier' of equivalent observations. if this is null (default) plugin uses the requestid.  another useful value is 'name' (the component name).",
		       PluginArg.ARG_STRING,
		       false,
		       null ),
	new PluginArg( TARGET_COMPONENT_ARG,
		       "no description",
		       PluginArg.ARG_STRING,
		       true,
		       "failure" ),
	new PluginArg( RANK_PERIOD_ARG,
		       "ranking period.  this plugin will run the ranking algorithm once every period...  unit is milliseconds.",
		       PluginArg.ARG_INTEGER,
		       true,
		       "30000" ),
	new PluginArg( DISTANCE_COEFF_ARG,
		       "distance coefficient to use while ranking",
		       PluginArg.ARG_STRING,
		       true,
		       "jacard" ),
	new PluginArg( SENSITIVITY_ARG,
		       " sensitivity threshold (when to stop ranking)",
		       PluginArg.ARG_DOUBLE,
		       true,
		       "1.0" )
    };



    /** allowed distance coefficient names */
    public static final String[] DISTANCE_COEFF_NAMES =
        {
            "simplematch",
            "yule",
            "jacard",
            "hamman",
            "sorenson",
            "commoncount" };

    /** integer ids for each distance coefficient */
    public static final int[] DISTANCE_COEFF_IDS =
        {
            ClusterElement.SIMPLE_MATCH_COEFF,
            ClusterElement.YULE_COEFF,
            ClusterElement.JACARD_COEFF,
            ClusterElement.HAMMAN_COEFF,
            ClusterElement.SORENSON_COEFF,
            ClusterElement.COMMON_COUNT_COEFF };

    private String inputCollectionName;
    private String outputCollectionName;

    private String clusterAttribute;
    private String targetComponent;
    private long rankPeriod;


    private int distanceCoeff;
    private int clusterMethod;
    private double sensitivity;

    private Timer timer;

    private AnalysisEngine engine;


    private int arrayIndexOf(Object[] arr, Object o) {
        for (int i = 0; i < arr.length; i++) {
            if (o == null ? arr[i] == null : o.equals(arr[i])) {
                return i;
            }
        }
        return -1;
    }

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {
	inputCollectionName = (String) args.get(INPUT_COLLECTION_NAME_ARG);
        outputCollectionName = (String) args.get(OUTPUT_COLLECTION_NAME_ARG);
        targetComponent = (String)args.get( TARGET_COMPONENT_ARG );
        clusterAttribute = (String) args.get(CLUSTER_ATTRIBUTE_ARG);
	rankPeriod = ((Integer)args.get(RANK_PERIOD_ARG)).intValue();
	sensitivity = ((Double)args.get(SENSITIVITY_ARG)).doubleValue();

        String sDistanceCoeff = (String) args.get(DISTANCE_COEFF_ARG);
        distanceCoeff =
            DISTANCE_COEFF_IDS[arrayIndexOf(
                DISTANCE_COEFF_NAMES,
                sDistanceCoeff)];

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

	   
            RecordCollection inputRecordCollection =
                engine.getRecordCollection(inputCollectionName);
            RecordCollection outputRecordCollection =
                engine.getRecordCollection(outputCollectionName);

            if (inputRecordCollection == null) {
                System.err.println(
                    "ERROR: Could not find "
                        + "input collection: "
                        + inputCollectionName);
                return;
            }
            if (outputRecordCollection == null) {
                System.err.println(
                    "ERROR: Could not find "
                        + "output collection: "
                        + outputCollectionName);
                return;
            }

            Map records = new HashMap(inputRecordCollection.getAllRecords());

            int size = records.size();

            Map idToIndex = new HashMap();
            int currIndex = 0;
            int maxIndex = 100;	    
 
	    List clusterElementsList = new LinkedList();
	    ClusterElement targetComponentElement = null;

	    // step 1. prepare the cluster elements based on the
            //         observations stored in the input collection.

            Iterator recordIter = records.keySet().iterator();
            while (recordIter.hasNext()) {
                String key = (String) recordIter.next();
                Record r = (Record) records.get(key);
                Set traces = (Set) r.getValue();

                boolean[] clusterElAttributes = new boolean[maxIndex];

                Iterator traceIter = traces.iterator();
                while (traceIter.hasNext()) {
                    Observation obs = (Observation) traceIter.next();

                    int idx;
                    String obsClusterAttribute;
                    if (clusterAttribute == null) {
                        obsClusterAttribute = obs.requestId;
                    }
                    else {
                        obsClusterAttribute =
                            (String) obs.originInfo.get(clusterAttribute);
                    }

                    if (idToIndex.containsKey(obsClusterAttribute)) {
                        idx =
                            ((Integer) idToIndex.get(obsClusterAttribute))
                                .intValue();
                    }
                    else {
                        // need to create new obsClusterAttribute -> idx
                        // mapping, and maybe grow maxIndex & size of
                        // clusterElAttributes
                        idx = currIndex;
                        currIndex++;
                        if (idx >= maxIndex) {
                            // grow array
                            boolean[] temp = clusterElAttributes;
			    maxIndex = maxIndex * 2;
                            clusterElAttributes = new boolean[maxIndex];
                            System.arraycopy(
                                temp,
                                0,
                                clusterElAttributes,
                                0,
                                temp.length);
                        }

                        idToIndex.put(obsClusterAttribute, new Integer(idx));
                    }

                    clusterElAttributes[idx] = true;
                }

                // todo copy down clusterElAttributes to smaller array.

                ClusterElement ce = new ClusterElement(clusterElAttributes);
                ce.setName(key);
                clusterElementsList.add(ce);

		if( key == targetComponent ) {
		    targetComponentElement = ce;
		}
            }	    

	    // step 2. 
	    // for each item in clusterElementsList, find its distance from
	    // the target components.  add to a sorted set...
	    SortedSet results = new TreeSet();
		
	    Iterator iter = clusterElementsList.iterator();
	    while( iter.hasNext()) {
		ClusterElement ce = (ClusterElement)iter.next();
		float distance = targetComponentElement.calculateDistance( ce,
									   distanceCoeff );

		RankedObject ro = new RankedObject( distance, ce );
		results.add( ro );
	    }

	    // step 3.
	    // the sorted elements are in 'results'. output this to
	    // the record collection.
	    
	    outputRecordCollection.clearAllRecords();
	    Record r = new Record( results );
	    outputRecordCollection.setRecord( "distanceResults", r );
	}

    }


}
