
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Scheduler.java,v 1.1.1.1 2002/11/16 05:35:30 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.emulator;

import java.util.*;
import com.sun.jini.thread.*;
import com.sun.ecperf.common.*;

/**
 * Scheduler is a wrapper to the Jini scheduler and thread pool
 * that schedules tasks to be run at a later time. Tasks will
 * be run using the thread pool. It has the inner classes
 * Scheduler.Task and Scheduler.Task.Runner.<br>
 * Scheduler.Task is the task Scheduler inserts into the Jini scheduler
 * which is run in the scheduler's thread. It simply inserts a runner
 * into the tread pool.<br>
 * Scheduler.Task.Runner gets inserted into the thread pool and calls the
 * actual task.<br>
 * Servlet clients just simply construct a Scheduler and call Schedule
 * to schedule the task.
 *
 * @author Akara Sucharitakul
 */

public class Scheduler {

    /** The singleton Jini pool */
    private static TaskManager staticPool;
    /** The singleton Jini scheduler */
    private static WakeupManager staticScheduler;

    /** The Jini pool, certainly not-null */
    TaskManager pool;
    /** The Jini scheduler, also not-null */
    WakeupManager scheduler;
    /** ECperf debugging flag */
    boolean debugging;
    /** ECperf debug class */
    Debug debug;

    /**
     * Returns a reference to the singleton pool, creates it if non-existent.
     * Parameters are used for creation only.
     */
    private static synchronized TaskManager getPool(int maxThreads,
                                  long timeout, float loadFactor) {
        if (staticPool == null) {
            staticPool = new TaskManager(maxThreads, timeout, loadFactor);
        }
        return staticPool;
    }

    /**
     * Returns a reference to the singleton scheduler, creates it if
     * non-existent.
     */
    private static synchronized WakeupManager getScheduler() {
        if (staticScheduler == null) {
            staticScheduler = new WakeupManager();
        }
        return staticScheduler;
    }

    /**
     * Constructs a scheduler.
     * @param maxThreads in case the singleton pool needs creation,
     *                   the maximum number of threads to be created.
     * @param timeout    in case the singleton pool needs creation,
     *                   the life of an inactive thread before it dies. 
     * @param debugging  whether debugging is on or not. 
     * @param debug      the debug object.
     */
    public Scheduler(int maxThreads, long timeout,
                     boolean debugging, Debug debug) {
        this.debugging = debugging;
        this.debug = debug;
        pool = getPool(maxThreads, timeout, 1.0f);
        scheduler = getScheduler();
    }

    /**
     * Schedules a task.
     * @param when the time the task is scheduled to run.
     * @param task the task to run.
     */
    public void schedule(long when, Runnable task) {
        scheduler.schedule(when, new Task(task));
    }

    /**
     * Scheduler.Task is a runnable used for scheduling purposes
     * It will make use of the Jini thread pool to execute the task,
     * but not for sleeping. The required thread pool size should be
     * relatively small.
     */
    class Task implements Runnable {

        /**
         * Runner is the party doing the actual delivery.
         */
        class Runner implements TaskManager.Task {

            /**
             * run is called by the thread pool to execute the task.
             */
            public void run() {
                try {
                    task.run();
                } catch (Exception e) {
                    if (debugging)
                        debug.println(1, e.getMessage());
                    debug.printStackTrace(e);
                }
            }

            /**
             * runAfter, implemented to satisfy TaskManager.Task
             * for the Jini thread pool. Since the Runner is run
             * unconditionally, runAfter always return false.
             * @param tasks a readonly list for tasks this task has
             *              to wait for.
             * @param size  tasks with index less than size needs waiting for.
             */
            public boolean runAfter(List tasks, int size) {
                return false;
            }
        }

        /**
         * The task to actually run.
         */
        Runnable task;

        /**
         * Constructs a Task.
         * @param task	The task to actually run.
         */
        public Task(Runnable task) {
            this.task = task;
        }

        /**
         * run is called by the scheduler to schedule the task.
         * Since this is run in the scheduler thread, it has to
         * do extremely little in order not to delay other scheduled
         * tasks. Here it basically asks the tread pool to do the job.
         */
        public void run() {
            pool.add(new Runner());
        }
    }
}
