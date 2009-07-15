
/*
 *
 * Copyright (c) 1999-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.largeorderses.ejb;


import javax.ejb.EJBObject;

import java.rmi.RemoteException;


/**
 * This interface is the remote interface for the Large Order
 * session bean. This bean is stateless.
 * @author Agnes Jacob
 */
public interface LargeOrderSes extends EJBObject {

    /**
     * Method createLargeOrder
     *
     *
     * @param orderId
     * @param oLineId
     * @param assemblyId
     * @param qty
     * @param dueDate
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public Integer createLargeOrder(
        int orderId, int oLineId, String assemblyId, short qty,
            java.sql.Date dueDate) throws RemoteException;

    /**
     * Method findLargeOrders
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public java.util.Vector findLargeOrders() throws RemoteException;
}

