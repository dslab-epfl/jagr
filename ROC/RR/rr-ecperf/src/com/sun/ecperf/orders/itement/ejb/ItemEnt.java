
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: ItemEnt.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.orders.itement.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the public interface of the Item entity bean. It provides
 * access to the various fields of an item.
 */
public interface ItemEnt extends EJBObject {

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
     * Method getPrice
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public double getPrice() throws RemoteException;

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
     * Method getDiscount
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public float getDiscount() throws RemoteException;
}

