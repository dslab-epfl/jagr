/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 * OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of
 * any nuclear facility.
 */
package com.sun.j2ee.blueprints.admin.client;

import java.util.*;


public class WorkQueue
{
    private final List queue = new LinkedList();
    private boolean stopped = false;


    public WorkQueue(String name) {
        new WorkerThread(name).start();
    }

    public final void enqueue(Runnable workItem) {
        synchronized(queue) {
            queue.add(workItem);
            queue.notify();
        }
    }

    public final void stop()  {
        synchronized(queue) {
            stopped = true;
            queue.notify();
        }
    }

    private class WorkerThread extends Thread {
        WorkerThread(String name) {
            super(name);
            setPriority(Thread.MIN_PRIORITY);
        }
        public void run() {
            while (true) {
                Runnable workItem = null;
                synchronized(queue) {
                    try {
                        while(queue.isEmpty() && !stopped)
                            queue.wait();
                    }
                    catch(InterruptedException e) {
                        stopped = true;
                    }
                    if (stopped) {
                        return;
                    }
                    workItem = (Runnable)(queue.remove(0));
                }
                workItem.run();
            }
        }
    }
}

