package roc.jagr.recomgr.worker;

import java.io.*;
import java.net.*;
import roc.jagr.recomgr.*;
import roc.jagr.recomgr.event.*;

import org.apache.log4j.Logger;

/**
 *  this class listens for failure reports
 *
 */
public class ReportWorker implements Worker {

    static Logger log = Logger.getLogger( "ReportWorker" );

    static final int PORT = 2999;

    RecoveryManager mgr;

    public void setRecoveryManager( RecoveryManager mgr ) {
	this.mgr = mgr;
    }

    public void run() {

	DatagramSocket socket = null;

	try {
	    socket = new DatagramSocket( PORT );
	    log.info( "listening on port " + PORT );
	    
	    byte[] buf = new byte[ 6000 ];
	    while( true ) {
		DatagramPacket packet = new DatagramPacket( buf, buf.length );
		socket.receive( packet );
		String contents = new String( buf );
		FailureReport report = 
		    FailureReport.fromByteArray( packet.getData(),
						 packet.getOffset(),
						 packet.getLength() );
		
		mgr.receiveEvent( new FailureReportEvent( report ));
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
