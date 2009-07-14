/*
 * $Id: UDP.java,v 1.1 2004/08/27 04:20:38 candea Exp $
 */

package swig.util;

import java.io.*; 
import java.net.*; 

/**
 * A helper class for sending/receiving objects over UDP sockets.
 * Designed for the simplest possible interface; do not use if
 * performance is a concern.
 *
 * @version <tt>$Revision: 1.1 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 */

public class UDP
{ 
    // The maximum object size (in bytes) we can receive
    public static final int MAX_OBJECT_SIZE=10000;

    /**
     * Send an object using a UDP datagram (caller needs to deal with
     * fragmentation, if object is bigger than UDP MTU).
     *
     * @param host  Destination hostname
     * @param port  Destination port
     * @param obj   The object to send
     **/
    public static void send( String host, int port, Object obj )
	throws IOException
    {
	DatagramSocket socket = new DatagramSocket();

	try {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream( baos );
	    oos.writeObject( obj );
	    oos.flush();
	    byte[] bytes = baos.toByteArray();
	    
	    InetSocketAddress sockAddr = new InetSocketAddress( host, port );
	    DatagramPacket packet = new DatagramPacket( bytes, bytes.length, sockAddr );
	    
	    socket.send( packet );
	}
	catch( IOException e ) { 
	    throw e; 
	}
	finally	{
	    socket.close(); 
	}
    }

    /**
     * Receive an object from a UDP datagram; this call may block for
     * a long time.
     *
     * @param port  Local port to listen on
     **/
    public static Object receive( int port )
	throws IOException, ClassNotFoundException
    {
	DatagramSocket socket = new DatagramSocket( port );

	try {
	    byte[] buf = new byte[ MAX_OBJECT_SIZE ];
	    DatagramPacket pkt = new DatagramPacket( buf, buf.length );

	    socket.receive( pkt );

	    ObjectInputStream ois = new ObjectInputStream
		( new ByteArrayInputStream( pkt.getData(), pkt.getOffset(), pkt.getLength() ));

	    return ois.readObject();
	}
	catch( IOException e ) { 
	    throw e; 
	}
	catch( ClassNotFoundException e ) {
	    throw e;
	}
	finally {
	    socket.close();
	}
    }
} 
