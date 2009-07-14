/*
 * $Id: InventoryDAODBUpdateException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.inventory.exceptions;

/**
 * InventoryDAODBUpdateException is an exception that extends the
 * InventoryDAOAppException. This is thrown by the DAOs of the inventory
 * component when there is some failure while writing/updating the database
 */
public class InventoryDAODBUpdateException extends InventoryDAOAppException {

    /**
     * Constructor
     * @param str    a string that explains what the exception condition is
     */
    public InventoryDAODBUpdateException (String str) {
        super(str);
    }

    /**
     * Default constructor. Takes no arguments
     */
    public InventoryDAODBUpdateException () {
        super();
    }

}

