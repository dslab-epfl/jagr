/**
 * $Id: FailureReceiverThread.java,v 1.1 2004/06/08 13:33:04 emrek Exp $
 *
 */

package roc.rr.afpi;

import java.io.*;
import java.net.*;
import java.util.*;
import org.jboss.logging.Logger;
import roc.rr.afpi.util.*;

public class FailureReceiverThread extends Thread 
{
    static private Logger log = Logger.getLogger( FailureReceiverThread.class );

    static final private int PORT = 2374; // UDP port on which notifications arive

    private DatagramSocket _socket = null; // socket for rcv-ing UDP datagrams
    private Queue          queue   = null; // FailureReport queue
    private boolean        die     = true; // true if we need to shut down

    /**
     * Constructor.
     *
     */
    public FailureReceiverThread( Queue queue )
	throws Exception
    {
	if ( this.queue != null ) 
	    throw new Exception("Failure receiver thread already running");

	this.queue = queue;

	String addr = InetAddress.getLocalHost().toString();
	try {
            _socket = new DatagramSocket(PORT);
            log.info("Failure receiver thread listening on " + addr + ":" + PORT + " (UDP)" );
        } catch (SocketException e){
            log.error( "Cannot listen on " + addr + ":" + PORT + "   " + e );
            throw e;
        }

	die = false;
    }

    /*
     * main method for this thread.
     * infinite loop of getting FailureReport from socket and add to this.queue.
     */
    public void run(){

	while ( !die ) {
	    FailureReport report = null;
	    try {
		report = getFailureReport();
	    } 
	    catch( InterruptedException intE ) { continue; } 
	    catch( Exception e )
	    {
                log.info( e );
                continue;
            }

	    if ( report != null )
		queue.enqueue( (Object)report );
        }

        log.info("Stopped");
    }

    /*
     * stop this ReceiverThread. set true for die, and send dummy packet to the
     * socket, because you don't do this, thread keeps waiting listeing to the 
     * socket. Finally close the socket.
     */
    public void die() throws Exception {
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
	_socket.close();
    }

    /*
     * get Failure report from _socket.
     *
     * @return returns FailureReport obtained from _socket.
     */
    private FailureReport getFailureReport()
        throws IOException, SocketException,
               ClassNotFoundException, InterruptedException {

        //receive packet from socket
        byte[] buf = new byte[64];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        _socket.receive(packet);
        String contents = new String(buf);
 
        if(contents.trim().length() == 0){
            //stop notice might have come.
            throw new InterruptedException();
        }
         
        //change string into FailureReprot format
        FailureReport report = new FailureReport();
        report.read(contents);
	report.addTimeStamp(System.currentTimeMillis());
        return report;
    }
}
    
