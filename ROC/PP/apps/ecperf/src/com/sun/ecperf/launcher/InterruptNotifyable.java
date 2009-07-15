/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: InterruptNotifyable.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */

package com.sun.ecperf.launcher;

/**
 * InterruptNotifyable provides an interface to notify
 * the cause of a thread interruption.
 * @author Akara Sucharitakul
 */
public interface InterruptNotifyable {

    /**
     * MATCH signifies that output or error
     * stream has been matched successfully
     * with given string. It's value is 101.
     */ 
    public static final int MATCH = 101;

    /**
     * Tells the target opject the reason of the interrupt.
     */
    public void notifyInterrupt(int value);
}
