
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.workorderent.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the public interface of the WorkOrder entity bean. It provides
 * the methods available for this object.
 *
 * @author Agnes Jacob
 *
 */
public interface WorkOrderEnt extends EJBObject {

    /**
     * Method process
     *
     *
     * @throws RemoteException
     *
     */
    public void process() throws RemoteException;

    /**
     * Method update
     *
     *
     * @throws RemoteException
     *
     */
    public void update() throws RemoteException;

    /**
     * Method finish
     *
     *
     * @throws RemoteException
     *
     */
    public void finish() throws RemoteException;

    /**
     * Method cancel
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public boolean cancel() throws RemoteException;

    /**
     * Method getQtyToOrder
     *
     *
     * @param comp_id
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getQtyToOrder(String comp_id) throws RemoteException;

    /**
     * Method getComponentDemand
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public java.util.Vector getComponentDemand() throws RemoteException;

    /**
     * Get methods for the instance variables
     */
    public Integer getId() throws RemoteException;

    /**
     * Method getSalesId
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getSalesId() throws RemoteException;

    /**
     * Method getOLineId
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getOLineId() throws RemoteException;

    /**
     * Method getStatus
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getStatus() throws RemoteException;

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
     * Method getOrigQty
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getOrigQty() throws RemoteException;

    /**
     * Method getCompQty
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getCompQty() throws RemoteException;

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
     * Method getStartDate
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public java.sql.Date getStartDate() throws RemoteException;
}

