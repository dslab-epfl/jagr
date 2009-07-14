
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id:
 *
 */
package com.sun.ecperf.orders.cartses.ejb;


import javax.ejb.*;

import java.rmi.*;

import java.util.Vector;

import com.sun.ecperf.orders.helper.ItemQuantity;
import com.sun.ecperf.orders.helper.InsufficientCreditException;


/**
 * This is the interface of the CartSessionBean. It is the
 * bean used by a GUI client or a OrderEntry driver to create a new order.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public interface CartSes extends EJBObject {

    /**
     * Method add - Add items to the cart
     * @param item_qtyIt has item id and quantity to be added.
     * @exception RemoteException If there is a system failure.
     */
    public void add(ItemQuantity item_qty) throws RemoteException;

    /**
     * Method delete - Delete items from the cart
     * @param item_id Item id of the item to be deleted
     * @exception RemoteException If there is a system failure.
     */
    public void delete(String item_id) throws RemoteException;

    /**
     * Method deleteAll - Delete all items from the cart
     * @exception RemoteException If there is a system failure.
     */
    public void deleteAll() throws RemoteException;

    /**
     * Method buy - Buy the contents of the cart. This will call newOrder method
 *      of OrderSes bean.
     *
     * @return int - Order Id of the new order created.
     * @exception RemoteException If there is a system failure.
     * @exception CreateException If creation of newOrder fails.
     * @exception InsufficientCreditException If newOrder fails due to bad credit
     */
    public int buy() throws RemoteException, CreateException, InsufficientCreditException;

    /**
     * Method removeItem - Remove item to the cart. This is used only by GUI client
     * @param item_qty It has item id and quantity to be removed.
     * @exception RemoteException If there is a system failure.
     */
    public void removeItem(ItemQuantity item_qty) throws RemoteException;

    /**
     * Method getItemsList - Get the list of items in the cart. This is used only
 *                                  by GUI client
     *
     * @return Vector - List of all items
     * @exception RemoteException If there is a system failure.
     */
    public Vector getItemsList() throws RemoteException;

    /**
     * Method setCustId - Set Customer Id. This is used only by GUI client
     * @param cust_id Id of the customer
     * @exception RemoteException If there is a system failure.
     */
    public void setCustId(int cust_id) throws RemoteException;
}

