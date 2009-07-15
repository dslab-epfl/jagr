
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.inventoryent.ejb;


import javax.ejb.*;

import java.rmi.*;

import java.sql.Date;


/**
 * This is the home interface of the InventoryEnt entity bean in the Mfg
 * domain.
 *
 * @author Agnes Jacob
 *
 */
public interface InventoryEntHome extends EJBHome {

    /**
     * This method corresponds to the ejbCreate method of the bean.
     * @param partId - part number
     * @param qty - amount in inventory
     * @param in_ordered
     * @param location - warehouse/bin
     * @param accCode - Finance account code
     * @param accDate - Date/Time of last activity
     */
    public InventoryEnt create(
        String partId, int qty, int in_ordered, String location, int accCode,
            java.sql.Date accDate) throws RemoteException, CreateException;

    /**
     * Method findByPrimaryKey
     *
     *
     * @param pk
     *
     * @return
     *
     * @throws FinderException
     * @throws RemoteException
     *
     */
    public InventoryEnt findByPrimaryKey(String pk)
        throws RemoteException, FinderException;

    /**
     * Method findAll
     *
     *
     * @return
     *
     * @throws FinderException
     * @throws RemoteException
     *
     */
    public java.util.Enumeration findAll()
        throws RemoteException, FinderException;
}

