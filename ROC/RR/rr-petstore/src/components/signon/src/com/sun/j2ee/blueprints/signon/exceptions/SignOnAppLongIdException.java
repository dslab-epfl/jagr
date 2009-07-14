/*
 * $Id: SignOnAppLongIdException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.signon.exceptions;

/**
 * SignOnAppLongIdException is an exception that extends the
 * SignOnAppException. This is thrown by the the signon
 * component when user id or password specified is too long
 */
public class SignOnAppLongIdException extends SignOnAppException {

    /**
     * Constructor
     * @param str    a string that explains what the exception condition is
     */
    public SignOnAppLongIdException (String str) {
        super(str);
    }

    /**
     * Default constructor. Takes no arguments
     */
    public SignOnAppLongIdException () {
        super();
    }

}
