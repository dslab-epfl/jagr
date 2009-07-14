/*
 * $Id: EJBUtil.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.customer.util;

import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ejb.CreateException;


import com.sun.j2ee.blueprints.customer.account.ejb.AccountHome;
import com.sun.j2ee.blueprints.customer.order.ejb.OrderHome;

/**
 * This is a utility class for obtaining EJB references.
 */
public final class EJBUtil {

    public static AccountHome getAccountHome() throws NamingException {
            InitialContext initial = new InitialContext();
            Object objref = initial.lookup(JNDINames.ACCOUNT_EJBHOME);
            return (AccountHome)
                PortableRemoteObject.narrow(objref, AccountHome.class);
    }

    public static OrderHome getOrderHome() throws  NamingException  {
            InitialContext initial = new InitialContext();
            Object objref = initial.lookup(JNDINames.ORDER_EJBHOME);
            return (OrderHome) PortableRemoteObject.narrow(objref, OrderHome.class);
    }
}
