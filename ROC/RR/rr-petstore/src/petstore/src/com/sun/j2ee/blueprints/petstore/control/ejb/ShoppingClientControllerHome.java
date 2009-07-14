/*
 * $Id: ShoppingClientControllerHome.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstore.control.ejb;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

/**
 * The Home interface for ShoppingSessionController EJB
 */
public interface ShoppingClientControllerHome extends EJBHome {
    public ShoppingClientController create()
        throws RemoteException, CreateException;
}
