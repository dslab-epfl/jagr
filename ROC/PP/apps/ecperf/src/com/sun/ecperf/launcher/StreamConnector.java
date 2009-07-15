/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: StreamConnector.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.launcher;

import java.io.*;

/**
 * StreamConnector provides active piping services
 * processes.
 * @author Akara Sucharitakul
 */
class StreamConnector implements Runnable {

    InputStream input;
    OutputStream output;
    byte[] buffer;
    ByteMatcher matcher= null;
    Thread parent;
    InterruptNotifyable target;

    /**
     * Constructs a new StreamConnector connecting input to output
     * with stream matching.
     * @param input The input
     * @param output The output
     * @param parent The parent thread to be interrupted
     * @param detect String to be matched
     */
    public StreamConnector(InputStream input, OutputStream output,
                           Thread parent, InterruptNotifyable target,
                           String detect) {
        this(input, output);
        this.parent = parent;
        this.target = target;
        if (detect != null && detect.length() != 0)
            matcher = new ByteMatcher(detect);
    }

    /**
     * Constructs a new StreamConnector connecting input to output.
     * @param input The input
     * @param output The output
     */
    public StreamConnector(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
        buffer = new byte[256];
    }

    /**
     * Performs buffered reads from input and writes to output.
     * Checks buffer for the detect string.
     */
    public void run() {
        try {
            for (;;) {
                int len = input.read(buffer);
                if (len < 0)
                    break;
                if (matcher != null && matcher.match(buffer, len)) {
                    target.notifyInterrupt(InterruptNotifyable.MATCH);
                    parent.interrupt();
                    matcher = null;
                }
                output.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!(output.equals(System.out) ||
                  output.equals(System.err)))
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
