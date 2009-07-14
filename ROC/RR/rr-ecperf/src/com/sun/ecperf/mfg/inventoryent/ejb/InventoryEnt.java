
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.inventoryent.ejb;


import javax.ejb.*;

import java.rmi.*;

import java.sql.Date;


/**
 * This is the public interface of the InventoryEnt entity bean.
 *
 * @author Agnes Jacob
 *
 */
public interface InventoryEnt extends EJBObject {

    /* For take and add, either the return value is boolean or
     * an exception is thrown for failure
     */

    /**
     * Method add
     *
     *
     * @param qty
     *
     * @throws RemoteException
     *
     */
    public void add(int qty) throws RemoteException;

    /**
     * Method take
     *
     *
     * @param qty
     *
     * @throws RemoteException
     *
     */
    public void take(int qty) throws RemoteException;

    /**
     * Method getOnHand
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getOnHand() throws RemoteException;

    /**
     * Method getOrdered
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getOrdered() throws RemoteException;

    /**
     * Method addOrdered
     *
     *
     * @param qty
     *
     * @throws RemoteException
     *
     */
    public void addOrdered(int qty) throws RemoteException;

    /**
     * Method takeOrdered
     *
     *
     * @param qty
     *
     * @throws RemoteException
     *
     */
    public void takeOrdered(int qty) throws RemoteException;

    /**
     * get methods for the instance variables
     */
    public String getPartId() throws RemoteException;

    /**
     * Method getQty
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getQty() throws RemoteException;

    /**
     * Method getLocation
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public String getLocation() throws RemoteException;

    /**
     * Method getAccCode
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getAccCode() throws RemoteException;

    /**
     * Method getAccDate
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public Date getAccDate() throws RemoteException;
}

