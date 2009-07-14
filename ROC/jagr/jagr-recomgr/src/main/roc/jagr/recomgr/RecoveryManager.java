/**
 * $Id: RecoveryManager.java,v 1.4 2004/07/24 03:00:49 candea Exp $
 **/

package roc.jagr.recomgr;

import java.util.*;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

public class RecoveryManager {

    static Logger log = Logger.getLogger( "RecoMgr" );

    // todo: read this from some cmd-line arguments or configfile
    boolean debug = true; 
    Map args;

    /*** CONFIG STUFF ***/

    // todo: read this from some cmd-line arguments or configfile
    Class[] MyWorkerClasses = {
	roc.jagr.recomgr.worker.ReportWorker.class
    };

    // todo: read this from some cmd-line arguments or configfile
    Class MyPolicyClass =
	roc.jagr.recomgr.policy.SimpleMicrorebootPolicy.class;

    String targetHostname = "localhost";

    /*** REAL CODE ***/

    RecoveryPolicy policy;
    
    public void start() 
	throws InstantiationException,IllegalAccessException {

	args = new HashMap();

	loadAllWorkers( MyWorkerClasses );
	loadPolicy( MyPolicyClass );
    }

    /**
     * This is where events (such as failure reports) come in.
     * The recovery manager will simply forward them to the current
     * policy.
     */
    public void receiveEvent( Event e ) {
	// TODO: right now, events are received in a multi-threaded fashion.
	// we probably want to add them to a queue, and let a single-thread
	// handle them in the policy to avoid complications

	try {
	    RecoveryAction action = policy.processEvent( e );
	    if( action != null ) {
		action.doAction();
	    }
	} catch( Exception exc ) {
	    exc.printStackTrace();
	    System.exit(-1);  // do something more clever than fail-stop...
	}
    }

    public void stop() {
	// ?? todo
    }

    public void setArg( String k, String v ) {
	args.put( k, v );
    }

    public String getArg( String k ) {
	return (String)args.get( k );
    }

    public String getTargetHostname() {
	return targetHostname;
    }

    public void loadPolicy( Class policyClass ) 
	throws InstantiationException, IllegalAccessException {
	log.info( "Loading policy: " + policyClass.toString() );
	policy = (RecoveryPolicy)policyClass.newInstance();
	policy.setManager( this );
    }

    public void loadAllWorkers( Class[] workerClasses ) 
	throws InstantiationException, IllegalAccessException {
	for( int i=0; i<workerClasses.length; i++ ) {
	    loadWorker( workerClasses[i] );
	}
    }

    public void loadWorker( Class workerClass ) 
	throws InstantiationException, IllegalAccessException {

	log.info( "Loading worker: " + workerClass.toString() );

	Worker worker = (Worker)workerClass.newInstance();
	worker.setRecoveryManager( this );
	Thread t = new Thread( worker );
	t.start();
    }


    public synchronized void logDebug( Object src, String s) {
	if( debug )
	    System.err.println( "[INFO] (" +
				(src==null?"null":src.getClass().getName()) +
				") " + s);
    }

    public synchronized void logInfo( Object src, String s) {
        // do something nicer here...
        System.err.println( "[INFO] (" + 
			    (src==null?"null":src.getClass().getName()) +
			    ") " + s);
    }

    public synchronized void logWarning(Object src, String s) {
        // do something nicer here...
        System.err.println( "[WARN] (" +
			    (src==null?"null":src.getClass().getName()) +
			    ") " + s);
    }

    public synchronized void logError(Object src, String s) {
        // do something nicer here...
        System.err.println( "[ERROR] (" + 
			    (src==null?"null":src.getClass().getName()) +
			    ") " + s);
	if( s == null || "null".equals(s)) {
	    (new Exception()).printStackTrace();
	}
    }
    
    public synchronized void logError(Object src, String s, Throwable t ) {
        // do something nicer here...
	System.err.println( "[ERROR] (" + 
			    (src==null?"null":src.getClass().getName()) +
			    ") " + s);
        t.printStackTrace();
    }
    

    public synchronized void logStats(Object src, String s) {
        // do something nicer here...
	System.err.println( "[STATS] (" + 
			    (src==null?"null":src.getClass().getName()) +
			    ") " + s);
    }



    public static void main( String[] argv ) {
	
	try {
	    String roctop = System.getProperty( "ROC_TOP" ); // temporary hack
	    PropertyConfigurator.configure( roctop + "/jagr/jagr-recomgr/conf/log4j.cfg" );

	    RecoveryManager mgr = new RecoveryManager();
	    mgr.start();
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
    }

}
