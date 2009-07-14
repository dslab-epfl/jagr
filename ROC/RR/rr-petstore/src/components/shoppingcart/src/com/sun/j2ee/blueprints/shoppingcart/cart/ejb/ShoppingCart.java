
/** $Id: ShoppingCart.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.shoppingcart.cart.ejb;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Locale;
import javax.ejb.EJBObject;

import com.sun.j2ee.blueprints.shoppingcart.cart.model.ShoppingCartModel;

/**
 * This interface provides methods to add an item to the
 * shoppingcart, delete an item from the shopping cart,
 * and update item quantities in the shopping cart.
 */
public interface ShoppingCart extends EJBObject {

    /**
     *  get a list of items & their qty in the cart
     *  @return the model data as read-only.
     */
    public ShoppingCartModel getDetails(Locale locale) throws RemoteException;

    //
    // Methods to update the state of shopping cart.
    //

    public void addItem(String itemNo) throws RemoteException;

    public void addItem(String itemNo, int qty) throws RemoteException;

    public void deleteItem(String itemNo) throws RemoteException;

    public void updateItemQty(String itemNo, int newQty)
        throws RemoteException;

    public void empty() throws RemoteException;
}
