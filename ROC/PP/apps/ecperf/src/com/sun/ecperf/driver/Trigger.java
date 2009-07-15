/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Trigger.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.driver;
 /**
  * This class is used to synchronize all the threads of an Agent.
  * All threads wait on this object's monitor until the Agent
  * signals that the run should begin
  */
class Trigger {
	private int delay;

    public Trigger() {
       super();
    }

    synchronized void waitForRun() {
        try {
            wait();
        } catch (Exception e) {
            System.err.println ("Exception in trigger.wiatForRun(): " + e);
        }
        return;
    }

	synchronized void startRun(int delay) {
		try {
			Thread.sleep(delay);
		} catch(InterruptedException ie) {
		}
		notifyAll();
		return;
	}
}
