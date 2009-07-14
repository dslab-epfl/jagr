//
// $Id: FailureReporter.java,v 1.1 2004/06/08 13:35:04 emrek Exp $
//

package edu.rice.rubis.client;

import java.net.*;
import java.io.*;
import java.util.*;

public class FailureReporter
{
    static private InetAddress recoMgrAddr;        // the recovery manager's IP address
    static private final int   recoMgrPort = 2374; // the recovery manager's UDP port
    private static DatagramSocket  socket  = null;

    public FailureReporter( RUBiSProperties props )
    {
	try {
	    if ( socket == null )
		socket = new DatagramSocket();
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
    }

    public static void send( URL url, String level )
    {
	try
	{  
	    String            name     = getName( url );
	    String report = "servlet;" + name;
	    byte[] reportbytes = report.getBytes();
	    recoMgrAddr                = InetAddress.getByName( url.getHost() );
	    InetSocketAddress sockAddr = new InetSocketAddress( recoMgrAddr, recoMgrPort );
	    DatagramPacket    packet   = new DatagramPacket( reportbytes, reportbytes.length, sockAddr );

	    socket.send(packet);

	    String    now = (new Date()).toString();
	    String[] strs = now.split(" ");
	    System.out.println(strs[3] + " Sent " + level + "-level failure report for " + recoMgrAddr + " : " + name );
	}
	catch (SocketException sockE)
	{
	    System.err.println("Failed to bind to UDP port");
	    sockE.printStackTrace();
	}
	catch (IOException ioE)
	{
	    System.err.println("Error reporting failure");
	    ioE.printStackTrace();
	}
    }

    private static String getName( URL url )
    {
	String urlString = url.getFile();
	String tail = urlString.substring( urlString.lastIndexOf("/") + 1 );

	final String servletPrefix = "edu.rice.rubis.beans.servlets"; 
	if ( tail.startsWith( servletPrefix  ) )
	{
	    int end = tail.indexOf('?');
	    if ( end < 0 )
		end = tail.length();

	    String servletName = tail.substring( 1+servletPrefix.length(), end );
	    return servletName;

	} else {
	    return tail;
	}
    }
}


