// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: BlockingQueue.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ---------------------------------------------------------------------------

package org.mortbay.util;

/* ------------------------------------------------------------ */
/** Blocking queue.
 *
 * XXX temp implementation. Should use java2 containers.
 * Implemented as circular buffer in a Vector. Synchronization is on the
 * vector to avoid double synchronization.
 *
 * @version $Id: BlockingQueue.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Greg Wilkins (gregw)
 */
public class BlockingQueue
{
    Object[] elements;
    int maxSize;
    int size=0;
    int head=0;
    int tail=0;

    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public BlockingQueue(int maxSize)
    {
        this.maxSize=maxSize;
        if (maxSize==0)
            this.maxSize=255;
        elements = new Object[this.maxSize];
    }

    /* ------------------------------------------------------------ */
    public int size()
    {
        return size;
    }
    
    /* ------------------------------------------------------------ */
    public int maxSize()
    {
        return maxSize;
    }
    
  
    /* ------------------------------------------------------------ */
    /** Put object in queue.
     * @param o Object
     */
    public void put(Object o)
        throws InterruptedException
    {
        synchronized(elements)
        {
            while (size==maxSize)
                elements.wait();

            elements[tail]=o;
            if(++tail==maxSize)
                tail=0;
            size++;
            elements.notify();
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Put object in queue.
     * @param timeout If timeout expires, throw InterruptedException
     * @param o Object
     * @exception InterruptedException Timeout expired or otherwise interrupted
     */
    public void put(Object o, int timeout)
        throws InterruptedException
    {
        synchronized(elements)
        {
            if (size==maxSize)
            {
                elements.wait(timeout);
                if (size==maxSize)
                    throw new InterruptedException("Timed out");
            }
            
            elements[tail]=o;
            if(++tail==maxSize)
                tail=0;
            size++;
            elements.notify();
        }
    }

    /* ------------------------------------------------------------ */
    /** Get object from queue.
     * Block if there are no objects to get.
     * @return The next object in the queue.
     */
    public Object get()
        throws InterruptedException
    {
        synchronized(elements)
        {
            while (size==0)
                elements.wait();
            
            Object o = elements[head];
            elements[head]=null;
            if(++head==maxSize)
                head=0;
            if (size==maxSize)
                elements.notifyAll();
            size--;
            return o;
        }
    }
    
        
    /* ------------------------------------------------------------ */
    /** Get from queue.
     * Block for timeout if there are no objects to get.
     * @param timeoutMs the time to wait for a job
     * @return The next object in the queue, or null if timedout.
     */
    public Object get(int timeoutMs)
        throws InterruptedException
    {
        synchronized(elements)
        {
            if (size==0)
                elements.wait((long)timeoutMs);
            
            if (size==0)
                return null;
            
            Object o = elements[head];
            elements[head]=null;
            if(++head==maxSize)
                head=0;

            if (size==maxSize)
                elements.notifyAll();
            size--;
            
            return o;
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Peek at the  queue.
     * Block  if there are no objects to peek.
     * @return The next object in the queue, or null if timedout.
     */
    public Object peek()
        throws InterruptedException
    {
        synchronized(elements)
        {
            if (size==0)
                elements.wait();
            
            if (size==0)
                return null;
            
            Object o = elements[head];
            return o;
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Peek at the  queue.
     * Block for timeout if there are no objects to peek.
     * @param timeoutMs the time to wait for a job
     * @return The next object in the queue, or null if timedout.
     */
    public Object peek(int timeoutMs)
        throws InterruptedException
    {
        synchronized(elements)
        {
            if (size==0)
                elements.wait((long)timeoutMs);
            
            if (size==0)
                return null;
            
            Object o = elements[head];
            return o;
        }
    }
}








