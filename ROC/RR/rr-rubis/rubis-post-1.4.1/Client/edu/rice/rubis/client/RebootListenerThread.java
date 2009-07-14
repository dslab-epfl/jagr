/* $Id: RebootListenerThread.java,v 1.4 2004/05/27 01:09:01 fjk Exp $ */
package edu.rice.rubis.client;

import java.io.*;
import java.net.*;
import java.util.*;

public class RebootListenerThread extends Thread {

    private PrintStream report;
    
    static final private int PORT = 2375; // UDP port on which notifications arive
                                                                                               
    private DatagramSocket socket = null; // socket for rcv-ing UDP datagrams
    private boolean        die     = true; // true if we need to shut down
    
    /**
     * Constructor.
     * 
     * @param report printstream. practically output index.html.
     */
    public RebootListenerThread( PrintStream report )
        throws Exception
    {
	//set report
	this.report = report;

	//initialize socket
	String addr = InetAddress.getLocalHost().toString();
	try {
	    this.socket = new DatagramSocket(PORT);
	    report.println("ClientEmluator : Reboot Listener thread listening on " + addr + ":" + PORT + " (UDP) <BR>" );
	} catch (SocketException e){
	    report.println( "ClientEmulator : Cannot listen on " + addr + ":" + PORT + "   " + e );
	    throw e;
	}
	
	//thread is alive.
	die = false;
    }

    /*
     * main method for this thread.
     * infinite loop of getting FailureReport from socket and add to this.queue.
     */
    public void run(){
                                                                                               
        while ( !die ) {
            try 
 	    {
		//receive packet from socket
		byte[] buf = new byte[64];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		String contents = (new String(buf)).trim();
		
		if(contents.length() == 0){
		    //stop notice might have come.
		    throw new InterruptedException();
		}

		System.out.println("received string is " + contents );

		InetAddress host = packet.getAddress();
		//set UserSession.instanceDown
		setInstanceDown(contents, host);

            }
            catch( InterruptedException intE ) { continue; }
            catch( Exception e )
	    {
		    report.println( e );
		    continue;
	    }
        }
 
        report.println("ClientEmulator : RebootListenerThread has stopped");
    }
 
    /*
     * stop this ReceiverThread. set true for die, and send dummy packet to the
     * socket, because you don't do this, thread keeps waiting listeing to the
     * socket. Finally close the socket.
     */
    public void waitFor() throws Exception {
        die = true;
 
        //send dummy message to DatagramSocket so that the thread stops
        //waiting listning on socket.
        try{
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(new byte[0], 0,
                                                       InetAddress.getLocalHost(),
                                                       PORT);
            socket.send(packet);
        } catch (Exception e){
            throw new Exception("failed to send wakeup message to port; can't stop");
        }
        this.socket.close();
    }

    /**
     * set UserSession.instanceDown properly.
     * If contents suggested starting of the reboot
     * and the host was i'th in UserSession.hostList,
     * then UserSession.instanceDown = 2^i.
     * 
     * If the contents suggested end of the reboot
     * then set UserSession.instanceDown = -1.
     *
     * @param contents contents of received packet.
     * @param host host that sent the packet
     */
    private void setInstanceDown(String contents, InetAddress host) {
	Iterator it = UserSession.hostList.iterator();
	for ( int i=0; it.hasNext(); i++ ) 
        {
	    if( it.next().equals(host) ) 
	    {
		int n = 1 << i;

		if ( contents.equals(new String("starting reboot")) ) 
		{
		    UserSession.instanceDown += n;
		}
		if (contents.equals(new String("reboot finished")) ) 
		{
		    if ( ( UserSession.instanceDown & n ) != 0 )
			UserSession.instanceDown -= n;
		}
		System.out.println("UserSession.instanceDown = " + UserSession.instanceDown);
		break;
	    }
	}
    }
}

                                                                                               
