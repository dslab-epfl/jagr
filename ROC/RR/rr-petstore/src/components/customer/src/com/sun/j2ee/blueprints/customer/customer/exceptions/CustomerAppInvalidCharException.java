/*
 * $Id: CustomerAppInvalidCharException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.customer.customer.exceptions;

/**
 * CustomerAppInvalidCharException is an exception that extends the
 * CustomerAppException. This is thrown by the the customer
 * component when there is some failure because of user error
 */
public class CustomerAppInvalidCharException extends CustomerAppException {

    /**
     * Constructor
     * @param str    a string that explains what the exception condition is
     */
    public CustomerAppInvalidCharException (String str) {
        super(str);
    }

    /**
     * Default constructor. Takes no arguments
     */
    public CustomerAppInvalidCharException () {
        super();
    }

}
