/*
 * $Id: ShoppingClientController.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstore.control.ejb;

import java.util.Collection;
import java.rmi.RemoteException;

import javax.ejb.EJBObject;
import javax.ejb.FinderException;
import javax.ejb.CreateException;

import com.sun.j2ee.blueprints.shoppingcart.cart.ejb.ShoppingCart;
import com.sun.j2ee.blueprints.personalization.profilemgr.ejb.ProfileMgr;
import com.sun.j2ee.blueprints.customer.customer.ejb.Customer;
import com.sun.j2ee.blueprints.petstore.control.event.EStoreEvent;

import com.sun.j2ee.blueprints.petstore.control.exceptions.EStoreAppException;
import com.sun.j2ee.blueprints.petstore.control.exceptions.EStoreEventException;

/**
 * This is the EJB-tier controller of the MVC.
 * It is implemented as a session EJB. It controls all the activities
 * that happen in a client session.
 * It also provides mechanisms to access other session EJBs.
 */
public interface ShoppingClientController extends EJBObject {

    /** @return the shopping cart session bean for this user. */
    public ShoppingCart getShoppingCart() throws EStoreAppException,
                                                 RemoteException;

    /** @return the Customer entity bean for this user. */
    public Customer getCustomer() throws EStoreAppException,
                                         RemoteException;

    /** @return the profilemgr entity bean for this user. */
    public ProfileMgr getProfileMgr() throws EStoreAppException, RemoteException;

    /**
     * @return the order ID corresponding to the request id
     * which is mapped to an order id in the State Machine.
     */
    public int getOrder(int requestId) throws RemoteException;

    /**
     * Feeds the specified event to the state machine of the business logic.
     * @return a list of models that got updated because of the
     * processing of this event. */
    public Collection handleEvent(EStoreEvent ese)
        throws RemoteException, EStoreEventException;
}
