/*
 * $Id: AccountDAODupKeyException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.customer.account.exceptions;

/**
 * AccountDAODupKeyException is an exception that extends the
 * AccountDAOAppException. This is thrown by the DAOs of the account
 * component when a row is already found with a given primary key
 */
public class AccountDAODupKeyException extends AccountDAOAppException {

    /**
     * Constructor
     * @param str    a string that explains what the exception condition is
     */
    public AccountDAODupKeyException (String str) {
        super(str);
    }

    /**
     * Default constructor. Takes no arguments
     */
    public AccountDAODupKeyException () {
        super();
    }

}
