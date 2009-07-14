
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: InvalidInfoException.java,v 1.1.1.1 2002/11/16 05:35:25 emrek Exp $
 *
 */
package com.sun.ecperf.common;


/**
 * Class InvalidInfoException
 *
 *
 * @author
 * @version %I%, %G%
 */
public class InvalidInfoException extends ECperfException {

    /**
     * Catches exceptions without a specified string
     *
     */
    public InvalidInfoException() {}

    /**
     * Constructs the appropriate exception with the specified string
     *
     * @param message           Exception message
     */
    public InvalidInfoException(String message) {
        super(message);
    }
}

