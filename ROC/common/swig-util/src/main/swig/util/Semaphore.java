/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

public class Semaphore {
    int val;

    public Semaphore(int val) {
        this.val = val;
    }

    public synchronized void inc() {
        Debug.Assert(val >= 0);

        val++;
        this.notify();
    }

    public synchronized void dec() throws InterruptedException {
        Debug.Assert(val >= 0);

        while (val == 0) {
            this.wait();
        }

        val--;
    }
}