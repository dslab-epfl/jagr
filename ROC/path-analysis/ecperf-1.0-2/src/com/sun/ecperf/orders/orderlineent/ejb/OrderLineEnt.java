
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: OrderLineEnt.java,v 1.1.1.1 2002/11/16 05:35:28 emrek Exp $
 *
 */
package com.sun.ecperf.orders.orderlineent.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * The methods in this interface are the public face of OrderLineBean.
 * OrderLineBean is an entity bean that represents an orderline row
 * in the orders table of the Customer Domain.
 */
public interface OrderLineEnt extends EJBObject {

    /**
     * Method getItemId
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public String getItemId() throws RemoteException;

    /**
     * Method getQuantity
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getQuantity() throws RemoteException;

    /**
     * Method setQuantity
     *
     *
     * @param quantity
     *
     * @throws RemoteException
     *
     */
    public void setQuantity(int quantity) throws RemoteException;

    /**
     * Method getShipDate
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public java.sql.Date getShipDate() throws RemoteException;

    /**
     * Method setShipDate
     *
     *
     * @param shipDate
     *
     * @throws RemoteException
     *
     */
    public void setShipDate(java.sql.Date shipDate) throws RemoteException;
}

