
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * akmits@eng.sun.com 03/20/2000
 *
 * $Id: ComponentEnt.java,v 1.1.1.1 2002/11/16 05:35:27 emrek Exp $
 */
package com.sun.ecperf.mfg.componentent.ejb;


import javax.ejb.EJBObject;

import java.rmi.RemoteException;

import com.sun.ecperf.common.*;


/**
 * The methods in this interface are the public face of ComponentBean.
 * ComponentBean is an entity bean that represents a row in the parts
 * table in the Corp Domain.
 *
 * @author Ajay Mittal
 *
 *
 */
public interface ComponentEnt extends EJBObject {

    /**
      * Method to add components of this type to the inventory
      * @param numComponents number of components to be added
      * @exception RemoteException if there is a system failure
      */
    public void addInventory(int numComponents) throws RemoteException;

    /**
      * Method to add components of this type to the inventory
      * and deduct the quantity on order at the same time.
      * @param numComponents number of components to be added
      * @exception RemoteException if there is a system failure
      */
    public void deliver(int numComponents) throws RemoteException;

    /**
      * Method to take components of this type from the inventory
      * @param numComponents number of components to be taken
      * @exception RemoteException if there is a system failure
      */
    public void takeInventory(int numComponents) throws RemoteException;

    /**
     * Method addOrderedInventory
     *
     *
     * @param numComponents
     *
     * @throws RemoteException
     *
     */
    public void addOrderedInventory(int numComponents) throws RemoteException;

    /**
     * Method getQtyRequired
     *
     * @param currentOrder Additional qty to be taken by current workorder
     * @return The quantity required to order
     *
     * @throws RemoteException
     *
     */
    public int getQtyRequired(int currentOrder) throws RemoteException;

    /**
     * Method getId
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public String getId() throws RemoteException;

    /**
     * Method getName
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public String getName() throws RemoteException;

    /**
     * Method getDescription
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public String getDescription() throws RemoteException;

    /**
     * Method getRevision
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public String getRevision() throws RemoteException;

    /**
     * Method getPlanner
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getPlanner() throws RemoteException;

    /**
     * Method getType
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getType() throws RemoteException;

    /**
     * Method getLomark
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getLomark() throws RemoteException;

    /**
     * Method getHimark
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getHimark() throws RemoteException;

    /**
     * Method getPurchased
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getPurchased() throws RemoteException;
}

