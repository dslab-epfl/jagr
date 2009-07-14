/*
 * $Id: Inventory.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.inventory.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import com.sun.j2ee.blueprints.inventory.model.InventoryModel;

/**
 * This interface provides methods to view and modify inventory
 * information for a particular item.
*/

public interface Inventory extends EJBObject {

    /**
     * @return the inventory information for this item.
     */
    public InventoryModel getDetails() throws RemoteException;

    /**
     * reduces the quantity in inventory by qty amount
     */
    public void reduceQuantity(int qty)
        throws RemoteException;

    /**
     * increases the quantity in inventory by qty amount
     */
    public void addQuantity(int qty)
        throws RemoteException;
}
