
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrderSesEJB.java,v 1.1.1.1 2002/11/16 05:35:28 emrek Exp $
 *
 */
package com.sun.ecperf.orders.orderses.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.orders.helper.*;
import com.sun.ecperf.orders.orderent.ejb.*;
import com.sun.ecperf.common.*;


/**
 * The OrderSessionBean is a wrapper for the Order and Orderline entity beans.
 * The session bean is what is accessed by the OrderEntry application. This
 * bean also implements the getCustStatus method to retrieve all orders
 * belonging to a particular customer.
 */
public class OrderSesEJB implements SessionBean {

    private SessionContext sessionContext;
    private OrderEntHome   orderHome;
    protected Debug        debug;
    protected boolean      debugging;

    /**
     * Method ejbCreate
     *
     *
     */
    public void ejbCreate() {
        if(debugging)
            debug.println(3, "ejbCreate ");
    }

    /**
     * newOrder: Enter an order for a customer
     * @param customerId   - Id of customer
     * @param quantities
     * @return int - Id of order
     * @throws DataIntegrityException
     * @exception InsufficientCreditException - if customer credit check fails
     * @exception CreateException - if creation of order fails
     */
    public int newOrder(int customerId, ItemQuantity[] quantities)
            throws InsufficientCreditException, DataIntegrityException,
                   CreateException, RemoteException {

        if(debugging)
            debug.println(3, "newOrder ");

        OrderEnt order = orderHome.create(customerId, quantities);

        return ((Integer) order.getPrimaryKey()).intValue();
    }

    /**
     * changeOrder: Changes an existing customer order
     * @param orderId   - Id of order being changed
     * @param quantities
     * @exception InsufficientCreditException - if customer credit check fails
     */
    public void changeOrder(int orderId, ItemQuantity[] quantities)
            throws InsufficientCreditException, RemoteException {

        if(debugging)
            debug.println(3, "changeOrder ");

        try {
            OrderEnt order = orderHome.findByPrimaryKey(new Integer(orderId));

            order.change(quantities);
        } catch (FinderException e) {
            throw new EJBException(e);
        } catch (DataIntegrityException pe) {
            throw new EJBException(pe);
        }
    }

    /**
     * cancelOrder: Cancel an existing customer order
     * @param orderId   - Id of order being changed
     *
     * @throws RemoteException
     */
    public void cancelOrder(int orderId) throws RemoteException {

        if(debugging)
            debug.println(3, "cancelOrder ");

        try {
            orderHome.remove(new Integer(orderId));
        } catch (RemoveException e) {
            throw new EJBException(e);
        }
    }

    /**
     * getOrderStatus: Retrieves status of an order
     * @param orderId   - Id of order
     * @return OrderStatus
     */
    public OrderStatus getOrderStatus(int orderId)
            throws RemoteException, DataIntegrityException {

        if(debugging)
            debug.println(3, "getOrderStatus ");

        try {
            OrderEnt order = orderHome.findByPrimaryKey(new Integer(orderId));

            return order.getStatus();
        } catch (FinderException e) {
            throw new EJBException(e);
        }
    }

    /**
     * Get status of all orders of a Customer
     *
     * @param customerId      int customer id
     * @return          Array of CustomerStatus objects (one for each order)
     */
    public CustomerStatus[] getCustomerStatus(int customerId)
            throws DataIntegrityException, RemoteException {

        if(debugging)
            debug.println(3, "getCustomerStatus ");

        try {
            Vector      customerStatusVector = new Vector();
            Enumeration orders               =
                orderHome.findByCustomer(customerId);

            while (orders.hasMoreElements()) {
                OrderEnt       order          =
                    (OrderEnt) javax.rmi.PortableRemoteObject
                        .narrow(orders.nextElement(), OrderEnt.class);
                OrderStatus    orderStatus    = order.getStatus();
                CustomerStatus customerStatus = new CustomerStatus();

                customerStatus.orderId    =
                    ((Integer) order.getPrimaryKey()).intValue();
                customerStatus.shipDate   = orderStatus.shipDate;
                customerStatus.quantities = orderStatus.quantities;

                customerStatusVector.addElement(customerStatus);

                if (customerStatusVector.size() == 20) {
                    break;
                }
            }

            return (CustomerStatus[]) customerStatusVector
                .toArray(new CustomerStatus[customerStatusVector.size()]);
        } catch (FinderException e) {
            throw new EJBException(e);
        }
    }

    /**
     * Method ejbRemove
     *
     *
     */
    public void ejbRemove() {}

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {}

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {}

    /**
     * Method setSessionContext
     *
     *
     * @param sessionContext
     *
     */
    public void setSessionContext(SessionContext sessionContext) {

        this.sessionContext = sessionContext;

        try {
            InitialContext initCtx    = new InitialContext();
            int            debugLevel =
                ((Integer) initCtx.lookup("java:comp/env/debuglevel"))
                    .intValue();

            debug = (debugLevel > 0)
                    ? new DebugPrint(debugLevel, this)
                    : new Debug();
        } catch (NamingException ne) {
            System.out.println(
                "OrderSesEJB: debuglevel Property not set. Turning off debug messages");

            debug = new Debug();
        }

        try {

            // the homes are available via EJB links
            Context context = new InitialContext();

            orderHome = (OrderEntHome) javax.rmi.PortableRemoteObject.narrow(
                context.lookup("java:comp/env/ejb/OrderEnt"),
                OrderEntHome.class);
        } catch (NamingException e) {
            throw new EJBException("Failure looking up home" + e);
        }
    }
}

