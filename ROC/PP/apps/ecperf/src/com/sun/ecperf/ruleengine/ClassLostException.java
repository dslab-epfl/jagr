
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: ClassLostException.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.ruleengine;


/**
 * Class ClassLostException
 *
 *
 * @author
 * @version %I%, %G%
 */
public class ClassLostException extends RuntimeException {

    /**
     * Constructor ClassLostException
     *
     *
     * @param message
     *
     */
    public ClassLostException(String message) {
        super(message);
    }
}

