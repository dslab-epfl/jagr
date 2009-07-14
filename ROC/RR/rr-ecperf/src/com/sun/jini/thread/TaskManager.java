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

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Collections;

/**
 * A task manager manages a single queue of tasks, and some number of
 * worker threads.  New tasks are added to the tail of the queue.  Each
 * thread loops, taking a task from the queue and running it.  Each
 * thread looks for a task by starting at the head of the queue and
 * taking the first task (that is not already being worked on) that is
 * not required to run after any of the tasks that precede it in
 * the queue (including tasks that are currently being worked on).
 */
public class TaskManager {

    /** The interface that tasks must implement */
    public interface Task extends Runnable {
	/**
	 * Return true if this task must be run after at least one task
	 * in the given task list with an index less than size (size may be
	 * less then tasks.size()).  Using List.get will be more efficient
	 * than List.iterator.
	 *
	 * @param tasks the tasks to consider.  A read-only List, with all
	 * elements instanceof Task.
	 * @param size elements with index less than size should be considered
	 */
	boolean runAfter(List tasks, int size);
    }

    /** Active and pending tasks */
    protected final ArrayList tasks = new ArrayList();
    /** Index of the first pending task; all earlier tasks are active */
    protected int firstPending = 0;
    /** Read-only view of tasks */
    protected final List roTasks = Collections.unmodifiableList(tasks);
    /** Active threads */
    protected final ArrayList threads = new ArrayList();
    /** Maximum number of threads allowed */
    protected final int maxThreads;
    /** Idle time before a thread should exit */
    protected final long timeout;
    /** Threshold for creating new threads */
    protected final float loadFactor;

    /**
     * Create a task manager with maxThreads = 10, timeout = 15 seconds,
     * and loadFactor = 3.0.
     */
    public TaskManager() {
	this(10, 1000 * 15, 3.0f);
    }

    /**
     * Create a task manager.
     *
     * @param maxThreads maximum number of threads to use on tasks
     * @param timeout idle time before a thread exits 
     * @param loadFactor threshold for creating new threads.  A new
     * thread is created if the total number of tasks (both active
     * and pending) exceeds the number of threads times the loadFactor,
     * and the maximum number of threads has not been reached.
     */
    public TaskManager(int maxThreads, long timeout, float loadFactor) {
	this.maxThreads = maxThreads;
	this.timeout = timeout;
	this.loadFactor = loadFactor;
    }

    /**
     * Add a new task if it is not equal to (using the equals method)
     * to any existing active or pending task.
     */
    public synchronized void addIfNew(Task t) {
	if (!tasks.contains(t))
	    add(t);
    }

    /** Add a new task. */
    public synchronized void add(Task t) {
	tasks.add(t);
	pokeThread();
    }

    /** Add all tasks in a collection, in iterator order. */
    public synchronized void addAll(Collection c) {
	tasks.addAll(c);
	for (int i = c.size(); --i >= 0; ) {
	    pokeThread();
	}
    }

    /**
     * Create a new thread if the threshold is exceeded and the maximum
     * number of threads has not yet been reached, else notify a waiting
     * thread (if any).
     */
    private void pokeThread() {
	if (threads.size() < maxThreads && needThread()) {
	    Thread th = new TaskThread();
	    threads.add(th);
	    th.start();
	} else {
	    notify();
	}
    }

    /** Return true if a new thread should be created (ignoring maxThreads). */
    protected boolean needThread() {
	return (tasks.size() > loadFactor * threads.size());
    }

    /**
     * Remove a task if it is pending (not active).  Object identity (==)
     * is used, not the equals method.  Returns true if the task was
     * removed.
     */
    public synchronized boolean removeIfPending(Task t) {
	return removeTask(t, firstPending);
    }

    /*
     * Remove a task if it is pending or active.  If it is active,
     * interrupt the thread executing the task, but do not wait for
     * the thread to terminate.  Object identity (==) is used, not the
     * equals method.  Returns true if the task was removed.
     */
    public synchronized boolean remove(Task t) {
	return removeTask(t, 0);
    }

    /**
     * Remove a task if it has index >= min.  If it is active,
     * interrupt the thread executing the task.
     */
    private boolean removeTask(Task t, int min) {
	for (int i = tasks.size(); --i >= min; ) {
	    if (tasks.get(i) == t) {
		tasks.remove(i);
		if (i >= firstPending)
		    return true;
		firstPending--;
		for (int j = threads.size(); --j >= 0; ) {
		    TaskThread thread = (TaskThread)threads.get(j);
		    if (thread.task == t) {
			thread.interrupt();
			threads.remove(j);
			break;
		    }
		}
		if (firstPending > tasks.size())
		    pokeThread();
		return true;
	    }
	}
	return false;
    }

    /**
     * Interrupt all threads, and stop processing tasks.  Only getPending
     * should be used afterwards.
     */
    public synchronized void terminate() {
	for (int i = threads.size(); --i >= 0; ) {
	    ((Thread)threads.get(i)).interrupt();
	}
    }

    /** Return all pending tasks.  A new list is returned each time. */
    public synchronized ArrayList getPending() {
	ArrayList tc = (ArrayList)tasks.clone();
	for (int i = firstPending; --i >= 0; ) {
	    tc.remove(0);
	}
	return tc;
    }

    private class TaskThread extends Thread {

	/** The task being run, if any */
	public Task task = null;

	public TaskThread() {
	    super("task");
	    setDaemon(true);
	}

	/**
	 * Find the next task that can be run, and mark it taken by
	 * moving firstPending past it (and moving the task in front of
	 * any pending tasks that are skipped due to execution constraints).
	 * If a task is found, set task to it and return true.
	 */
	private boolean takeTask() {
	    int size = tasks.size();
	    for (int i = firstPending; i < size; i++) {
		Task t = (Task)tasks.get(i);
		if (!t.runAfter(roTasks, i)) {
		    if (i > firstPending) {
			tasks.remove(i);
			tasks.add(firstPending, t);
		    }
		    firstPending++;
		    task = t;
		    return true;
		}
	    }
	    return false;
	}

	public void run() {
	    try {
		while (true) {
		    synchronized (TaskManager.this) {
			if (isInterrupted())
			    return;
			if (task != null) {
			    int i;
			    for (i = 0; tasks.get(i) != task; i++)
				;
			    tasks.remove(i);
			    firstPending--;
			    task = null;
			}
			if (!takeTask()) {
			    TaskManager.this.wait(timeout);
			    if (!takeTask()) {
				threads.remove(threads.indexOf(this));
				return;
			    }
			}
		    }
		    task.run();
		}
	    } catch (InterruptedException e) {
	    }
	}
    }
}
