/*
 * $Id: EStoreEventException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstore.control.exceptions;

/**
 * This exception is the base class for all the event exceptions.
 */
public class EStoreEventException extends Exception
    implements java.io.Serializable {

    public EStoreEventException() {}

    public EStoreEventException(String str) {
        super(str);
    }
}
