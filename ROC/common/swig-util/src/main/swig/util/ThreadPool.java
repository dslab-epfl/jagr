/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * ThreadPool keeps around a pool of worker threads, and dispatches
 * jobs to them, creating new workers and deleting old workers as
 * needed.  The idea is to generally avoid the overhead of creating
 * a new thread for a short task.
 *
 * $Id: ThreadPool.java,v 1.1 2003/04/22 20:09:37 emrek Exp $
 *
 * $Log: ThreadPool.java,v $
 * Revision 1.1  2003/04/22 20:09:37  emrek
 * * populated swig-util directory ROC/common/swig-util
 *
 * Revision 1.1.1.1  2003/03/07 08:12:40  emrek
 * * first checkin of PP to the new ROC/PP/ subdir after reorg
 *
 * Revision 1.2  2002/12/28 12:27:30  emrek
 * no functional changes, just formatting and general cleanup. also did some javadoc'ing of roc.pinpoint.** classes.
 *
 * Revision 1.1  2002/12/17 15:27:43  emrek
 * first commit of new pinpoint tracing and analysis framework
 *
 * Revision 1.3  2002/08/19 06:49:43  emrek
 * Added copyright information to source files
 *
 * Revision 1.2  2002/08/15 22:08:07  emrek
 * formatting changes (only) because of new editor
 *
 * Revision 1.1.1.1  2002/07/17 09:07:55  emrek
 *
 *
 * Revision 1.1.1.1  2001/10/17 00:53:46  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.1  2001/03/13 09:24:43  emrek
 * *** empty log message ***
 *
 *
 */
public class ThreadPool {
    List jobs;
    Set allthreads;
    int numfree;
    int minsize;
    int maxsize;
    double newthreshold;
    /* if < newthreshold are free, than create a
        new thread */
    double diethreshold; /* if > diethreshold are free, than kill a worker */

    public ThreadPool(
        int minsize,
        int maxsize,
        double newthreshold,
        double diethreshold) {
        Debug.Assert(minsize > 0);
        Debug.Assert(minsize < maxsize);
        Debug.Assert(newthreshold < diethreshold);
        this.minsize = minsize;
        this.maxsize = maxsize;
        this.newthreshold = newthreshold;
        this.diethreshold = diethreshold;

        allthreads = new HashSet();
        jobs = new LinkedList();

        for (int i = 0; i < minsize; i++) {
            makeNewWorker();
        }
    }

    public ThreadPool(int minsize, int maxsize) {
        this(minsize, maxsize, 0.2, 0.4);
    }

    public ThreadPool() {
        this(1, 1000);
    }

    void makeNewWorker() {
        Thread w = new Thread(new Worker());
        allthreads.add(w);
        w.start();
    }

    public void submitJob(Runnable r) {
        synchronized (jobs) {
            if ((numfree < newthreshold * allthreads.size())
                && (allthreads.size() < maxsize)) {
                makeNewWorker();
            }

            jobs.add(r);
            jobs.notify();
        }
    }

    class Worker implements Runnable {
        public void run() {
            Debug.Print("threadpool", "starting new worker");

            while (true) {
                Runnable r = null;
                synchronized (jobs) {
                    numfree++;

                    try {
                        while (jobs.size() == 0) {
                            jobs.wait();
                        }
                    }
                    catch (InterruptedException e) {
                    }

                    r = (Runnable) jobs.remove(0);

                    numfree--;
                }

                r.run();
                synchronized (jobs) {
                    if ((numfree > diethreshold * allthreads.size())
                        && (allthreads.size() > minsize)) {
                        break;
                    }
                }
            }

            Debug.Print("threadpool", "killing new worker");
        }
    }
}