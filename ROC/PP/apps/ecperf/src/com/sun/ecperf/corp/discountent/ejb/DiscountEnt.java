
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: DiscountEnt.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.corp.discountent.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the public interface of the Discount entity bean. It provides
 * access to the various fields of an item.
 */
public interface DiscountEnt extends EJBObject {

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
     * Method getPercent
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public double getPercent() throws RemoteException;

    /**
     * Method setPercent
     *
     *
     * @param percent
     *
     * @throws RemoteException
     *
     */
    public void setPercent(double percent) throws RemoteException;
}

