/*
 * $Id: SignOnDAODupKeyException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.signon.exceptions;

/**
 * SignOnDAODupKeyException is an exception that extends the
 * SignOnDAOAppException. This is thrown by the DAOs of the signon
 * component when a row is already found with a given primary key
 */
public class SignOnDAODupKeyException extends SignOnDAOAppException {

    /**
     * Constructor
     * @param str    a string that explains what the exception condition is
     */
    public SignOnDAODupKeyException (String str) {
        super(str);
    }

    /**
     * Default constructor. Takes no arguments
     */
    public SignOnDAODupKeyException () {
        super();
    }

}
