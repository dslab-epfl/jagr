/*
 * $Id: DuplicateAccountException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstore.control.exceptions;


/** Duplicate Account Exception
  * Signifys to app that someone tried to create
  * an account where the userid has already been used.
 */
public class DuplicateAccountException extends EStoreEventException
                           {

    public DuplicateAccountException (String str) {
        super(str);
    }
}

