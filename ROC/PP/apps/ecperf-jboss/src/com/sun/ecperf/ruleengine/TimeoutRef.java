
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: TimeoutRef.java,v 1.1 2004/02/19 14:45:09 emrek Exp $
 *
 */
package com.sun.ecperf.ruleengine;


import java.util.HashMap;


/**
 * Class TimeoutRef keeps a list of references to any object. These objects
 * references will be kept for at least n milliseconds where n is the
 * timeout value given at construction. Afterwards they will be released as
 * soon as the TimeoutRef becomes active. No threading is used here and
 * the class is not thread safe. It needs to be wrapped around a thread-safe
 * wrapper if thread safety is needed.
 *
 * Object references can only be added but not be retrieved and they will
 * be purged automatically after timeout. The major use of this class
 * is to work with the java.lang.ref package and maintain a timed hard
 * reference to objects otherwise only softly or weakly reachable.
 *
 * This influences the garbage collection behaviour so these objects
 * will be kept softly/weakly referencable for at least the defined time
 * thus implementing a minimum-time caching behaviour.
 *
 * Unlike the use of collections where only objects of a single type
 * are encouraged to be populated in any one collection object, this
 * TimeoutRef encourages use of multiple object types and high number of
 * objects so it is kept active (hence no threading).
 *
 * @author Akara Sucharitakul
 */
public class TimeoutRef {

    Queue   list;    // Reference list
    Queue   pool;    // Entry pool
    HashMap refs;
    long    timeout;

    /**
     * Constructor TimeoutRef
     *
     *
     * @param timeout
     *
     */
    public TimeoutRef(long timeout) {

        list         = new Queue();
        pool         = new Queue();
        refs         = new HashMap();
        this.timeout = timeout;
    }

    /**
     * Method add
     *
     *
     * @param x
     *
     */
    public void add(Object x) {

        long    tm         = System.currentTimeMillis();
        Entry   reuseEntry = null;
        boolean pushed     = false;

        // Check if entry exists and move it to the beginning of
        // the queue
        Entry entry = (Entry) refs.get(x);

        if (entry != null) {
            entry.lastAccess = tm;

            list.repush(entry);

            pushed = true;
        }

        // Remove all obsolete entries (from beginning, first)
        entry = list.peek();

        while ((entry != null) && (tm - entry.lastAccess > timeout)) {
            if (reuseEntry != null) {
                refs.remove(reuseEntry.ref);

                reuseEntry.ref = null;    // Drop reference

                pool.push(reuseEntry);
            }

            reuseEntry = list.pop();
            entry      = list.peek();
        }

        // Reuse entry if possible
        if (pushed) {
            if (reuseEntry != null) {
                refs.remove(reuseEntry.ref);

                reuseEntry.ref = null;

                pool.push(reuseEntry);
            }
        } else {
            entry = reuseEntry;         // Take the current entry

            if (entry == null) {        // If none has been taken from list
                entry = pool.pop();     // Take from pool
            }

            if (entry == null) {        // If pool is empty
                entry = new Entry();    // Create new one.
            }

            // Add new entry to the queue
            entry.ref        = x;
            entry.lastAccess = tm;

            refs.put(x, entry);
            list.push(entry);
        }
    }

    class Entry {

        Object ref;
        long   lastAccess;
        Entry  prevEntry, nextEntry;
    }

    class Queue {

        Entry start = null, end = null;

        void push(Entry entry) {

            entry.prevEntry = null;
            entry.nextEntry = start;

            if (start != null) {
                start.prevEntry = entry;
            }

            start = entry;

            if (end == null) {
                end = entry;
            }
        }

        void repush(Entry entry) {

            if (entry.prevEntry != null) {
                entry.prevEntry.nextEntry = entry.nextEntry;
            }

            if (entry.nextEntry != null) {
                entry.nextEntry.prevEntry = entry.prevEntry;
            }

            push(entry);
        }

        Entry peek() {
            return end;
        }

        Entry pop() {

            Entry entry = null;

            if (end != null) {
                entry = end;
                end   = entry.prevEntry;

                if (end != null) {
                    end.nextEntry = null;
                } else {
                    start = null;
                }

                entry.prevEntry = null;
                entry.nextEntry = null;
            }

            return entry;
        }
    }
}

