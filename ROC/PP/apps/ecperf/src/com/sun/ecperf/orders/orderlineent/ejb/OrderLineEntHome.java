
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrderLineEntHome.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.orders.orderlineent.ejb;


import javax.ejb.*;

import java.rmi.*;

import java.util.*;


/**
 * This is the home interface of the OrderLine bean
 */
public interface OrderLineEntHome extends EJBHome {

    /**
     * This method corresponds to the EJBCreate method in the bean.
     * @param id - Id of orderline
     * @param orderId - Id of order to which this belongs
     * @param itemId - Id of item being ordered
     * @param quantity - Quantity of item
     * @param shipDate - Required ship date for this orderline
     * @return OrderLine
     * @exception CreateException - if the create fails
     * @exception RemoteException - if there is a system failure
     */
    OrderLineEnt create(int id, int orderId, String itemId, int quantity, java
        .sql.Date shipDate) throws RemoteException, CreateException;

    OrderLineEnt findByPrimaryKey(OrderLineEntPK pk)
        throws RemoteException, FinderException;

    /**
     * Find all orderlines for a particular order
     * @param orderId - id of order for which orderlines are required
     * @return Enumeration of orderlines
     * @exception FinderException - if the find fails
     * @exception RemoteException - if there is a system failure
     */
    Enumeration findByOrder(int orderId)
        throws RemoteException, FinderException;

    /**
     * Find a particular item in an order
     * @param orderId - id of order for which orderline is required
     * @param itemId - id of item in order to find
     * @return OrderLine
     * @exception FinderException - if the find fails
     * @exception RemoteException - if there is a system failure
     */
    OrderLineEnt findByOrderAndItem(int orderId, String itemId)
        throws RemoteException, FinderException;
}

