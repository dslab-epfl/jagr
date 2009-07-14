package roc.recomgr.worker;

import java.io.*;
import java.net.*;
import roc.recomgr.*;
import roc.recomgr.event.*;
import swig.util.UDP;

import org.apache.log4j.Logger;

/**
 *  this class listens for failure reports
 *
 */
public class ReportWorker implements Worker 
{
    // Log output
    static Logger log = Logger.getLogger( "ReportWorker" );

    static final int PORT = 2999;

    RecoveryManager mgr;

    public void setRecoveryManager( RecoveryManager mgr ) {
	this.mgr = mgr;
    }

    public void run() {

	DatagramSocket socket = null;

	try {
	    while( true ) {
	       Object event = UDP.receive(PORT);    
	       System.out.println("received: "+event);
	       if ( event.getClass() == FailureReport.class ) 
	       {
		   log.debug( "Received failure event " + event );
		   // Failure report case
		   mgr.receiveEvent( new FailureReportEvent( (FailureReport)event ));
	       } 
	       else if ( event.getClass() == FaultInjectionReport.class ) 
               {
		   // Fault injection report case
		   log.debug( "Received injection event" + event );
		   mgr.setFaultInjectionReport((FaultInjectionReport)event);
	       }
	    }
	}
	catch( Exception e ) {
	    log.error( "generic exception", e );
	}
	finally {
	    try {
		socket.close();
	    }
	    catch( Exception ignore ) { }
	}

    }

    
}
