
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Debug.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.common;

import java.io.PrintStream;

import java.rmi.RemoteException;

import javax.ejb.EJBException;

/**
 * Class Debug
 *
 *
 * @author
 * @version %I%, %G%
 */
public class Debug implements java.io.Serializable {

    transient PrintStream logTarget = System.err;

    /**
     * Method setLogTarget
     *
     *
     * @param target The the new log destination to be used.
     *
     */
    public void setLogTarget(PrintStream target) {
        logTarget = target;
    }

    /**
     * Method getLogTarget
     *
     *
     * @return The PrintStream used as the current log target
     *
     */
    public PrintStream getLogTarget() {
        return logTarget;
    }

    /**
     * Method print
     *
     *
     * @param debugLevel
     * @param message
     *
     */
    public void print(int debugLevel, String message) {

        // Do nothing.
    }

    /**
     * Method println
     *
     *
     * @param debugLevel
     * @param Message
     *
     */
    public void println(int debugLevel, String Message) {

        // Do nothing.
    }

    /**
     * Method printStackTrace
     *
     *
     * @param e
     *
     */
    public void printStackTrace(Throwable e) {

        // Yes, we print the stack trace for an originating
        // exception, but nothing else.
        // If e is not a remote exception
        // or if e.detail is null
        // or if e.detail is not an EJBException
        // Print the stack
        // Otherwise the stack has already been printed, don't print.
        if (!(e instanceof RemoteException)
                || ((RemoteException) e).detail == null
                ||!(((RemoteException) e).detail instanceof EJBException)) {
            e.printStackTrace(logTarget);
        }
    }
}

