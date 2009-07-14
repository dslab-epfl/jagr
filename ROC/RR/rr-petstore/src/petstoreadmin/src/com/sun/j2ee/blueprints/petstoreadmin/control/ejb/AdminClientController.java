/*
 * $Id: AdminClientController.java,v 1.1.1.1 2002/10/03 21:17:37 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstoreadmin.control.ejb;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Locale;
import java.rmi.RemoteException;

import javax.ejb.EJBObject;
import javax.ejb.FinderException;

/**
 * This is the EJB-tier controller of the MVC for the admin functionality.
 * It is implemented as a session EJB. It controls all the activities
 * that happen in a client session.
 * It also provides mechanisms to access other session EJBs.
 */
public interface AdminClientController extends EJBObject {

    public Collection getPendingOrders(Locale l) throws RemoteException;
    public void setOrdersStatus(ArrayList ordersList) throws RemoteException;
}
