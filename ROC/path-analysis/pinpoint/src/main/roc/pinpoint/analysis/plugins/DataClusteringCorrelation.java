package roc.pinpoint.analysis.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.clustering.Cluster;
import roc.pinpoint.analysis.clustering.ClusterElement;
import roc.pinpoint.tracing.Observation;

/**
 * cluster component observations or request traces
 * @author emrek
 *
 */
public class DataClusteringCorrelation implements Plugin {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String CLUSTER_ATTRIBUTE_ARG = "clusterAttribute";
    public static final String CLUSTER_PERIOD_ARG = "clusterPeriod";
    public static final String DISTANCE_COEFF_ARG = "distancecoeff";
    public static final String CLUSTER_METHOD_ARG = "clustermethod";
    public static final String SENSITIVITY_ARG = "sensitivity";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for component traces in the  record collection specified by this argument",
		       PluginArg.ARG_STRING,
		       true,
		       "componenttraces" ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.  this plugin will place the clusters in the  record collection specified by this argument",
		       PluginArg.ARG_STRING,
		       true,
		       "correlations" ),
	new PluginArg( CLUSTER_ATTRIBUTE_ARG,
		       "cluster attribute.  this specifies what attribute of an observation is used as an 'identifier' of equivalent observations. if this is null (default) plugin uses the requestid.  another useful value is 'name' (the component name).",
		       PluginArg.ARG_LIST,
		       false,
		       "" ),
	new PluginArg( CLUSTER_PERIOD_ARG,
		       "cluster period.  this plugin will run the clustering algorithm once every period...  unit is milliseconds.",
		       PluginArg.ARG_INTEGER,
		       true,
		       "30000" ),
	new PluginArg( DISTANCE_COEFF_ARG,
		       "distance coefficient to use while clustering.",
		       PluginArg.ARG_STRING,
		       true,
		       "jacard" ),
	new PluginArg( CLUSTER_METHOD_ARG,
		       "clustering method to use",
		       PluginArg.ARG_STRING,
		       true,
		       "upgma" ),
	new PluginArg( SENSITIVITY_ARG,
		       "data clustering sensitivity algorithm... (when to stop clustering)",
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

    /** allowed cluster methods **/
    public static final String[] CLUSTER_METHOD_NAMES =
        { "upgma", "slink", "clink" };

    /** integer ids for each cluster method **/
    public static final int[] CLUSTER_METHOD_IDS =
        {
            Cluster.UPGMA_CLUSTERMETHOD,
            Cluster.SLINK_CLUSTERMETHOD,
            Cluster.CLINK_CLUSTERMETHOD };

    private String inputCollectionName;
    private String outputCollectionName;

    private String clusterAttribute;
    //    String correlationTarget;
    private long clusterPeriod;

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

        this.engine = engine;

        inputCollectionName = (String) args.get(INPUT_COLLECTION_NAME_ARG);
	outputCollectionName = (String) args.get(OUTPUT_COLLECTION_NAME_ARG);
        
        clusterAttribute = (String) args.get(CLUSTER_ATTRIBUTE_ARG);
        
	clusterPeriod = ((Integer)args.get( CLUSTER_PERIOD_ARG )).intValue();
	
	String sDistanceCoeff = (String) args.get(DISTANCE_COEFF_ARG);
	distanceCoeff = DISTANCE_COEFF_IDS[arrayIndexOf( DISTANCE_COEFF_NAMES,
							 sDistanceCoeff)];

        String sClusterMethod = (String) args.get(CLUSTER_METHOD_ARG);
        clusterMethod = CLUSTER_METHOD_IDS[arrayIndexOf( CLUSTER_METHOD_NAMES,
							 sClusterMethod)];

	sensitivity = ((Integer)args.get( SENSITIVITY_ARG )).doubleValue();

        timer = new Timer();
        timer.schedule(new MyClusteringTask(), 0, clusterPeriod);
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        timer.cancel();
    }

    class MyClusteringTask extends TimerTask {

        public void run() {

            System.err.println("Data Clustering Correlation Plugin pass...");

            // step 0. get the input/output record collections 
            //         

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
            }

            // step 2. prepare the clusters

            List initialClusters = Cluster.BuildClusters(clusterElementsList);

            // step 3. run the clustering algorithm

            List finalClusters =
                Cluster.MergeCluster(
                    initialClusters,
                    clusterMethod,
                    distanceCoeff,
                    (float)sensitivity);

            // step 4. put the output into another record collection.

            outputRecordCollection.clearAllRecords();

            int id = 0;
            Iterator clusterIter = finalClusters.iterator();
            while (clusterIter.hasNext()) {
                Cluster c = (Cluster) clusterIter.next();
                Record r = new Record(c);
                outputRecordCollection.setRecord("id" + id, r);
                id++;
            }

            System.err.println("Data Clustering Pass Done...");
        }

    }

}
