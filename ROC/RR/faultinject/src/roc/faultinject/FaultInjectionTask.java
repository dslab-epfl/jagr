/*
 * $Id: FaultInjectionTask.java,v 1.5 2004/09/20 05:05:59 candea Exp $ 
 */

package roc.faultinject;

import java.util.*;
import java.io.*;
import javax.management.*;
import roc.rr.Action;
import org.jboss.console.remote.AppletRemoteMBeanInvoker;
import swig.util.UDP;
import roc.recomgr.FaultInjectionReport;

import org.apache.log4j.Logger;

public class FaultInjectionTask
    extends TimerTask 
{
    static Logger log = Logger.getLogger( "FaultInjectionTask" );

    private String         targetHost = null; // Fault Injection Target host name.
    private FaultInjection fault      = null; // The task needs to inject this fault upon activation
    private static String recomgrHost = "localhost"; // recovery manager host name
    private static int    recomgrPort = 2999;        // recovery manager port number


    /**
     * constructor
     *
     * @param fault    fault to inject
     * @param service  FaultInjectionService to use for injections
     * @param log      Logger
     */
    public FaultInjectionTask( FaultInjection fault, String hostName )
    { 
	this.fault      = fault;
	this.targetHost = hostName;
    }
	
    /**
     * Method to run when task is scheduled.
     */
    public void run() 
    {
	doInjection( targetHost, fault );
    }

    public static void doInjection( String targetHost, FaultInjection fault )
    {
	int    faultType    = fault.getFaultType();
	String compName     = fault.getCompName();
	String faultSubType = null;  // for jndi and data corruption
	AppletRemoteMBeanInvoker invoker;
	ObjectName service;

	try {
	    // invoker used in talking to JBoss
	    invoker = new AppletRemoteMBeanInvoker("http://"+targetHost 
					   +":8080/web-console/Invoker");
	    // name of the JBoss svc used for fault injection
	    service = new ObjectName("RR:service=FaultInjection"); 
	} catch( Exception ex ) {
	    ex.printStackTrace();
	    return;
	}

	try {
	    String ret = null;
	    if ( faultType == Action.INJECT_THROWABLE ) 
	    {
		Object[] params    = { (Object)compName,
				       (Object)"java.lang.Exception" };
		String[] signature = { "java.lang.String",
				       "java.lang.String" };
		ret = (String)invoker.invoke( service, "scheduleThrowable", 
					      params, signature);
	    }
	    else if ( faultType == Action.INJECT_MEMLEAK )
	    {
		Object params[]    = { (Object)compName,
				       (Object)fault.getAmount() };
		String signature[] = { "java.lang.String",
				       "java.lang.Integer" };
		ret = (String)invoker.invoke( service, 
					      "scheduleMemoryLeakByArg",
					      params, signature );
	    }
	    else if ( faultType == Action.SET_NULL_TXINT )
	    {
 		Object params[]    = { (Object)compName   };
		String signature[] = { "java.lang.String" };
		ret = (String)invoker.invoke( service, 
					      "setNullInTxInterceptorCMT",
					      params, signature );
	    }
	    else if ( faultType == Action.DEADLOCK )
	    {
 		Object params[]    = { (Object)compName   };
		String signature[] = { "java.lang.String" };
		ret = (String)invoker.invoke( service, 
					      "scheduleDeadlockByArg",
					      params, signature );
	    }
	    else if ( faultType == Action.INFINITE_LOOP )
	    {
		Object params[]    = { (Object)compName,
				       (Object)fault.getAmount() };
		String signature[] = { "java.lang.String",
				       "java.lang.Integer" };
		ret = (String)invoker.invoke( service, 
					      "scheduleInfiniteLoopByArg",
					      params, signature );
	    }
	    else if ( faultType == Action.CORRUPT_JNDI )
	    {
		faultSubType = fault.getCtype();
		Object params[]    = { (Object)compName,
				       (Object)faultSubType };
		String signature[] = { "java.lang.String",
				       "java.lang.String" };
		ret = (String)invoker.invoke( service, "corruptJndiName",
					      params, signature );
	    }
	    else if ( faultType == Action.CORRUPT_DATA )
	    {
		faultSubType = fault.getCtype();
		Object params[]    = { (Object)compName,
				       (Object)faultSubType,
				       (Object)fault.getCtime() };
		String signature[] = { "java.lang.String",
				       "java.lang.String",
		                       "java.lang.Integer" };
		ret = (String)invoker.invoke( service, 
					      "scheduleDataCorruption",
					      params, signature );
	    }
	    else if ( faultType == Action.END_OF_CAMPAIGN )
	    {
		System.exit(0);
	    }
	    else 
	    {
		ret = "ERROR: Unknown fault, cannot inject";
		log.error("* "+ret);
		return;
	    }
	    log.info(ret);

	    // send fault injection report to recover manager

	    // TODO: make this hack generic
	    int x = compName.indexOf( "Home" );
	    if( x > 0 )
		compName = compName.substring( 0, x );

	    FaultInjectionReport report 
		= new FaultInjectionReport(targetHost, compName, 
					   faultType, faultSubType);
	    UDP.send(recomgrHost, recomgrPort, report);
	    
	} catch( Exception e ) {
	    e.printStackTrace();
	}
    }
}
	
