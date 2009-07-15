
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * akmits@eng.sun.com 04/04/2000
 *
 */
package com.sun.ecperf.mfg.largeorderent.ejb;


import javax.ejb.EJBObject;

import java.rmi.RemoteException;

import com.sun.ecperf.mfg.helper.*;
import com.sun.ecperf.common.*;


/**
 * The methods in this interface are the public face of LargeOrderBean.
 * LargeOrderBean is an entity bean that represents a row in the large order
 * table in the Mfg Domain.
 *
 * @author Ajay Mittal
 *
 *
 */
public interface LargeOrderEnt extends EJBObject {

    /* Methods to get all the bean attributes */

    /**
     * Method getId
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public Integer getId() throws RemoteException;

    /**
     * Method getSalesOrderId
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getSalesOrderId() throws RemoteException;

    /**
     * Method getOrderLineNumber
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getOrderLineNumber() throws RemoteException;

    /**
     * Method getAssemblyId
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public String getAssemblyId() throws RemoteException;

    /**
     * Method getQty
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public short getQty() throws RemoteException;

    /**
     * Method getDueDate
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public java.sql.Date getDueDate() throws RemoteException;

    /**
     * Method getLargeOrderInfo
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public LargeOrderInfo getLargeOrderInfo() throws RemoteException;
}

