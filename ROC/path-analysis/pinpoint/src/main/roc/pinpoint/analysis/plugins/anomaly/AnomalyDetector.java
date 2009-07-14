package roc.pinpoint.analysis.plugins.anomaly;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;

/**
 * this plugin detects structural anomalies in the links between components
 * @author emrek
 *
 */
public class AnomalyDetector implements Plugin {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String DETECTION_PERIOD_ARG = "detectionPeriod";
    public static final String DEFINING_ATTRIBUTES_ARG = "definingAttributes";
    public static final String MAX_REPORTED_ANOMALIES_ARG = "maxReportedAnomalies";

    PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection. this plugin will read records from the collection specified by this argument",
		       PluginArg.ARG_STRING,
		       true,
		       "links" ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection.  this plugin will place reports of anomalies in the record collection specified by this argument.",
		       PluginArg.ARG_STRING,
		       true,
		       "anomalies" ),
	new PluginArg( DETECTION_PERIOD_ARG,
		       "detection period.  this argument specifies how often this plugin should look for anomalies, in milliseconds",
		       PluginArg.ARG_INTEGER,
		       true,
		       "60000" ),
	new PluginArg( DEFINING_ATTRIBUTES_ARG,
		       "defining attributes.  this argument specifies what attributes define 'equivalent nodes' which should be compared to one another.",
		       PluginArg.ARG_LIST,
		       true,
		       "name" ),
	new PluginArg( MAX_REPORTED_ANOMALIES_ARG,
		       "how many anomalies to report",
		       PluginArg.ARG_INTEGER,
		       true,
		       "10" )
    };

    private String inputCollectionName;
    private String outputCollectionName;

    private List definingAttributes;
    private long detectionPeriod;
    private int maxReportedAnomalies;

    private AnalysisEngine engine;
    private Timer timer;


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
	detectionPeriod = ((Integer)args.get(DETECTION_PERIOD_ARG)).intValue();
	definingAttributes = (List)args.get(DEFINING_ATTRIBUTES_ARG);

	maxReportedAnomalies = ((Integer)args.get(MAX_REPORTED_ANOMALIES_ARG)).intValue();

        timer = new Timer();
        timer.schedule(new MyAnomalyDetector(), 0, detectionPeriod);
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        timer.cancel();
    }

    class MyAnomalyDetector extends TimerTask {

        Map getMapSubset(Map m, Collection keys) {
            Map ret = new HashMap();
            Iterator keyIter = keys.iterator();
            while (keyIter.hasNext()) {
                Object k = keyIter.next();
                ret.put(k, m.get(k));
            }

            return ret;
        }

        Map getMapExclusion( Map m, Collection keys ) {
            Map ret = new HashMap();
            Iterator keyIter = m.keySet().iterator();
            while( keyIter.hasNext() ) {
                Object k = keyIter.next();
                if( !keys.contains( k )) {
                    ret.put( k, m.get( k ));
                }
            }
            return ret;
        }


        public void run() {

	    RecordCollection inputRecordCollection =
                engine.getRecordCollection(inputCollectionName);
            RecordCollection outputRecordCollection =
                engine.getRecordCollection(outputCollectionName);

      
            Map components = new HashMap();

            parseRawLinks(inputRecordCollection, components);

            searchForAnomalies(components, outputRecordCollection );
        }

        void searchForAnomalies(Map components, 
				RecordCollection outputRecordCollection ) {
            Iterator componentIter = components.values().iterator();


	    TreeSet biggestAnomalies = new TreeSet();
	    
	    System.err.println( "AD: INFO: number of components = " 
				+ components.size() );
	    

	    outputRecordCollection.clearAllRecords();

            while( componentIter.hasNext() ) {
                ComponentInfo ci = (ComponentInfo)componentIter.next();
                
		
		System.err.println( "AD: NumLinks[" + ci.attrs.toString()
				    + "] = " +
				    ci.links.size() );
		

                Iterator linkIter = ci.links.keySet().iterator();
                while( linkIter.hasNext() ) {
                    Object k = linkIter.next();
                    
                    StatInfo overallStatInfo = (StatInfo)ci.links.get( k );
                 
		    
		    System.err.println( "AD: NumMinorStats[" + k.toString() 
					+ "] = " + 
					overallStatInfo.values.size() );
		    

		    
		    System.err.println( "AD: OVERALL STATINFO: = " +
					overallStatInfo.toString() );
		    
   
		    Set s = overallStatInfo.getSortedMinorStats();

		    biggestAnomalies.addAll( overallStatInfo.getSortedMinorStats() );
		    while( biggestAnomalies.size() > maxReportedAnomalies ) {
			biggestAnomalies.remove( biggestAnomalies.first() );
		    }
			

		    System.err.println( "AD: OVERALL STATINFO AFTER SORTING: = " +
					overallStatInfo.toString() );

		    System.err.println( "AD: SORTED MINORSTATS = " 
					+ s.toString() );

		}
            }

	    outputRecordCollection.setRecord( "anomaly",
					      new Record( biggestAnomalies ));
        }

        void parseRawLinks(RecordCollection inputRecordCollection, Map components) {
            Iterator linkIter =
                inputRecordCollection.getAllRecords().values().iterator();
                
            while (linkIter.hasNext()) {
		Record rec = (Record)linkIter.next();
		Link link = (Link)rec.getValue();
                Map srcAttrs =
                    getMapSubset(
                        link.getSrcComponentAttributes(),
                        definingAttributes);
                Map srcExtraAttrs =
                    getMapExclusion( 
                        link.getSrcComponentAttributes(),
                        definingAttributes );
                Map sinkAttrs =
                    getMapSubset(
                        link.getSinkComponentAttributes(),
                        definingAttributes);
            
                ComponentInfo ci = (ComponentInfo)components.get( srcAttrs );

		if( ci == null ) {
		    ci = new ComponentInfo();
		    ci.attrs = new HashMap( srcAttrs );
		    components.put( ci.attrs, ci );
		    //System.err.print( "a" );
		}
		else {
		    //System.err.print( "b" );
		}

                ci.addLinkInfo( srcExtraAttrs, sinkAttrs, link.getStatInfo() );
            }
        }
        
    }

}
