/* 
 * $Id: RecoveryManagerThread.java,v 1.1 2004/06/08 13:33:04 emrek Exp $ 
 *
 */
package roc.rr.afpi;

import java.util.*;
import java.sql.*;
import java.net.*;
import java.io.*;
import javax.management.ObjectName;
import org.jboss.deployment.*;
import org.jboss.logging.Logger;

import roc.rr.afpi.util.*;

public class RecoveryManagerThread 
    extends Thread 
{
    private static Logger log = Logger.getLogger( RecoveryManagerThread.class );

    private boolean               die = false;        // false when thread is running
    private FailureReceiverThread receiver = null;    // FailureReceiver thread
    private Queue                 reportQueue = null; // queue of FailureReports
    private FailureHandler        handler = null;     // FailureHandler 

    /**
     * Constructor.  'threshold' pertains to session EJBs when in
     * microreboot mode, or to entire application in full reboot
     * mode.
     * 
     * @param log          Logger
     * @param threshold    threshold value for triggering recovery
     * @param isFullReboot true is reco mgr should run in full reboot mode
     *
     */ 
    public RecoveryManagerThread( RecoveryService recoSvc, int threshold, boolean isFullReboot ) 
	throws Exception
    {
	this.reportQueue = new Queue();
	this.receiver = new FailureReceiverThread( this.reportQueue );
	this.receiver.start();
	
	if( !isFullReboot )
	{
	    this.handler = new FailureHandler( true, threshold, recoSvc); // uRB version
	    log.info( "Recovery manager started (mode=MICRO, threshold=" + threshold + ")" );
	}
	else
	{
	    this.handler = new FailureHandler( false, threshold, recoSvc); // full RB version
	    log.info( "Recovery manager started (mode=FULL, threshold=" + threshold + ")" );
	}
    }

    /**
     * Run method consists of a listening loop that retrieves failure
     * reports from 'reportQueue' and passes them to the handler.
     *
     */
    public void run() 
    {
	while ( !die ) 
	{
	    FailureReport report = (FailureReport) reportQueue.dequeue();
	    log.info("Dequeued " + report);
	    try {
		handler.process(report);
	    } catch ( Exception e ) {
		Information.stackToLog( e, log );
		break;
	    }
	}

	this.die();
	log.info("Recovery manager thread stopped.");
    }

    /**
     * Stop the recovery manager thread.
     *
     */
    public void die()
    {
	try { 
	    receiver.die();
	}
	catch( Exception e ) {
	    log.info( "Couldn't stop failure receiver thread: " + e );
	}
        die = true;
    }

    public Queue getQueue()  { return this.reportQueue; }
}
