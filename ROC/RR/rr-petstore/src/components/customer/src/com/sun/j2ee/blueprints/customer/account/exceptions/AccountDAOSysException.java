/*
 * $Id: AccountDAOSysException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.customer.account.exceptions;

import java.lang.RuntimeException;

/**
 * AccountDAOSysException is an exception that extends the standard
 * RunTimeException Exception. This is thrown by the DAOs of the account
 * component when there is some irrecoverable error (like SQLException)
 */
public class AccountDAOSysException extends RuntimeException {

    /**
     * Constructor
     * @param str    a string that explains what the exception condition is
     */
    public AccountDAOSysException (String str) {
        super(str);
    }

    /**
     * Default constructor. Takes no arguments
     */
    public AccountDAOSysException () {
        super();
    }

}
