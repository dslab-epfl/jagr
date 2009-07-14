
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrderCustomerSesEJB.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.orders.ordercustomerses.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.orders.helper.*;
import com.sun.ecperf.orders.ordercustomerent.ejb.*;
import com.sun.ecperf.common.*;


/**
 * The OrderCustomerSessionBean is a wrapper for the OrderCustomer entity bean.
 * The session bean is what is accessed by the OrderEntry application.
 */
public class OrderCustomerSesEJB implements SessionBean {

    private SessionContext       sessionContext;
    private OrderCustomerEntHome orderCustomerHome;
    private OrderCustomerEnt     customer;

    /**
     * Method ejbCreate
     *
     *
     */
    public void ejbCreate() {}

    /**
     * valdiateCustomer: Check whether a given cId is valid
     * @param customerId   - Id of customer
     * @exception DataIntegrityException- if customer is not valid
     */
    public void validateCustomer(int customerId)
            throws DataIntegrityException, RemoteException {

        try {
            orderCustomerHome.findByPrimaryKey(new Integer(customerId));
        } catch (FinderException oe) {
            throw new DataIntegrityException("Customer not found: "
                                             + customerId + oe);
        }
    }

    /**
     * addCustomer: Add a new customer to the Orders database
     *
     * @param info
     * @return cId of customer added
     *
     * @throws DataIntegrityException
     * @exception InvalidInfoException - if customer data is invalid
     */
    public int addCustomer(CustomerInfo info)
            throws InvalidInfoException, DataIntegrityException,
                   RemoteException {

        try {
            OrderCustomerEnt ordCust = orderCustomerHome.create(info);

            return ((Integer) ordCust.getPrimaryKey()).intValue();
        } catch (CreateException ce) {
            throw new EJBException(ce);
        }
    }

    /**
     * getPercentDiscount: Calculate discount for specified customer
     * @param customerId   - Id of customer
     * @param total   - Pre-discount amount of the order
     * @return The percent discount off the pre-discount amount
     */
    public double getPercentDiscount(int customerId, double total)
            throws DataIntegrityException, RemoteException {

        try {
            customer =
                orderCustomerHome.findByPrimaryKey(new Integer(customerId));
        } catch (FinderException oe) {
            throw new DataIntegrityException("Customer not found: "
                                             + customerId + oe);
        }

        return customer.getPercentDiscount(total);
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

            // the homes are available via EJB links
            Context context = new InitialContext();

            orderCustomerHome =
                (OrderCustomerEntHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/OrderCustomerEnt"),
                    OrderCustomerEntHome.class);
        } catch (NamingException e) {
            throw new EJBException("Failure looking up OrderOustomerEnt home "
                                   + e);
        }
    }
}

