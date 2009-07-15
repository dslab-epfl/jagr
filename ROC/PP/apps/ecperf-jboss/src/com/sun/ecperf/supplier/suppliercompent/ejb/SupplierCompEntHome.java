
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: SupplierCompEntHome.java,v 1.1 2004/02/19 14:45:11 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.suppliercompent.ejb;


//Import statements
import javax.ejb.*;

import java.rmi.*;

import java.util.*;


/**
 * This is the Home interface for the Supplier Component Entity Bean.
 *
 *
 * @author Damian Guy
 */
public interface SupplierCompEntHome extends EJBHome {

    /**
     * create
     * @param suppCompID - part number.
     * @param suppCompSuppID - supplier id.
     * @param suppCompPrice - price of supplied qty (suppCompQty).
     * @param suppCompQty - quantity that is supplied.
     * @param suppCompDiscount - discount the applies.
     * @param suppCompDelDate - probably should be lead time.
     * @return SuppCompEntPK - primary Key for this object (suppCompID + suppCompSuppID).
     * @exception RemoteException - if there is a system failure.
     * @exception CreateException - if there is a create failure.
     */
    public SupplierCompEnt create(
        String suppCompID, int suppCompSuppID, double suppCompPrice, int suppCompQty,
            double suppCompDiscount, int suppCompDelDate)
                throws RemoteException, CreateException;

    /**
     * findByPrimaryKey
     * @retrun SupplierCompEnt
     *
     * @param key
     *
     * @return
     *
     * @throws FinderException
     * @exception RemoteException
     */
    public SupplierCompEnt findByPrimaryKey(SuppCompEntPK key)
        throws RemoteException, FinderException;

    /**
     * findAllBySupplier: find all components for supplier.
     * @param suppID - id of supplier.
     * @return Enumeration
     * @exception RemoteException - if there is a system failure.
     * @exception FinderException - if there are not any rows found.
     */
    public Enumeration findAllBySupplier(int suppID)
        throws RemoteException, FinderException;
}

