/*
 * $Id: InventoryHome.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.inventory.ejb;


import java.rmi.RemoteException;
import javax.ejb.FinderException;
import javax.ejb.EJBHome;

/**
 * The home interface of the Inventory Entity EJB.
 */

public interface InventoryHome extends EJBHome {

    public Inventory findByPrimaryKey (String itemId)
        throws RemoteException, FinderException;
}
