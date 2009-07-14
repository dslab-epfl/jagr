/*
 * $Id: FaultInjectionTask.java,v 1.12 2004/08/26 21:00:33 skawamo Exp $ 
 */

package roc.rr.afpi;

import java.util.*;
import java.io.*;
import org.jboss.logging.Logger;
import javax.management.*;
import roc.rr.Action;

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
	int faultType = fault.getFaultType();
	String compName   = fault.getCompName();

	try {
	    String ret = null;
	    if ( faultType == Action.INJECT_THROWABLE ) 
	    {
		ret = service.scheduleThrowable( compName, "java.lang.Exception" );
	    }
	    else if ( faultType == Action.INJECT_MEMLEAK )
	    {
		ret = service.scheduleMemoryLeakByArg( compName, new Integer(fault.getAmount()) );
	    }
	    else if ( faultType == Action.MICROREBOOT )
	    {
		ret = service.microrebootNow( compName );
	    }
	    else if ( faultType == Action.FULL_REBOOT )
	    {
		ret = service.rebootApplication( compName );
	    }
	    else if ( faultType == Action.UNBIND_NAME )
	    {
		ret = service.unbindJndiName( compName );
	    }
	    else if ( faultType == Action.SET_NULL_TXINT )
	    {
		ret = service.setNullInTxInterceptorCMT( compName );
	    }
	    else if ( faultType == Action.DEADLOCK )
	    {
		ret = service.scheduleDeadlockByArg( compName );
	    }
	    else if ( faultType == Action.INFINITE_LOOP )
	    {
		ret = service.scheduleInfiniteLoopByArg( compName, new Integer(fault.getAmount()) );
	    }
	    else if ( faultType == Action.CORRUPT_JNDI )
	    {
		ret = service.corruptJndiName( compName, fault.getCtype() );
	    }
	    else if ( faultType == Action.CORRUPT_DATA )
	    {
		ret = service.scheduleDataCorruption( compName, fault.getCtype(), new Integer(fault.getCtime()) );
	    }
	    else if ( faultType == Action.NO_ACTION ) 
	    {
		ret = service.cancelScheduledInjection( compName );
	        ret += "\n" + service.cancelMemoryLeakByArg( compName );
	    }
	    else if ( faultType == Action.END_OF_CAMPAIGN )
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
	
