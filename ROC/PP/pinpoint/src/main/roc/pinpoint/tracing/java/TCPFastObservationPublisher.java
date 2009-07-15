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

import org.apache.log4j.Logger;

public class TCPFastObservationPublisher implements ObservationPublisher {

    static Logger log = Logger.getLogger( "TCPFastObservationPublisher" );

    public static final String HOSTNAME_ARG = 
	"roc.pinpoint.publishto.hostname";
    public static final String MAX_QUEUE_ARG = 
	"roc.pinpoint.publishto.maxqueue";

    public static final int DEFAULT_MAX_QUEUE_SIZE = 1000;

    // config option ... if we reuse observation structures, we need
    // to serialize msgs immediately...
    static final boolean SEND_MSGS_INLINE = false;

    String hostname;
    int port;
    int maxQueueSize;

    Object lock;
    LinkedList queue;
    Thread worker;

    DataOutputStream os;

    public TCPFastObservationPublisher() {
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


    public TCPFastObservationPublisher( String hostname, int port ) {
	init( hostname, port, DEFAULT_MAX_QUEUE_SIZE );
    }


    public TCPFastObservationPublisher( String hostname, int port,
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
		    sendObservation( observation );
		}
		catch( Exception e ) {
		    log.error( "EMKDEBUG: Could not send observation " + observation.toString() );
		    e.printStackTrace();
		    // Thread.sleep( 100 );
		    try {
			initNetworkConnection();
		    } catch( IOException ignore ) { };
		}
	    }
	}
	else {
	    synchronized( lock ) {
		// admissions control
		if( queue.size() > maxQueueSize ) {
		    log.warn( "admission control: dropping msg" );
		    observation.recycle();
		    return;
		}
		
		queue.add( observation );
		lock.notify();
	    }
	}
    }

    void initNetworkConnection() throws IOException {
	Socket s = new Socket( hostname, port );
	os = new DataOutputStream(  s.getOutputStream() );
    }
    
    void sendObservation( Observation o ) throws IOException {
        o.writeExternalD( os );
	o.recycle();
    }

    void sendObservations( LinkedList items ) throws IOException {
	while( !items.isEmpty() ) {
	    sendObservation( (Observation)items.removeFirst() );
	}
	//os.flush();
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
			sendObservations( tosend );
		    }
		    catch( Exception ex ) {
			// Thread.sleep(100);
			try {
			    initNetworkConnection();
			}
			catch( IOException ignore ) { 
			    ignore.printStackTrace();
			};
		    }
		}
	    }
	    catch( Exception e ) {
		e.printStackTrace();
	    }
	}

    }

}
