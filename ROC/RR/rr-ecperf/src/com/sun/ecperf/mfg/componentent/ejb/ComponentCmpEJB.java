
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * akmits@eng.sun.com 03/16/2000
 *
 * $Id: ComponentCmpEJB.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 */
package com.sun.ecperf.mfg.componentent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.mfg.partent.ejb.*;
import com.sun.ecperf.mfg.inventoryent.ejb.*;


/**
 * This class implements the Component entity bean. It is
 * responsible for performing all the transactions on the Components
 *
 * @author Ajay Mittal
 *
 */
public class ComponentCmpEJB extends PartCmpEJB {

    private InventoryEntHome inventoryEntHome;
    private InventoryEnt inventory = null; // Cached reference.


    /**
     * Method getInventory() provides the matching inventory object.
     * If it's never found, it will be searched. Otherwise
     * the cached one will be provided.
     * @return The inventory reference of matching inventory
     * @exception ECperfException If database does not have the record
     */
    protected InventoryEnt getInventory()
            throws FinderException, RemoteException {
        if (inventory == null)
            inventory = inventoryEntHome.findByPrimaryKey(id);
        return inventory;
    }

    /**
     * Resets the inventory reference cache.
     */
    public void ejbActivate() {
        super.ejbActivate();
        inventory = null;
    }

    /**
     * Method setEntityContext
     *
     *
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        super.setEntityContext(entityContext);
	if (debugging)
	    debug.println(3, "compo:setEntityContext");

        // we need to get inventoryEntHome home object
        try {
            Context context = new InitialContext();

            inventoryEntHome =
                (InventoryEntHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/InventoryEnt"),
                    InventoryEntHome.class);

	    if (debugging)
		debug.println(
                3, "setEntityContext: found InventoryEntHome interface");
        } catch (NamingException e) {
            throw new EJBException("Failure looking up home " + e);
        }
    }

    /**
      * Method to add components of this type to the inventory
      * @param numComponents number of components to be added
      */
    public void addInventory(int numComponents) {

	if (debugging)
	    debug.println(3, "addInventory");

        InventoryEnt inventoryEnt;

        try {

            getInventory().add(numComponents);

        } catch (FinderException e) {
            try {
                inventoryEntHome.create(id, numComponents, 0, "location",
                                        1234,
                                        new java.sql
                                            .Date((new java.util.Date())
                                                .getTime()));
            } catch (CreateException ce) {
                throw new EJBException(ce);
            } catch (RemoteException re) {
                debug.printStackTrace(re);
                throw new EJBException(re);
            }
        } catch (RemoteException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        }
    }

    /**
      * Method to add components of this type to the inventory
      * and deduct the quantity on order at the same time.
      * @param numComponents number of components to be added
      */
    public void deliver(int numComponents) {

	if (debugging)
	    debug.println(3, "deliver");

        try {
            InventoryEnt inventoryEnt = getInventory();
            inventoryEnt.add(numComponents);
            inventoryEnt.takeOrdered(numComponents);

        } catch (FinderException e) {
            try {
                inventoryEntHome.create(id, numComponents, 0, "location",
                                        1234,
                                        new java.sql
                                            .Date((new java.util.Date())
                                                .getTime()));
            } catch (CreateException ce) {
                throw new EJBException(ce);
            } catch (RemoteException re) {
                debug.printStackTrace(re);
                throw new EJBException(re);
            }
        } catch (RemoteException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        }
    }

    /**
      * Method to take components of this type from the inventory
      * @param numComponents number of components to be taken
      */
    public void takeInventory(int numComponents) {

	if (debugging)
	    debug.println(3, "takeInventory");

        InventoryEnt inventoryEnt;

        try {
            getInventory().take(numComponents);
        } catch (FinderException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } catch (RemoteException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        }
    }

    /**
     * Method to add the number of components that have been ordered to the inventory
     *
     * @param numComponents
     */
    public void addOrderedInventory(int numComponents) {

        InventoryEnt inventoryEnt;

        try {
            getInventory().addOrdered(numComponents);
        } catch (FinderException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } catch (RemoteException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        }
    }

    /* ?ajay? not needed
      * Method to find number of components available in the inventory
      * @return int
      * @exception RemoteException if there is a system failure
      */

    //  public int getOnHand() throws RemoteException;

    /**
     * Method to work out the number of parts required.
     * Takes into account the number on order + the number on hand
     * @param currentOrder Additional needs for the current workorder
     * @return int - number to order.
     */
    public int getQtyRequired(int currentOrder) {

	if (debugging)
	    debug.println(3, "getQtyRequired");

        try {
            InventoryEnt inventoryEnt = getInventory();

            int numOnHand  = inventoryEnt.getOnHand();
            int numOrdered = inventoryEnt.getOrdered();
            int numAvail   = numOnHand + numOrdered - currentOrder;

            if (numAvail <= lomark) {
                return himark - numAvail;
            } else {
                return 0;
            }
        } catch (FinderException fe) {
	    if (debugging)
		debug.println(2, fe.getMessage());

            throw new EJBException(fe);
        } catch (RemoteException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        }
    }
}

