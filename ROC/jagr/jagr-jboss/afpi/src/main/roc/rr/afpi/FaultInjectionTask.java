/*
 * $Id: FaultInjectionTask.java,v 1.3 2004/07/29 01:58:47 candea Exp $ 
 */

package roc.rr.afpi;

import java.util.*;
import java.io.*;
import org.jboss.logging.Logger;
import javax.management.*;
import roc.rr.EJBInterceptor;

public class FaultInjectionTask
    extends TimerTask 
{
    Logger                log     = null; // Logger
    FaultInjectionService service = null; // FaultInjectionService to use for injections
    FaultInjection        fault   = null; // The task needs to inject this fault upon activation

    /**
     * constructor
     *
     * @param fault    fault to inject
     * @param service  FaultInjectionService to use for injections
     * @param log      Logger
     */
    public FaultInjectionTask( FaultInjection fault, FaultInjectionService service, Logger log )
    {
	this.fault   = fault;
	this.log     = log;
	this.service = service;
    }
	
    /**
     *
     * Method to run when task is scheduled.
     */
    public void run() 
    {
	Object faultType = fault.getFaultType();
	String compName   = fault.getCompName();

	try {
	    String ret = null;
	    if ( faultType == EJBInterceptor.INJECT_THROWABLE ) 
	    {
		ret = service.scheduleThrowable( compName, "java.lang.Exception" );
	    }
	    else if ( faultType == EJBInterceptor.INJECT_MEMLEAK )
	    {
		ret = service.scheduleMemoryLeakByArg( compName, fault.getAmount() );
	    }
	    else if ( faultType == EJBInterceptor.MICROREBOOT )
	    {
		ret = service.microrebootNow( compName );
	    }
	    else if ( faultType == FaultInjection.FULL_REBOOT )
	    {
		ret = service.rebootApplication( compName );
	    }
	    else if ( faultType == FaultInjection.UNBIND_NAME )
	    {
		ret = service.unbindJndiName( compName );
	    }
	    else if ( faultType == EJBInterceptor.SET_NULL_TXINT )
	    {
		ret = service.setNullInTxInterceptorCMT( compName );
	    }
	    else if ( faultType == EJBInterceptor.DEADLOCK )
	    {
		ret = service.scheduleDeadlockByArg( compName );
	    }
	    else if ( faultType == EJBInterceptor.INFINITE_LOOP )
	    {
		ret = service.scheduleInfiniteLoopByArg( compName, fault.getAmount() );
	    }
	    else if ( faultType == EJBInterceptor.NO_ACTION ) 
	    {
		ret = service.cancelScheduledInjection( compName );
	        ret += "\n" + service.cancelMemoryLeakByArg( compName );
	    }
	    else if ( faultType == FaultInjection.END_OF_CAMPAIGN )
	    {
		ret = service.stopFaultInjectionCampaign();
	    }
	    else 
	    {
		ret = "ERROR: Unknown fault, cannot inject";
	    }
	    log.info( ret );
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
    }
}
	
