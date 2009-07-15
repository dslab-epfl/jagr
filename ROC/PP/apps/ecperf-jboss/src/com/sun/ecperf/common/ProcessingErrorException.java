
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: ProcessingErrorException.java,v 1.1 2004/02/19 14:45:10 emrek Exp $
 *
 */
package com.sun.ecperf.common;


/**
 * Class ProcessingErrorException
 *
 *
 * @author
 * @version %I%, %G%
 */
public class ProcessingErrorException extends Exception {

    private Exception nextException;

    /**
     * Catches exceptions without a specified string
     *
     */
    public ProcessingErrorException() {}

    /**
     * Constructor ProcessingErrorException
     *
     *
     * @param previous
     *
     */
    public ProcessingErrorException(Exception previous) {
        nextException = previous;
    }

    /**
     * Constructs the appropriate exception with the specified string
     *
     * @param message           Exception message
     */
    public ProcessingErrorException(String message) {
        super(message);
    }

    /**
     * Constructor ProcessingErrorException
     *
     *
     * @param previous
     * @param message
     *
     */
    public ProcessingErrorException(Exception previous, String message) {

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

