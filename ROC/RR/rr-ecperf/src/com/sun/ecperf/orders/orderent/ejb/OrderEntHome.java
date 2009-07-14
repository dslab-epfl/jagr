
/*
 *  Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrderEntHome.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.orders.orderent.ejb;


import javax.ejb.*;

import java.rmi.RemoteException;

import java.util.*;

import com.sun.ecperf.orders.helper.*;
import com.sun.ecperf.common.*;


/**
 * This interface is the home interface for the EJBean OrderBean.
 *
 */
public interface OrderEntHome extends EJBHome {

    /**
     * This method corresponds to the ejbCreate method in the bean
     * "OrderBean.java".
     *
     * @param customerId int Id of customer creating this order
     * @param quantities itemId,itemQuantity pairs for all items in this order
     * @return Order
     * @exception InsufficientCreditException if customer credit check fails
     * @exception CreateException if the create fails
     * @exception RemoteException if there is a system failure
     */
    OrderEnt create(int customerId, ItemQuantity[] quantities)
        throws InsufficientCreditException, CreateException, RemoteException;

    OrderEnt findByPrimaryKey(Integer id)
        throws RemoteException, FinderException;

    /**
     * findByCustomer: Find orders by customerId
     *
     * @param customerId int Id of customer whose orders is required
     * @return Enumeration
     * @exception RemoteException if there is a system failure
     * @exception FinderException if the find fails
     */
    Enumeration findByCustomer(int customerId)
        throws RemoteException, FinderException;
}

