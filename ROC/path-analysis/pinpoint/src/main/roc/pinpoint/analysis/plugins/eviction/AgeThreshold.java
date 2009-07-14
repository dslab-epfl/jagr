package roc.pinpoint.analysis.plugins.eviction;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import roc.pinpoint.analysis.RecordCollectionListener;

/**
 * this plugin implements a simple eviction policy.  it marks the age of records
 * as they enter the record collection, and periodically sweeps through record
 * collections to clear out old records.
 * @author emrek
 *
 */
public class AgeThreshold implements Plugin, RecordCollectionListener {

    /** the name of this eviction policy **/
    public static final String POLICY_NAME = "AgeThreshold";

    public static final String MAX_AGE_ARG = "maxAge";
    public static final String SWEEP_PERIOD_ARG = "sweepPeriod";

    PluginArg[] args = {
	new PluginArg( MAX_AGE_ARG,
		       "argument name for the age threshold. we'll remove records which reach  this age",
		       PluginArg.ARG_INTEGER,
		       true,
		       "10" ),
	new PluginArg( SWEEP_PERIOD_ARG,
		       "the periodicity of our sweeps.  This plugin will iterate over record collections every period. specified in milliseconds",
		       PluginArg.ARG_INTEGER,
		       true,
		       "1000" ),
	new PluginArg( EvictionPolicyConstants.IS_DEFAULT_EVICTION_POLICY_ARG,
		       "is this the default eviction policy",
		       PluginArg.ARG_BOOLEAN,
		       true,
		       "true" )
    };


    private int maxAge;
    private long sweepPeriod;
    private boolean isDefaultEvictionPolicy;

    private AnalysisEngine engine;

    private Timer timer;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine)
        throws PluginException {
        this.engine = engine;

	maxAge = ((Integer)args.get(MAX_AGE_ARG)).intValue();
	sweepPeriod = ((Integer)args.get(SWEEP_PERIOD_ARG )).intValue();
	isDefaultEvictionPolicy = ((Boolean)args.get( EvictionPolicyConstants.IS_DEFAULT_EVICTION_POLICY_ARG )).booleanValue();
	
        timer = new Timer();
        timer.schedule(new MySweeper(), 0, sweepPeriod); // fixed-delay
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        timer.cancel();
    }

    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(
     * String, List)
     */
    public void addedRecords(String collectionName, List items) {

        // reset all the records' ages
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            Record r = (Record) iter.next();
            r.setAttribute("age", new Integer(0), true);
        }
    }

    /**
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(
     *  String,  List)
     */
    public void removedRecords(String collectionName, List items) {
        // ignore
    }

    class MySweeper extends TimerTask {

        void sweepRecordCollection(RecordCollection recordCollection) {

            int currMaxAge = maxAge;

            // see if this recordCollection overrides the global default.
            if (recordCollection.getAttribute(MAX_AGE_ARG) != null) {
                currMaxAge =
                    Integer.parseInt(
                        (String) recordCollection.getAttribute(MAX_AGE_ARG));
            }

            Map records = recordCollection.getAllRecords();
            Set keysToRemove = new HashSet();
            Iterator iter = records.keySet().iterator();
            while (iter.hasNext()) {
                String k = (String) iter.next();
                Record r = recordCollection.getRecord(k);
                Integer age = (Integer) r.getAttribute("age");
                int a = 0;
                if (age != null) {
                    a = age.intValue();
                }
                a++;
                if (a > currMaxAge) {
                    keysToRemove.add(k);
                }
                r.setAttribute("age", new Integer(a), true);
            }

            iter = keysToRemove.iterator();
            while (iter.hasNext()) {
                recordCollection.removeRecord((String) iter.next());
            }

        }

        public void run() {

            Map collections = engine.getAllRecordCollections();

            Iterator iter = collections.values().iterator();

            while (iter.hasNext()) {
                RecordCollection rc = (RecordCollection) iter.next();
                String sEvictionPolicy =
                    (String) rc.getAttribute(
                        EvictionPolicyConstants.EVICTION_POLICY_ARG);
                if ((sEvictionPolicy == null && isDefaultEvictionPolicy)
                    || (sEvictionPolicy.equals(POLICY_NAME))) {
                    sweepRecordCollection(rc);
                }
            }

        }
    }

}
