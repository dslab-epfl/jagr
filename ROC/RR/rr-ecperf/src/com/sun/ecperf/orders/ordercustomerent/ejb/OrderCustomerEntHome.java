
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrderCustomerEntHome.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.orders.ordercustomerent.ejb;


import javax.ejb.*;

import java.rmi.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.orders.helper.*;


/**
 * Interface OrderCustomerEntHome
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface OrderCustomerEntHome extends EJBHome {

    /**
     * Create a new customer
     * @param CustomerInfo info (information for customer row)
     * @exception CreateException if the create fails
     * @exception InvalidInfoException if the customer info is invalid
     * @exception DataIntegrityException if 'customer' entry not found in U_sequences
     * @exception RemoteException if there is a system failure
     */
    OrderCustomerEnt create(CustomerInfo info)
        throws DataIntegrityException, InvalidInfoException, 
               RemoteException, CreateException;

    OrderCustomerEnt findByPrimaryKey(Integer id)
        throws RemoteException, FinderException;
}

