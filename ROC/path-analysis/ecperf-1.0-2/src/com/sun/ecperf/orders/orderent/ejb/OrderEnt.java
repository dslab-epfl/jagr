
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id:
 *
 */
package com.sun.ecperf.orders.orderent.ejb;


import javax.ejb.EJBObject;

import java.rmi.RemoteException;

import com.sun.ecperf.common.*;
import com.sun.ecperf.orders.helper.*;


/**
 * The methods in this interface are the public face of OrderBean.
 * OrderBean is an entity bean that represents a row in the orders
 * table in the Customer Domain.
 *
 */
public interface OrderEnt extends EJBObject {

    /**
     * change: Change the quantities of orderlines
     *
     * @param     quantities      array of Items & Quantities to be changed
     *
     * @throws DataIntegrityException
     * @exception         InsufficientCreditException if the customer
     *                                    has insufficient credit for the changes
     * @exception         RemoteException if there is a communications
     *                                    or system failure
     */
    public void change(ItemQuantity[] quantities)
        throws RemoteException, InsufficientCreditException,
               DataIntegrityException;

    /**
     * Get status - retreive shipDate and order line info
     *
     * @return            OrderStatus object
     *              customerId, shipDate, (itemId,itemQuantity) of each item
     *
     * @throws DataIntegrityException
     * @exception               RemoteException if there is
     *                          a communications or systems failure
     */
    public OrderStatus getStatus()
        throws RemoteException, DataIntegrityException;
}

