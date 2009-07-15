
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: ECperfException.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.common;


/**
 * Class ECperfException
 *
 *
 * @author
 * @version %I%, %G%
 */
public class ECperfException extends Exception {

    private Exception nextException;

    /**
     * Catches exceptions without a specified string
     *
     */
    public ECperfException() {}

    /**
     * Constructor ECperfException
     *
     *
     * @param previous
     *
     */
    public ECperfException(Exception previous) {
        nextException = previous;
    }

    /**
     * Constructs the appropriate exception with the specified string
     *
     * @param message           Exception message
     */
    public ECperfException(String message) {
        super(message);
    }

    /**
     * Constructor ECperfException
     *
     *
     * @param previous
     * @param message
     *
     */
    public ECperfException(Exception previous, String message) {

        super(message);

        nextException = previous;
    }

    /**
     * Method getNextException
     *
     *
     * @return
     *
     */
    public Exception getNextException() {
        return nextException;
    }
}

