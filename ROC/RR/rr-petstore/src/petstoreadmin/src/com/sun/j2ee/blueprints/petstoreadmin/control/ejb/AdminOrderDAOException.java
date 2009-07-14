/*
 * $Id: AdminOrderDAOException.java,v 1.1.1.1 2002/10/03 21:17:37 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstoreadmin.control.ejb;

public class AdminOrderDAOException extends Exception {

    public AdminOrderDAOException (String str) {
        super(str);
    }

    public AdminOrderDAOException () {
        super();
    }
}

