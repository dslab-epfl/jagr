
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: OrderCustomerSes.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.orders.ordercustomerses.ejb;


import javax.ejb.*;

import java.rmi.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.orders.helper.*;


/**
 * The OrderCustomerSession bean is a session bean whose interface
 * is exposed to the client.
 * It uses the underlying OrderCustomer entity bean to do its work.
 */
public interface OrderCustomerSes extends javax.ejb.EJBObject {

    /**
     * This method checks for the existence of a customer with
     * the specified id.
     */
    public void validateCustomer(int id)
        throws DataIntegrityException, RemoteException;

    /**
     * This method adds a new customer with the specified info
     *
     * @param info
     * @return id - Customer id of newly created customer
     */
    public int addCustomer(CustomerInfo info)
        throws InvalidInfoException, DataIntegrityException, RemoteException;

    /**
     * Method getPercentDiscount
     *
     *
     * @param customerId
     * @param total
     *
     * @return
     *
     * @throws DataIntegrityException
     * @throws RemoteException
     *
     */
    public double getPercentDiscount(int customerId, double total)
        throws DataIntegrityException, RemoteException;
}

