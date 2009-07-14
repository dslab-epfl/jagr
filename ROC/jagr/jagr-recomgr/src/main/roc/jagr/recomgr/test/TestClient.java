package roc.jagr.recomgr.test;

import java.util.*;
import java.io.IOException;
import java.net.*;
import roc.jagr.recomgr.FailureReport;


public class TestClient {

    static String recoveryMgrHost;
    static int recoveryMgrPort;

    public static void main( String[] args ) {
	recoveryMgrHost = "localhost";
	recoveryMgrPort = 2999;
	sendFailureReport( args );
    }

    
    private static void sendFailureReport( String[] components ) {
	
	FailureReport report = new FailureReport();

	for( int i=0; i<components.length; i++ ) {
	    report.addSuspect( new FailureReport.Suspect( components[i], 1.0 ));
	}

	sendFailureReport( report );
    }

    private static void sendFailureReport( FailureReport report ) {
	try {
	    DatagramSocket socket = new DatagramSocket();

	    byte[] reportbytes = report.toByteArray();
	    InetSocketAddress sockAddr = new InetSocketAddress( recoveryMgrHost, recoveryMgrPort );
	    DatagramPacket packet = new DatagramPacket( reportbytes, reportbytes.length, sockAddr );

	    System.err.println( "[INFO] JagrConnector: Sending Failure Report: " +
				report );
	    socket.send( packet );
	
	}
	catch( SocketException e ) {
	    System.err.println( "[ERROR] JagrConnector: Failed to bind to UDP port" );
	    e.printStackTrace();
	}
	catch( IOException e ) {
	    System.err.println( "[ERROR] JagrConnector: IO Error reporting failure" );
	    e.printStackTrace();
	}
    }
    

}
