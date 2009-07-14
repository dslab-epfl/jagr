
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: NotReadyException.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.common;


/**
 * NotReadyException indicates that the operation being performed
 * hits a "not ready" state and should be retried at a later point
 * in time. It is up to the application logic to schedule for a
 * retry if this exception is received.
 *
 * @author Akara Sucharitakul
 * @version %I%, %G%
 */
public class NotReadyException extends ECperfException {

    /**
     * Catches exceptions without a specified string
     *
     */
    public NotReadyException() {}

    /**
     * Constructs the appropriate exception with the specified string
     *
     * @param message           Exception message
     */
    public NotReadyException(String message) {
        super(message);
    }
}

