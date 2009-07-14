/*
 * $Id: EJBUtil.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.shoppingcart.util;

import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ejb.CreateException;

import com.sun.j2ee.blueprints.shoppingcart.catalog.ejb.CatalogHome;

/**
 * This is a utility class for obtaining EJB references.
 */
public final class EJBUtil {


    public static CatalogHome getCatalogHome() throws Exception{
        try {
            InitialContext initial = new InitialContext();
            Object objref = initial.lookup(JNDINames.CATALOG_EJBHOME);
            return (CatalogHome)
                PortableRemoteObject.narrow(objref, CatalogHome.class);
        } catch (NamingException ne) {
            throw new Exception(ne.getMessage());
        }
    }


}
