
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OtherException.java,v 1.1.1.1 2002/11/16 05:35:31 emrek Exp $
 *
 */
package com.sun.ecperf.webbeans;


/**
 * This is an exception class used by helper beans to throw
 * an exception for all the different exceptions that are
 * caught. The message of the exception gives information
 * about the exception it encountered.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public class OtherException extends Exception {

    public Throwable detail;

    /**
     * Constructor OtherException
     *
     *
     */
    public OtherException() {
        super();
    }

    /**
     * Constructor OtherException
     *
     *
     * @param s
     *
     */
    public OtherException(String s) {
        super(s);
    }

    /**
     * Constructor OtherException
     *
     *
     * @param s
     * @param ex
     *
     */
    public OtherException(String s, Throwable ex) {

        super(s);

        detail = ex;
    }
}

