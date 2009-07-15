
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id:
 *
 */
package com.sun.ecperf.supplier.buyerses.ejb;


import javax.ejb.*;

import java.rmi.*;

import com.sun.ecperf.common.*;


/**
 * This is the interface of the BuyerSessionBean. It is the
 * interface from the Manufacturing Domain to the Supplier Domain.
 * The Mfg domain repeatedly calls the add method to add components
 * that are low in inventory.  When all components have been added
 * the Mfg domain calls purchase to issue the Purchase Order.
 *
 * Working on method getPOStatus to see how we can utilize it in Mfg.
 *
 * @author Damian Guy
 *
 */
public interface BuyerSes extends EJBObject {

    /**
     * add: Add components that are low in inventory to PO.
     * @param componentID  - Id of component that is being added to PO.
     * @param qtyRequired - number to be purchased.
     * @exception RemoteException - if there is a system failure.
     */
    public void add(String componentID, int qtyRequired)
        throws RemoteException;

    /**
     * purchase: Issue the Purchase Order.
     * @throws ECperfException
     * @exception RemoteException - if there is a system failure.
     * @exception CreateException - if creation of Purchase Order fails.
     * @exception FinderException
     */
    public void purchase()
        throws RemoteException, FinderException, CreateException,
               ECperfException;

    /**
     * getPOStatus: Retrieve status information of outstanding PO.
     * @param poID - Id of the Purchase Order.
     * @return POStatus - Object containg PO Status information.
     * @exception RemoteException - if there is a system failure.
     */

    //      public POStatus getPOStatus(int poId)
    //              throws RemoteException;
}

