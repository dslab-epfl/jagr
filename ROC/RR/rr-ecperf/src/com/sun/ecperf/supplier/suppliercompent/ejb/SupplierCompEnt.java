
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: SupplierCompEnt.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.supplier.suppliercompent.ejb;


//Import statements
import javax.ejb.*;

import java.rmi.*;


/**
 * This is the Remote Interface for the Supplier Componect entity bean.
 * This Bean enables lookup of parts that particular suppliers supply.
 *
 *
 * @author Damian Guy
 */
public interface SupplierCompEnt extends EJBObject {

    /**
     * getPrice
     * @return double - price of component.
     */
    public double getPrice() throws RemoteException;

    /** getDiscount
     * @return double - discount percent.
     */
    public double getDiscount() throws RemoteException;

    /**
     * getDeliveryDate
     * @return int
     */
    public int getDeliveryDate() throws RemoteException;

    /**
     * getQuantity
     * @return int
     */
    public int getQuantity() throws RemoteException;

    /**
     * getDiscountedPrice
     * return double - cost of parts with discount applied.
     */
    public double getDiscountedPrice() throws RemoteException;
}

