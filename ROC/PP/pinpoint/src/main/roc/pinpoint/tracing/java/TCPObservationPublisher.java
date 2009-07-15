/**
    Copyright (C) 2004 Emre Kiciman and Stanford University

    This file is part of Pinpoint

    Pinpoint is free software; you can distribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    Pinpoint is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Pinpoint; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**/
package roc.pinpoint.tracing.java;

// marked for release 1.0

import java.io.*;
import java.util.*;
import java.net.*;

import roc.pinpoint.tracing.Observation;
import roc.pinpoint.tracing.ObservationException;
import roc.pinpoint.tracing.ObservationPublisher;

/**
 *  A simple implementation of an Observation Publisher that uses
 *  Java serialization over TCP.
 *  <p>
 *  <b>Required</b>Set the java property "roc.pinpoint.publishto.hostname" to
 *  the "hostname:port" to contact.
 *  <p>
 *  Optionally, set the "roc.pinpoint.publishto.maxqueue" size to set
 *  the number of items that can be queued up waiting to be published over
 *  the network.
 *  
 */
public class TCPObservationPublisher implements ObservationPublisher {

    public static final String HOSTNAME_ARG = 
	"roc.pinpoint.publishto.hostname";
    public static final String MAX_QUEUE_ARG = 
	"roc.pinpoint.publishto.maxqueue";

    public static final int DEFAULT_MAX_QUEUE_SIZE = 10000;

    // config option ... if we reuse observation structures, we need
    // to serialize msgs immediately...
    static final boolean SEND_MSGS_INLINE = false;

    String hostname;
    int port;
    int maxQueueSize;

    Object lock;
    LinkedList queue;
    Thread worker;

    ObjectOutputStream oos;

    int debug_outputcounter=0;

    public TCPObservationPublisher() {
	String publishto = System.getProperty( HOSTNAME_ARG );
	String maxqueue = System.getProperty( MAX_QUEUE_ARG );
	int idx = publishto.indexOf( ":" );

	String hostname = null;
	int port = -1;
	int mq = DEFAULT_MAX_QUEUE_SIZE;

	if( idx == -1 ) {
	    hostname = publishto;
	}
	else {
	    hostname = publishto.substring( 0, idx );
	    port = Integer.parseInt( publishto.substring( idx+1 ));
	}

	if( maxqueue != null ) {
	    mq = Integer.parseInt( maxqueue );
	}

	init( hostname, port, mq );
    }


    public TCPObservationPublisher( String hostname, int port ) {
	init( hostname, port, DEFAULT_MAX_QUEUE_SIZE );
    }


    public TCPObservationPublisher( String hostname, int port,
				    int maxQueueSize ) {
	init( hostname, port, maxQueueSize );
    }

    private void init( String hostname, int port,
		       int maxQueueSize ) {
	this.hostname = hostname;
	this.port = port;
	if( this.port == -1 ) 
	    this.port = TCPObservationSubscriber.DEFAULT_PORT;
	this.maxQueueSize = maxQueueSize;

	lock = new Object();

	if( SEND_MSGS_INLINE ) {
	    try {
		initNetworkConnection();
	    }
	    catch( IOException e ) {
		e.printStackTrace();
	    }
	}
	else {
	    queue = new LinkedList();

	    worker = new Thread( new MyWorker() );
	    worker.start();
	}
    }

    public void send( Observation observation ) throws ObservationException {

	if( SEND_MSGS_INLINE ) {
	    synchronized( lock ) {
		try {
		    sendObject( observation );
		}
		catch( Exception e ) {
		    // Thread.sleep( 100 );
		    try {
			initNetworkConnection();
		    } catch( IOException ignore ) { };
		}
	    }
	}
	else {
	    synchronized( lock ) {

		debug_outputcounter++;
		if( debug_outputcounter % 1000 == 0 ) {
		    debug_outputcounter=0;
		    System.err.println( "TCPObservationPublisher: Queuesize=" + queue.size() );
		}

		// admissions control
		if( queue.size() > maxQueueSize ) {
		    System.err.println( "TCPObservationPublisher: admission control, dropping msg" );
		    return;
		}
		
		queue.add( observation );
		lock.notify();
	    }
	}
    }

    void initNetworkConnection() throws IOException {
	Socket s = new Socket( hostname, port );
	OutputStream os = s.getOutputStream();
	oos = new ObjectOutputStream( os );
    }
    
    void sendObject( Object o ) throws IOException {
	oos.writeObject( o );
    }

    void sendObjects( LinkedList items ) throws IOException {
	while( !items.isEmpty() ) {
	    sendObject( items.removeFirst() );
	}
	oos.flush();
	oos.reset();
    }

    class MyWorker implements Runnable {

	public void run() {
	    try {
		initNetworkConnection();

		LinkedList tosend = new LinkedList();

		while( true ) {
		    synchronized( lock ) {
			while( queue.size() == 0 ) {
			    lock.wait();
			}

			tosend.addAll( queue );
			queue.clear();
		    }

		    try {
			sendObjects( tosend );
		    }
		    catch( Exception ex ) {
			ex.printStackTrace();
			// Thread.sleep(100);
			try {
			    initNetworkConnection();
			}
			catch( IOException ignore ) {
			    ignore.printStackTrace();
			};
		    }

		    //tosend.clear();
		}
	    }
	    catch( Exception e ) {
		e.printStackTrace();
	    }
	}

    }

}
