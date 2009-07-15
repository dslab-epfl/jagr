
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: SupplierEntHome.java,v 1.1 2004/02/19 14:45:07 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.supplierent.ejb;


//Import statements
import javax.ejb.*;

import java.rmi.*;

import java.util.*;


/**
 * Home interface for the Supplier Entity bean.
 *
 * @author Damian Guy
 */
public interface SupplierEntHome extends EJBHome {

    /**
     * create: create a new supplier.
     * @param suppID - id of supplier.
     * @param suppName - supplier name.
     * @param suppStreet1 - street line 1.
     * @param suppStreet2 - street line 2.
     * @param suppCity - city supplier is located.
     * @param suppState
     * @param suppCountry - country supplier is located.
     * @param suppZip - zip/postal code.
     * @param suppPhone - contact phone number.
     * @param suppContact - contact person.
     * @return SupplierEnt - newly created Supplier
     * @exception RemoteException - if there is a system failure.
     * @exception CreateException - if the create fails.
     */
    public SupplierEnt create(
        int suppID, String suppName, String suppStreet1,
            String suppStreet2, String suppCity, String suppState,
                String suppCountry, String suppZip, String suppPhone,
                    String suppContact)
                        throws RemoteException, CreateException;

    /**
     * findByPrimaryKey: find the supplier whose id = pk.
     * @param pk - id of supplier.
     * @return SUpplierEnt.
     * @exception RemoteException - if there is a system failure.
     * @exception FinderException - if cannot find object for pk.
     */
    public SupplierEnt findByPrimaryKey(Integer pk)
        throws RemoteException, FinderException;

    /**
     * findAll: find all suppliers.
     * @return Enumeration - of Suppliers.
     * @exception RemoteException - if there is a system failure.
     * @exception FinderException - if there are not any suppliers.
     */
    public Enumeration findAll() throws RemoteException, FinderException;
}

