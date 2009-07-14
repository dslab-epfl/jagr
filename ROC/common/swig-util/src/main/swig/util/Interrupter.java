/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 *
 * $Id: Interrupter.java,v 1.1 2003/04/22 20:09:37 emrek Exp $
 *
 * $Log: Interrupter.java,v $
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
 * Revision 1.1.1.1  2002/07/17 09:07:52  emrek
 *
 *
 * Revision 1.1.1.1  2001/10/17 00:53:46  emrek
 * initial checkin of code that needs a better name than 'u'
 *
 * Revision 1.3  2001/04/25 14:50:01  emrek
 * set Interrupter thread to be a Daemon thread
 *
 * Revision 1.2  2001/03/13 11:28:38  emrek
 * *** empty log message ***
 *
 * Revision 1.1  2001/03/13 09:24:43  emrek
 * *** empty log message ***
 *
 *
 */
public class Interrupter {
    private Interrupter() {
    };
    static Object lock = new Object();
    public static final long PRECISION = 100;
    static boolean initialized = false;
    static boolean globalstop;
    static SortedSet schedule;
    static Thread worker;
    static int currid = 0;

    public static void Initialize() {
        synchronized (lock) {
            if (!initialized) {
                Debug.Print("interrupt", "initializing interrupter");
                schedule = new TreeSet();
                globalstop = false;
                worker = new Thread(new InterrupterWorker());
                worker.setDaemon(true);
                worker.start();
                initialized = true;
            }
        }
    }

    public static int RegisterInterruption(Thread t, long timetowait) {
        Debug.Print("interrupt", "registering interruption");

        if (!initialized) {
            Initialize();
        }

        int ret;
        long currenttime = System.currentTimeMillis();
        synchronized (lock) {
            InterrupterRequest req = new InterrupterRequest();
            req.t = t;
            req.nextschedule = timetowait + currenttime;
            req.id = currid++;
            ret = req.id;
            schedule.add(req);
            worker.interrupt();
        }

        return ret;
    }

    public static int RegisterInterruption(long timetowait) {
        return RegisterInterruption(Thread.currentThread(), timetowait);
    }

    public static boolean UnregisterInterruption(int id) {
        Debug.Print("interrupt", "unregistering interruption");
        synchronized (lock) {
            Iterator iter = schedule.iterator();

            while (iter.hasNext()) {
                InterrupterRequest req = (InterrupterRequest) iter.next();

                if (req.id == id) {
                    schedule.remove(req);

                    return true;
                }
            }
        }

        return false;
    }

    static long sleepfor() {
        long ret = 0;
        synchronized (lock) {
            if (schedule.size() > 0) {
                try {
                    InterrupterRequest r =
                        (InterrupterRequest) schedule.first();
                    ret = r.nextschedule - System.currentTimeMillis();
                }
                catch (NoSuchElementException e) {
                    Debug.Assert(false);
                }
            }
            else {
                ret = 5000;
            }
        }

        Debug.Print("interrupt", "sleep for " + ret);

        return (ret > PRECISION) ? ret : 0;
    }

    static InterrupterRequest[] getOutstandingRequests() {
        InterrupterRequest[] ret = null;

        Debug.Print("interrupt", "getoutstanding requests...");
        synchronized (lock) {
            InterrupterRequest dummy = new InterrupterRequest();
            long now = System.currentTimeMillis();

            dummy.nextschedule = now + PRECISION;
            SortedSet s = schedule.headSet(dummy);
            ret =
                (InterrupterRequest[]) s.toArray(
                    new InterrupterRequest[s.size()]);

            for (int i = 0; i < ret.length; i++) {
                InterrupterRequest r = ret[i];
                schedule.remove(r);
            }
        }

        Debug.Print("interrupt", "...returning " + ret);

        return ret;
    }
}

class InterrupterWorker implements Runnable {
    public void run() {
        int i;

        InterrupterRequest[] r;

        while (!Interrupter.globalstop) {
            try {
                long s = Interrupter.sleepfor();

                if (s > 0) {
                    Debug.Print("interrupt", "going to sleep");
                    Thread.sleep(s);
                }
            }
            catch (InterruptedException e) {
            }

            Debug.Print("interrupt", "woke up");

            r = Interrupter.getOutstandingRequests();
            Debug.Print("interrupt", "got requests");

            for (i = 0; i < r.length; i++) {
                Debug.Print("interrupt", "interrupting thread...");
                r[i].t.interrupt();
            }
        }
    }
}

class InterrupterRequest implements Comparable {
    long nextschedule;
    Thread t;
    int id;

    public int compareTo(Object o) {
        if (o instanceof InterrupterRequest) {
            InterrupterRequest r = (InterrupterRequest) o;

            if (this.nextschedule < r.nextschedule) {
                return -1;
            }
            else if (
                (this.nextschedule == r.nextschedule)
                    && (this.hashCode() < r.hashCode())) {
                return -1;
            }
            else if (this.equals(r)) {
                return 0;
            }
            else {
                return 1;
            }
        }
        else {
            throw new ClassCastException("can't compare object to InterrupterRequest");
        }
    }
}