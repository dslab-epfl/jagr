package roc.pinpoint.analysis.plugins2.correlation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
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
import roc.pinpoint.analysis.structure.Distanceable;

/**
 * cluster component observations or request traces
 * @author emrek
 *
 */
public class Clustering implements Plugin {

    public static final String INPUT_COLLECTION_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_ARG = "outputCollection";
    public static final String CLUSTER_PERIOD_ARG = "clusterPeriod";
    public static final String SENSITIVITY_ARG = "sensitivity";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_ARG,
		       "input collection.  this plugin will look for component traces in the  record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "componenttraces" ),
	new PluginArg( OUTPUT_COLLECTION_ARG,
		       "output collection.  this plugin will place the clusters in the  record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       "correlations" ),
	new PluginArg( CLUSTER_PERIOD_ARG,
		       "cluster period.  this plugin will run the clustering algorithm once every period...  unit is milliseconds.",
		       PluginArg.ARG_INTEGER,
		       false,
		       "30000" ),
	new PluginArg( SENSITIVITY_ARG,
		       "data clustering sensitivity algorithm... (when to stop clustering)",
		       PluginArg.ARG_DOUBLE,
		       false,
		       "1.0" )
    };
	
    private RecordCollection inputCollection;
    private RecordCollection outputCollection;

    private long clusterPeriod;
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

        inputCollection = (RecordCollection) args.get(INPUT_COLLECTION_ARG);
	outputCollection = (RecordCollection) args.get(OUTPUT_COLLECTION_ARG);

	clusterPeriod = ((Integer)args.get( CLUSTER_PERIOD_ARG )).intValue();
      	sensitivity = ((Double)args.get( SENSITIVITY_ARG )).doubleValue();

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

	    try {
		List finalClusters;

		synchronized( inputCollection ) {
		    Map records = inputCollection.getAllRecords();
		    List distanceables = new ArrayList( records.values().size() );
		    Iterator iter = records.values().iterator();
		    while( iter.hasNext() ) {
			Record rec = (Record)iter.next();
			distanceables.add( (Distanceable)rec.getValue() );
		    }
		    List initialClusters = Cluster.BuildClusters( distanceables );
		    
		    finalClusters =
			Cluster.MergeCluster( initialClusters, sensitivity);
		    
		}
		
		outputCollection.clearAllRecords();
		
		int id = 0;
		System.err.println( "found " + finalClusters.size() + " clusters" );
		Iterator clusterIter = finalClusters.iterator();
		while (clusterIter.hasNext()) {
		    Cluster c = (Cluster) clusterIter.next();
		    Record r = new Record(c);
		    outputCollection.setRecord("id" + id, r);
		    id++;
		}
	    }
	    catch( Exception e ) {
		e.printStackTrace();
	    }

            System.err.println("Data Clustering Pass Done...");
        }

    }

}
