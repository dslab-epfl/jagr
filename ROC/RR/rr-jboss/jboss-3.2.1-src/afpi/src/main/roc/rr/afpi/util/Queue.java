/**
 * $Id: Queue.java,v 1.1 2004/05/06 09:27:22 candea Exp $
 */

package roc.rr.afpi.util;

import java.util.*;

public class Queue
{
    private static List queue = null;

    /**
     * Constructor
     */
    public Queue()
    {
	if ( queue == null )
	    queue = (List) Collections.synchronizedList( new LinkedList() );
    }

    /**
     * Enqueue an element.
     */
    public synchronized void enqueue( Object obj )
    {
	queue.add( obj );
	notifyAll();
    }

    /**
     * Dequeue (blocks caller until element becomes available).
     */
    public synchronized Object dequeue ()
    {
	while( true )
	{
	    try { 
		return queue.remove(0);
	    }
	    catch( IndexOutOfBoundsException idxE ) {
		try {
		    wait();
		} catch( InterruptedException intE ) {}
	    }
	}
    }

    /**
     * Re-initialize queue.
     */
    public void reset ()
    {
	queue = (List) Collections.synchronizedList( new LinkedList() );
    }
}
