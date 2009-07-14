/*
 * $Id: SigninFailedException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.petstore.control.exceptions;

/**
* This exception is thrown when a user fails to propperly log into
* the application.
*/
public class SigninFailedException extends EStoreEventException  {

    public SigninFailedException (String str) {
        super(str);
    }
}
