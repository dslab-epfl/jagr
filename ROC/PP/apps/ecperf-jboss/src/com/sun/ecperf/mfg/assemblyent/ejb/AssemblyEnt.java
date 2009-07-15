
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * akmits@eng.sun.com 03/20/2000
 *
 * $Id: AssemblyEnt.java,v 1.1 2004/02/19 14:45:08 emrek Exp $
 */
package com.sun.ecperf.mfg.assemblyent.ejb;


import javax.ejb.EJBObject;

import java.rmi.RemoteException;

import com.sun.ecperf.common.*;


/**
 * The methods in this interface are the public face of AssemblyBean.
 * AssemblyBean is an entity bean that represents a row in the parts
 * table in the Corp Domain.
 *
 * @author Ajay Mittal
 *
 *
 */
public interface AssemblyEnt extends EJBObject {

    /**
      * Method to get BOMs for this type of the Assembly
     *
     * @return
      * @exception RemoteException if there is a system failure
      */
    public java.util.Enumeration getBoms() throws RemoteException;

    /**
     * Method addInventory
     *
     *
     * @param numComponents
     *
     * @throws RemoteException
     *
     */
    public void addInventory(int numComponents) throws RemoteException;

    /**
     * Method deliver
     *
     *
     * @param numComponents
     *
     * @throws RemoteException
     *
     */
    public void deliver(int numComponents) throws RemoteException;

    /**
     * Method takeInventory
     *
     *
     * @param numComponents
     *
     * @throws RemoteException
     *
     */
    public void takeInventory(int numComponents) throws RemoteException;

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

