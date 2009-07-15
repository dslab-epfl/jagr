
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: InsufficientCreditException.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.orders.helper;


import com.sun.ecperf.common.*;


/**
 * Class InsufficientCreditException
 *
 *
 * @author
 * @version %I%, %G%
 */
public class InsufficientCreditException extends ECperfException {

    /**
     * Constructor InsufficientCreditException
     *
     *
     */
    public InsufficientCreditException() {}

    /**
     * Constructor InsufficientCreditException
     *
     *
     * @param message
     *
     */
    public InsufficientCreditException(String message) {
        super(message);
    }
}

