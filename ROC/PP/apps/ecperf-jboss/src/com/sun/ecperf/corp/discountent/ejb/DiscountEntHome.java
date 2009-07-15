
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: DiscountEntHome.java,v 1.1 2004/02/19 14:45:07 emrek Exp $
 *
 */
package com.sun.ecperf.corp.discountent.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the home interface of the Discount entity bean in the Customer
 * domain.
 */
public interface DiscountEntHome extends EJBHome {

    DiscountEnt create(String id, double percent)
        throws RemoteException, CreateException;

    DiscountEnt findByPrimaryKey(String id)
        throws RemoteException, FinderException;

    java.util.Enumeration findAll() throws RemoteException, FinderException;
}

