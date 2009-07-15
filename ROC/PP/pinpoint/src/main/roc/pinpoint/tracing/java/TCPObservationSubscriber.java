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
import java.net.*;
import java.util.*;

import roc.pinpoint.tracing.Observation;
import roc.pinpoint.tracing.ObservationException;
import roc.pinpoint.tracing.ObservationSubscriber;

/**
 *  A simple implementation of an Observation subscriber that uses
 *  Java serialization over TCP.
 *  The default port is 17000, though this can be overridden through
 *  the constructor.
 *
 * @author emrek
 *
 */
public class TCPObservationSubscriber implements ObservationSubscriber {

    public static final int DEFAULT_PORT = 17000;

    ServerSocket serverSock;

    Object lock;
    LinkedList queue;

    public TCPObservationSubscriber() {
	this( DEFAULT_PORT );
    }

    public TCPObservationSubscriber( int port ) {
	init( port );
    }

    private void init( int port ) {
	
	try {
	    lock = new Object();
	    queue = new LinkedList();

	    serverSock = new ServerSocket( port );

	    Thread worker = new Thread( new MyWorker());
	    worker.start();
	}
	catch( IOException e ) {
	    e.printStackTrace();
	}

    }


    /**
     * @see roc.pinpoint.tracing.ObservationSubscriber#receive()
     */
    public Observation receive() throws ObservationException {

	Observation ret = null;

	try {
	    synchronized( lock ) {
		while( queue.size() == 0 ) {
		    lock.wait();
		}

		ret = (Observation)queue.removeFirst();
	    }
	}
	catch( InterruptedException e ) {
	    ObservationException t = 
		new ObservationException( "Interrupted while waiting to receive Observation" );
	    t.initCause( e );
	    throw t;
	}

	return ret;
    }

    class MyWorker implements Runnable { 
	
	public void run() {
	    try {
		Socket s = serverSock.accept();
		
		// spawn new worker to listen for more connections
		Thread anotherWorker = new Thread( new MyWorker() );
		anotherWorker.start();

		InputStream is = s.getInputStream();
		ObjectInputStream ois = new ObjectInputStream( is );
		while( true ) {
		    Observation obs = (Observation)ois.readObject();
		    synchronized( lock ) {
			queue.add( obs );
			lock.notify();
		    }
		}
	    }
	    catch( EOFException ignore ) {
	    }
	    catch( Exception e ) {
		e.printStackTrace();
	    }
	}

    }
}
