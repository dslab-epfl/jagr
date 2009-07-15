
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: OrderSes.java,v 1.1 2004/02/19 14:45:07 emrek Exp $
 *
 */
package com.sun.ecperf.orders.orderses.ejb;


import javax.ejb.*;

import java.rmi.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.orders.helper.*;


/**
 * This is the interface of the OrderSessionBean, which is a wrapper
 * for the Order and Orderline entity beans. The session bean also
 * implements the getCustStatus method to retrieve all orders
 * belonging to a particular customer
 */
public interface OrderSes extends EJBObject {

    /**
     * newOrder: Enter an order for a customer
     * @param customerId   - Id of customer
     * @param quantities
     * @return int - Id of order
     * @throws DataIntegrityException
     * @exception InsufficientCreditException - if customer credit check fails
     * @exception CreateException - if creation of order fails
     * @exception RemoteException  - if there is a system failure
     */
    public int newOrder(int customerId, ItemQuantity[] quantities)
        throws InsufficientCreditException, DataIntegrityException,
               RemoteException, CreateException;

    /**
     * changeOrder: Changes an existing customer order
     * @param orderId   - Id of order being changed
     * @param quantities
     * @exception InsufficientCreditException - if customer credit check fails
     * @exception RemoteException  - if there is a system failure
     */
    public void changeOrder(int orderId, ItemQuantity[] quantities)
        throws InsufficientCreditException, RemoteException;

    /**
     * cancelOrder: Cancel an existing customer order
     * @param orderId   - Id of order being changed
     * @exception RemoteException  - if there is a system failure
     */
    public void cancelOrder(int orderId) throws RemoteException;

    /**
     * getOrderStatus: Retrieves status of an order
     * @param orderId   - Id of order
     * @return OrderStatus object
     *
     * @throws DataIntegrityException
     * @exception RemoteException  - if there is a system failure
     */
    public OrderStatus getOrderStatus(int orderId)
        throws DataIntegrityException, RemoteException;

    /**
     * Get status of all orders of a Customer
     *
     * @param customerId      int customer id
     * @return          Array of CustomerStatus objects (one for each order)
     *
     * @throws DataIntegrityException
     * @exception RemoteException  - if there is a system failure
     */
    public CustomerStatus[] getCustomerStatus(int customerId)
        throws DataIntegrityException, RemoteException;
}

