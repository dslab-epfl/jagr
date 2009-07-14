/**
 * $Id: RecoveryManager.java,v 1.13 2004/09/21 22:47:58 candea Exp $
 **/

package roc.recomgr;

import java.util.*;

import roc.recomgr.event.*;
import roc.loadgen.interceptors.loadbalancer.*;
import swig.util.*;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

public class RecoveryManager 
{
    // # of millisec to wait prior to repeating a...
    public static final int JBOSS_REBOOT_WAIT = 220000;  // JBoss node reboot
    public static final int EJB_REBOOT_WAIT = 60000;     // EJB microreboot

    // Array containing the hostnames of all load balancers we know about
    private static String[] loadbalancers=null;

    // Log output
    static Logger log = Logger.getLogger( "RecoveryManager" );

    private FaultInjectionReport fireport = null;

    // Java class that implements the worker
    Class workerClass;

    // Java class that implements the policy
    Class policyClass;

    static final boolean failoverEnabled = true;

    public static final int    LOADBAL_PORT = 3688;

    /*** REAL CODE ***/

    RecoveryPolicy policy;
    
    public void start() 
	throws InstantiationException,IllegalAccessException {

	loadWorker( workerClass );
	loadPolicy( policyClass );
	
	log.debug( "Recovery Manager started" );
    }

    /**
     *  Set fault injection report 
     *
     */
    public void setFaultInjectionReport(FaultInjectionReport report) 
    {
	log.debug( "Current fault injection report is " + report );
	this.fireport = report;
    }

    /**
     *  Get fault injection report
     *
     */
    public FaultInjectionReport getFaultInjectionReport() {
	return this.fireport;
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
	    if( action != null ) 
	    {
		// If needed, notify load balancer (recovery starting)
		String affectedSrv = getTargetHostname();
		log.debug( "affectedSrv=" + affectedSrv );
		if( affectedSrv!=null  &&  failoverEnabled )
		{
		    HostUpdate update = new HostUpdate( affectedSrv, false );
		    log.debug( "Starting recovery..." );
		    for( int i=0 ; i < loadbalancers.length ; i++ )
		    {
			log.debug( "Sending " + update + " to " + loadbalancers[i] );
			UDP.send( loadbalancers[i], LOADBAL_PORT, (Object)update );
		    }
		}
		    
		// perform the recovery action
		log.debug( "Performing recovery action" );
		action.doAction();
		log.debug( "Recovery action completed" );

		// If needed, notify load balancer (recovery complete)
		if( affectedSrv!=null  &&  failoverEnabled )
		{
		    HostUpdate update = new HostUpdate( affectedSrv, true );
		    for( int i=0 ; i < loadbalancers.length ; i++ )
		    {
			log.debug( "Sending " + update + " to " + loadbalancers[i] );
			UDP.send( loadbalancers[i], LOADBAL_PORT, (Object)update );
		    }
		}

		// System.exit(0);
	    }
	} catch( Exception exc ) {
	    exc.printStackTrace();
	    System.exit(-1);  // do something more clever than fail-stop...
	}
    }

    public void stop() {
	// ?? todo
    }

    public String getTargetHostname() {
	if ( fireport != null )
	    return fireport.getHostName();
	else 
	    return null;
    }

    public void loadPolicy( Class policyClass ) 
	throws InstantiationException, IllegalAccessException {
	log.info( "Loading policy " + policyClass.toString() );
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

	log.info( "Loading worker " + workerClass.toString() );

	Worker worker = (Worker)workerClass.newInstance();
	worker.setRecoveryManager( this );
	Thread t = new Thread( worker );
	t.start();
    }


    public static void main( String[] argv ) 
	throws ClassNotFoundException
    {
	String log4jcfg = System.getProperty( "env.log4j" ); // temporary hack
	assert !log4jcfg.equals( "null" );
	PropertyConfigurator.configure( log4jcfg );

	try {
	    RecoveryManager mgr = new RecoveryManager();
	    mgr.processArguments( argv );
	    mgr.start();
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
    }


    /**
     * Extract our arguments from the command line.
     **/
    private void processArguments( String[] args )
	throws ClassNotFoundException
    {
	if( args.length != 3 )
	{
	    log.fatal( "Usage: RecoveryManager worker=<class>  policy=<class>  loadbal=<host1>,<host2>,..." );
	    System.exit( -1 );
	}

	for( int i=0 ; i < args.length ; i++ )
	{
	    String arg = args[i];
	    int x = arg.indexOf( "=" );

	    if( arg.startsWith( "loadbal=" ))
	    {
		String names = arg.substring( 1+x, arg.length() );
		loadbalancers = names.split( "," );

		String lbs="";
		for( int j=0 ; j < loadbalancers.length ; j++ )
		    lbs += loadbalancers[j] + " ";
		log.info( "Active load balancers: " + lbs );
	    }
	    else if( arg.startsWith( "worker=" ))
	    {
		workerClass = Class.forName( arg.substring( 1+x, arg.length() ));
	    }
	    else if( arg.startsWith( "policy=" ))
	    {
		policyClass = Class.forName( arg.substring( 1+x, arg.length() ));
	    }
	    else
	    {
		log.info( "Bad argument: " + arg );
		System.exit( -2 );
	    }
	}
    }


}
