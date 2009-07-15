
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: InvalidEntryException.java,v 1.2 2003/03/22 04:55:02 emrek Exp $
 *
 */
package com.sun.ecperf.webbeans;


/**
 * This is an exception class used by helper beans to throw
 * an exception when invalid entry is entered.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public class InvalidEntryException extends Exception {

    /**
     * Constructor InvalidEntryException
     *
     *
     */
    public InvalidEntryException() {
        super();
    }

    /**
     * Constructor InvalidEntryException
     *
     *
     * @param s
     *
     */
    public InvalidEntryException(String s) {
        super(s);
    }
}

