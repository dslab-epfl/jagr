package roc.pinpoint.analysis.plugins;

import java.util.Map;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.tracing.Observation;
import roc.pinpoint.tracing.ObservationSubscriber;

/**
 * This plugin collects observations from an outside source.  It will
 * dynamically load a class which implements "roc.pinpoint.tracing.
 * ObservationSubscriber," and use this to receive observations.
 * @author emrek
 *
 */
public class ObservationCollector implements Plugin, Runnable {

    public static final String SUBSCRIBER_CLASS_ARG = "subscriberClassName";
    public static final String COLLECTION_NAME_ARG = "collectionName";

    PluginArg[] args = {
	new PluginArg( SUBSCRIBER_CLASS_ARG,
		       "the class implementing 'roc.pinpoint.tracing.ObservationSubscriber'",
		       PluginArg.ARG_STRING,
		       true,
		       null ),
	new PluginArg( COLLECTION_NAME_ARG,
		       "the output collection name.  this plugin will place the observations it receives into this record collection",
		       PluginArg.ARG_STRING,
		       true,
		       "observations" )
    };


    private ObservationSubscriber subscriber;

    private Thread worker = null;
    private boolean stopCollecting = false;

    private AnalysisEngine engine;
    private String collectionName;

    /**
     * loads a class implementing ObservationSubscriber ...
     * @param subscriberClassname the name of the class to load
     */
    protected void loadSubscriber(String subscriberClassname) {

        try {
            Class sInterface =
                Class.forName("roc.pinpoint.tracing.ObservationSubscriber");

            Class sClass = Class.forName(subscriberClassname);

            if (!(sInterface.isAssignableFrom(sClass))) {
                System.err.println(subscriberClassname + " not a subscriber");
            }
            else {
                subscriber = (ObservationSubscriber) sClass.newInstance();
                System.err.println(
                    "ObservationCollector: loadSubscriber(...) 5 ");
            }
        }
        catch (ClassNotFoundException cnfe) {
            System.err.println("Class " + subscriberClassname + " not found");
        }
        catch (IllegalAccessException iae) {
            System.err.println("Illegal Access: " + subscriberClassname);
        }
        catch (InstantiationException ie) {
            System.err.println(
                "Could not instantiate subscriber " + subscriberClassname);
        }
    }

    public PluginArg[] getPluginArguments() {
	return args;
    }
    
/**
 * 
 * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
 */
    public void start(String id, Map args, AnalysisEngine engine) 
        throws PluginException {


	String subscriberClassName = (String)args.get( SUBSCRIBER_CLASS_ARG );
	collectionName = (String)args.get(COLLECTION_NAME_ARG );

        this.engine = engine;

        loadSubscriber(subscriberClassName);

        stopCollecting = false;
        // assert worker = null
        worker = new Thread(this);
        worker.start();
    }

    /**
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        stopCollecting = true;
        worker.interrupt();
    }

    /**
     * Loops, collecting observations from the ObservationSubscriber.
     * @see java.lang.Runnable#run()
     */
    public void run() {

        while (!stopCollecting) {
            try {
                Observation obs = subscriber.receive();
                obs.collectedTimestamp = System.currentTimeMillis();

                engine.getRecordCollection(collectionName).setRecord(
                    obs.requestId
                        + obs.sequenceNum
                        + "_"
                        + obs.collectedTimestamp,
                    new Record(obs));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        worker = null;
    }

}
