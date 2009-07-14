/**
 * $Id: FailoverAndRebootPolicy.java,v 1.3 2004/09/14 03:09:53 candea Exp $
 **/

package roc.recomgr.policy;

import roc.recomgr.*;
import roc.recomgr.event.*;
import roc.recomgr.action.*;

import org.apache.log4j.Logger;

/**
 * Policy that fails over at the first sign of failure and reboots the
 * offending application server.
 *
 * @version <tt>$Revision: 1.3 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 */

public class FailoverAndRebootPolicy implements RecoveryPolicy 
{
    // Log output
    private static Logger log = Logger.getLogger( "FailoverAndRebootPolicy" );

    RecoveryManager mgr;

    // The time we last initiated a reboot (in milliseconds) -- assume only one target server is being rebooted
    private static long lastRebootStarted=0;

    // The server we rebooted last time
    private static String rebootedServer=null;

    public void setManager( RecoveryManager mgr ) 
    {
	this.mgr = mgr;
    }

    public RecoveryAction processEvent( Event event ) 
    {
	assert event.getType().equals( FailureReportEvent.TYPE );
	FailureReport report = ((FailureReportEvent) event).getReport();

	String srv = report.getServer();
	long now = System.currentTimeMillis();

	if( lastRebootStarted>0  &&  now-lastRebootStarted < RecoveryManager.JBOSS_REBOOT_WAIT  &&  srv.equals( rebootedServer) )
	{
	    log.debug( "Reboot initiated on " + srv + " less than " +
		       RecoveryManager.JBOSS_REBOOT_WAIT/1000 + " sec ago; will not reboot it at this time." );
	    return null;
	}
	
	rebootedServer = srv;
	lastRebootStarted = now;
	return new RebootServerAction( mgr, report );
    }

}
