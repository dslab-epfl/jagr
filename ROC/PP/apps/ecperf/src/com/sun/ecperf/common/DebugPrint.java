
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: DebugPrint.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.common;


import java.io.*;


/**
 * Class DebugPrint
 *
 *
 * @author
 * @version %I%, %G%
 */
public class DebugPrint extends Debug {

    private static final String refMethod   = "getCaller";
    private static final int    stackOffset = 2;
    private String              clientName  = "";
    private int                 debugLevel;

    /**
     * Constructor DebugPrint
     *
     *
     * @param debugLevel
     * @param client
     *
     */
    public DebugPrint(int debugLevel, Object client) {
        this.debugLevel = debugLevel;
        clientName      = client.getClass().getName() + " ";
    }

    /**
     * Constructor DebugPrint
     *
     *
     * @param debugLevel
     *
     */
    public DebugPrint(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    private static int findLineEnd(int ptr, byte[] buffer) {

        int i;

        for (i = ptr; i < buffer.length; i++) {
            if (buffer[i] == '\n') {
                break;
            }
        }

        return i;
    }

    private static int jumpSpaces(int ptr, byte[] buffer) {

        int i;

        for (i = ptr; i < buffer.length; i++) {
            if ((buffer[i] != ' ') && (buffer[i] != '\t')) {
                break;
            }
        }

        return i;
    }

    private String getCaller() {

        boolean               counting = false;
        int                   counter  = 0;
        String                line     = null;
        ByteArrayOutputStream bout     = new ByteArrayOutputStream();
        PrintStream           pout     = new PrintStream(bout);
        Exception             e        = new Exception();

        e.printStackTrace(pout);

        byte[] inpBuffer = bout.toByteArray();

        for (int bufPtr = 0; bufPtr < inpBuffer.length; ) {
            bufPtr = jumpSpaces(bufPtr, inpBuffer);

            int bufPtr2 = findLineEnd(bufPtr, inpBuffer);

            line   = new String(inpBuffer, bufPtr, bufPtr2 - bufPtr);
            bufPtr = ++bufPtr2;

            if (!counting) {
                counting = line.indexOf(refMethod) >= 0;
            } else {
                ++counter;

                if (counter == stackOffset) {
                    break;
                }
            }
        }

        return line;
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

        if (this.debugLevel >= debugLevel) {
            logTarget.print(clientName + getCaller() + ": " + message);
        }
    }

    /**
     * Method println
     *
     *
     * @param debugLevel
     * @param message
     *
     */
    public void println(int debugLevel, String message) {

        if (this.debugLevel >= debugLevel) {
            logTarget.println(clientName + getCaller() + ": " + message);
        }
    }

    /**
     * Method printStackTrace
     *
     *
     * @param e
     *
     */
    public void printStackTrace(Throwable e) {

        if (debugLevel > 3) {
            e.printStackTrace(logTarget);
        } else {
            super.printStackTrace(e);
        }
    }
}

