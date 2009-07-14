
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.boment.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the public interface of the BOM entity bean. It provides
 * the methods available for this object.
 *
 * @author Agnes Jacob
 *
 */
public interface BomEnt extends EJBObject {

    /**
     * Get methods for the instance variables
     */
    public String getAssemblyId() throws RemoteException;

    /**
     * Method getComponentId
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public String getComponentId() throws RemoteException;

    /**
     * Method getLineNo
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getLineNo() throws RemoteException;

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
     * Method getEngChange
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public String getEngChange() throws RemoteException;

    /**
     * Method getOpsNo
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getOpsNo() throws RemoteException;

    /**
     * Method getOpsDesc
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public String getOpsDesc() throws RemoteException;
}

