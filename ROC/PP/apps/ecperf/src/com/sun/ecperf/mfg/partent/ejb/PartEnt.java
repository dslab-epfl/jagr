
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * akmits@eng.sun.com 03/16/2000
 *
 * $Id: PartEnt.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 */
package com.sun.ecperf.mfg.partent.ejb;


import javax.ejb.EJBObject;

import java.rmi.RemoteException;

import com.sun.ecperf.common.*;


/**
 * The methods in this interface are the public face of PartBean.
 * PartBean is an entity bean that represents a row in the parts
 * table in the Corp Domain.
 *
 * @author Ajay Mittal
 *
 *
 */
public interface PartEnt extends EJBObject {

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
     * Method getPurchased
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getPurchased() throws RemoteException;

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
}

