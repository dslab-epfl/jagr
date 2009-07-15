
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: OrderCustomerEnt.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.orders.ordercustomerent.ejb;


import javax.ejb.*;

import java.rmi.*;

import com.sun.ecperf.common.*;


/**
 * This is the customer entity bean in the Customer domain.
 * It represents a row in the Customer table of the orders database.
 */
public interface OrderCustomerEnt extends EJBObject {

    /**
     * This method computes the customer discount based on
     * some business logic
     */
    public double getPercentDiscount(double total)
        throws DataIntegrityException, RemoteException;
}

