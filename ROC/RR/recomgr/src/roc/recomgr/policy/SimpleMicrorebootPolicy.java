/**
 * $Id: SimpleMicrorebootPolicy.java,v 1.6 2004/09/20 04:04:00 candea Exp $
 **/

package roc.recomgr.policy;

import java.util.*;

import roc.recomgr.*;
import roc.recomgr.event.*;
import roc.recomgr.action.*;

import org.apache.log4j.Logger;

public class SimpleMicrorebootPolicy implements RecoveryPolicy 
{
    // Log output
    private static Logger log = Logger.getLogger( "SimpleMicrorebootPolicy" );

    // Reference to our recovery manager
    private RecoveryManager mgr;

    // The time we last initiated a microreboot (in milliseconds) -- assume only one target EJB is being rebooted
    private static long lastRebootStarted=0;

    // The component we microrebooted last time
    private static String rebootedComp=null;


    public void setManager( RecoveryManager mgr )  { this.mgr = mgr; }


    public RecoveryAction processEvent( Event event ) 
	throws Exception
    {
	if( ! (event instanceof FailureReportEvent) ) 
	{
	    log.info( "Received unknown event " + event );
	    return null;
	}

	FailureReport fr = ((FailureReportEvent) event).getReport();
	
	// A single uRB target, extracted from the injection report
	FaultInjectionReport report = mgr.getFaultInjectionReport();
	if ( report == null ) 
	{
	    log.info( "Injection report is null" );
	    return null;
	}

	long now = System.currentTimeMillis();
	String compName = report.getTarget();

	if( lastRebootStarted>0  &&  now-lastRebootStarted < RecoveryManager.EJB_REBOOT_WAIT  &&  compName.equals( rebootedComp ) )
	{
	    log.debug( "Microreboot initiated on " + compName + " less than " + 
		       RecoveryManager.EJB_REBOOT_WAIT/1000 + " sec ago; will not reboot it at this time." );
	    return null;
	}
	
	rebootedComp = compName;
	lastRebootStarted = now;

	Set rebootTargets = new HashSet();
	rebootTargets.add( compName );

	return new MicrorebootAction( mgr, rebootTargets );
    }
}
