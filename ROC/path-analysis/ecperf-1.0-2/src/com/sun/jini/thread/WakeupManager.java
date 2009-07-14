/*
 * 
 * Copyright 2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * The contents of this file are subject to the Sun Community Source License
 * v 3.0/Jini Technology Specific Attachment v 1.0 (the "License"). You may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.sun.com/jini/ . Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License.
 * 
 * The Reference Code is Jini Technology Core Platform code, v 1.1. The
 * Developer of the Reference Code is Sun Microsystems, Inc.
 * 
 * Contributor(s): Sun Microsystems, Inc.
 * 
 * The contents of this file comply with the Jini Technology Core Platform
 * Compatibility Kit, v 1.1A.
 * 
 * Tester(s): Sun Microsystems, Inc.
 * 
 * Test Platform(s):
 * 
 * 	Java 2 SDK, Standard Edition, V 1.2.2_006 Solaris
 * 	   Reference Implementation Release
 * 
 * 	Java 2 SDK, Standard Edition, V 1.2.2_05a Solaris
 * 	   Production Release
 * 
 * 	Java 2 SDK, Standard Edition, V 1.2.2_006 Windows 95/98/NT
 * 	   Production Release
 * 	   
 * Version 1.1
 * 
 */
package com.sun.jini.thread;
import com.sun.jini.constants.TimeConstants;

import java.text.DateFormat;
import java.util.SortedMap;
import java.io.PrintWriter;

/**
 * A Queue of timed tasks.  Each task implements <code>Runnable</code>.
 * Events can either be executed in the queue's thread or in their own thread.
 * <p>
 * A task is an object that implements <code>Runnable</code>.  It is
 * scheduled by invoking <code>schedule</code> with a time at which it
 * should be run.  When that time arrives (approximately) the task will
 * be pulled off the queue and have its <code>run</code> method invoked.
 * <p>
 * A <code>schedule</code> request can specify a
 * <code>WakeupManager.ThreadDesc</code>, which will define the parameters
 * of a thread to be created to run the <code>Runnable</code>.  You can
 * specify the group, whether the thread is a daemon thread, and the priority.
 * <p>
 * When a tasks is scheduled a <code>WakeupManager.Ticket</code> is returned
 * that can be used to cancel the event if desired.
 * <p>
 * The queue requires its own thread, whose parameters can be defined
 * via a <code>ThreadDesc</code> if desired.  This thread will
 * continue indefinitely until <code>stop</code> is called.
 *
 * @author Ken Arnold
 *
 * @see java.lang.Runnable
 */
public class WakeupManager implements TimeConstants {
    private SortedMap	contents;	// the queue
    private long        nextBreaker = 0;// Guarded by contents
    private Ticket      head = null;    // First item in contents 
    private Kicker	kicker;		// the kicker object
    private ThreadDesc	kickerDesc;	// the desc for kicker threads
    private Thread	kickerThread;	// the thread running the kicker

    private static DateFormat dateFmt  = 
	DateFormat.getTimeInstance(DateFormat.LONG);

    private final static PrintWriter DEBUG =
	ThreadDebug.getWriter("wakeupQueue");

    /**
     * Description of a future thread.
     *
     * @see WakeupManager#schedule
     * @see WakeupManager#WakeupManager(WakeupManager.ThreadDesc)
     */
    public static class ThreadDesc {
	private final ThreadGroup group;	// group to create in
	private final boolean daemon;		// create as daemon?
	private final int priority;		// priority

	/**
	 * Equivalent to
	 * <pre>
	 *     ThreadDesc(null, false)
	 * </pre>
	 */
	public ThreadDesc() {
	    this(null, false);
	}

	/**
	 * Equivalent to
	 * <pre>
	 *     ThreadDesc(group, deamon, Thread.NORM_PRIORITY)
	 * </pre>
	 */
	public ThreadDesc(ThreadGroup group, boolean daemon) {
	    this(group, daemon, Thread.NORM_PRIORITY);
	}

	/**
	 * Describe a future thread that will be created in the given group,
	 * deamon status, and priority.
	 *
	 * @param group The group to be created in.  If <code>null</code>,
	 *		the thread will be created in the default group.
	 * @param daemon The thread will be a daemon thread if this is
	 *		<code>true</code>.
	 * @param priority The thread's priority.
	 */
	public ThreadDesc(ThreadGroup group, boolean daemon, int priority) {
	    this.group = group;
	    this.daemon = daemon;
	    this.priority = priority;
	}

	/**
	 * Create a thread for the given runnable based on the values in this
	 * object.
	 */
	private Thread thread(Runnable r) {
	    Thread thr;
	    if (group == null)
		thr = new Thread(r);
	    else
		thr = new Thread(group, r);
	    thr.setDaemon(daemon);
	    thr.setPriority(priority);
	    return thr;
	}

	public String toString() {
	    return "[" + group + ", " + daemon + ", " + priority + "]";
	}
    }

    /**
     * A ticket that can be used for cancelling a future task.  It describes
     * the task itself as well.
     */
    public static class Ticket implements Comparable {
	/** When the task should occur. */
	public final long when;
	/** The task object to be executed */
	public final Runnable task;
	/** The <code>ThreadDesc</code>, or <code>null</code> if none. */
	public final ThreadDesc desc;

	/** Tie beaker used when two tickets have the same value for when */
	private final long breaker;

        Ticket(long when, Runnable task, ThreadDesc threadDesc,
	     long breaker)
	{
	    if (task == null)
		throw new NullPointerException("task not specified");
	    this.when = when;
	    this.task = task;
	    this.desc = threadDesc;
	    this.breaker = breaker;
	}

	public String toString() {
	    return dateFmt.format(new Long(when)) + "(" + when + ")" + ", "
		+ task.getClass().getName() + ", " + desc;
	}

	public boolean equals(Object o) {
	    if (!(o instanceof Ticket))
		return false;

	    final Ticket that = (Ticket)o;

	    return that.when == when && that.breaker == breaker;
	}

	public int compareTo(Object o) {
	    final Ticket that = (Ticket)o;	    
	    
	    final long whenDiff = when - that.when;
	    if (whenDiff > 0)
		return 1;
	    else if (whenDiff < 0)
		return -1;
	    else {
		final long breakerDiff = breaker - that.breaker;	

		if (breakerDiff > 0)
		    return 1;
		else if (breakerDiff < 0)
		    return -1;
		else
		    return 0;
	    }
	}
    }

    /**
     * Create a new <code>WakeupManager</code>. Equivalent to.
     * <pre>
     *     WakeupManager(new ThreadDesc())
     * </pre>
     *
     * @see WakeupManager.ThreadDesc
     */
    public WakeupManager() {
	this(new ThreadDesc());
    }

    /**
     * Create a new <code>WakeupManager</code>.  The thread used for
     * timing will be created according to the provided <code>ThreadDesc</code>.
     */
    public WakeupManager(ThreadDesc desc) {
	kickerDesc = desc;
	contents = new java.util.TreeMap();
	kicker = new Kicker();
	kickerThread = kickerDesc.thread(kicker);
	kickerThread.start();
    }

    /**
     * Schedule the given task for the given time.  The task's <code>run</code>
     * method will be executed synchronously in the queue's own thread, so it
     * should be brief or it will affect whether future events will be executed
     * at an appropriate time.
     */
    public Ticket schedule(long when, Runnable task) {
	return schedule(when, task, null);
    }

    /**
     * Schedule the given task for the given time, to be run in a thread.
     * When the time comes, a new thread will be created according to the
     * <code>ThreadDesc</code> object provided.  If <code>threadDesc</code> is
     * <code>null</code>, this is equivalent to the other form of
     * <code>schedule</code>.
     */
    public Ticket schedule(long when, Runnable task, ThreadDesc threadDesc) {
	if (task == null)
	    throw new NullPointerException("task");

	synchronized (contents) {
	    Ticket t = new Ticket(when, task, threadDesc, nextBreaker++);
	    contents.put(t,t);
	    checkHead();
	    return t;
	}
    }

    /**
     * Cancel the given ticket.
     */
    public void cancel(Ticket t) {
	synchronized (contents) {
	    contents.remove(t);
	    checkHead();
	}
    }

    /**
     * Cancel all tickets.
     */
    public void cancelAll() {
	synchronized (contents) {
	    contents.clear();
	    checkHead();
	}
    }


    /** Called whenever we change contents to see if there is a new value
	for head to see if we have to tell the kicker */
    private void checkHead() {
        // We are always called from code syncronized on contents
	final Ticket oldHead = head;

	if (contents.isEmpty())
	    head = null;
	else
	    head = (Ticket)contents.firstKey();

	if (head == oldHead) return;

	// New first event, kicker needs to wake up and change its sleep time
	kicker.newTime();
    }

    /**
     * Return whether the queue is currently empty.
     */
    public boolean isEmpty() {
	synchronized (contents) {
	    return (contents.isEmpty());
	}
    }

    /**
     * Stop executing.
     */
    public void stop() {
	kicker.kill();
    }

    /**
     * The kicker work.  This is what sleeps until the time of
     * the next event.
     */
    private class Kicker implements Runnable {
	/** 
	 * Time to sleep until. Note, this field is only read/writen
	 * from run() and the methods it calls.
	 */
	private long sleepTime;

	/**
	 * True if the head of contents has not changed since
	 * sleepTime was last set.
	 */
	private boolean sleepTimeValid;   

	/** Should I die instead of continue? */
	private boolean die = false;

	/**
	 * Keep running along, executing tasks and sleeping.  Most of
	 * the work is in the <code>doTasks</code> method.
	 */
	public void run() {
	    while (true) {
		doTasks();		// figures out the next sleepTime

		if (DEBUG != null) {
		    DEBUG.println("sleepTime = " +
			(sleepTime == Long.MAX_VALUE ? "***FOREVER***" :
			                               sleepTime + ""));
		}
		
		synchronized (this) {
		    try {
			// Only wait if sleepTime has not been
			// invalidated by a change to contents,
			// otherwise drop through and re-calc
			// sleepTime.  Note, we used to have a race
			// condition here, thanks to Kenneth Olwing
			// <kenneth.olwing@eoncompany.com> for
			// submitting the problem report that pointed
			// to this race condition.  His help was also
			// invaluable for testing the fix.
			if (sleepTimeValid && sleepTime > 0 && !die)
			    wait(sleepTime);
		    } catch (InterruptedException e) {
			// Fall through to if below
		    }

		    if (die)
			return;
		}
	    }
	}

	/**
	 * Kill off this thread
	 */
	private synchronized void kill() {
	    die = true;
	    kickerThread.interrupt();
	}

	/**
	 * A new time has arrived -- if we're sleeping, wake up. If we
	 * are not sleeping invalidate the sleepTime so we don't 
	 * endup with the wrong time.  Note, this method is only
	 * called when the caller owns the lock on contents.
	 */
	private synchronized void newTime() {
	    sleepTimeValid = false;
	    notifyAll();
	}

	/**
	 * Execute the tasks whose time has come, and calculate the
	 * next sleep time.
	 */
	private void doTasks() {
	    Ticket t;

	    do {
		synchronized (contents) {
		    // We are going to come up with a sleep time that
		    // is valid for the curret head of contents.
		    synchronized (this) {
			sleepTimeValid = true;
		    }

		    // It's ok to get the head (or decide that
		    // contents is empty) while not holding the lock
		    // on this because contents can't get a new head
		    // until we let go of contents' lock.

		    if (contents.isEmpty()) {           
			// nothing to do sleep forever
			sleepTime = Long.MAX_VALUE;
			continue;
		    }

		    t = (Ticket)contents.firstKey();
		}

		// Now if the head changes ether it will happen before
		// the wait in run() and sleepTimeValid will be set to
		// false before we test it, or while we are in the
		// wait() and the notifyAll() will wake us up
		
		sleepTime = t.when - System.currentTimeMillis();

		if (sleepTime <= 0) {		// it's time has come
		    // consume it
		    synchronized (contents) { 
			if (t != contents.remove(t))
			    // Someone must have not wanted it to
			    // run and removed it! 
                            continue;
		    }

		    // run it
		    if (t.desc == null)
			t.task.run();	               // ... in this thread
		    else			
			t.desc.thread(t.task).start(); // ... in its own thread

		    // we may have been interrupted to die
		    if (kickerThread.isInterrupted())
			return;
		}
	    // While the head's when may already be here...
	    } while (sleepTime <= 0);
	}
    }
}
