/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: RuleEntHome.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 * $Mod: RulEntHome.java,v 1.5 2001/04/14 08:30:00 hogstrom - Modified
 *       bean to support single row entities.
 *
 */
package com.sun.ecperf.corp.ruleent.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the home interface of the item entity bean in the Customer
 * domain.
 */
public interface RuleEntHome extends EJBHome {

    /**
     * This method corresponds to the ejbCreate method of the bean.
     * Note that a new item is never actually created as part of
     * of the ECperf workload.
     * @param id - Part number of item to create
     * @param price - Price of the item
     * @param name - Name of the item
     * @param description - Description of the item
     * @param discount - Discount applied for this item
     */
    RuleEnt create(String id, String rules)
        throws RemoteException, CreateException;

    RuleEnt findByPrimaryKey(String id)
        throws RemoteException, FinderException;

    java.util.Enumeration findAll() throws RemoteException, FinderException;
}
