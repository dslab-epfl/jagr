
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: CustomerEnt.java,v 1.1.1.1 2002/11/16 05:35:25 emrek Exp $
 *
 */
package com.sun.ecperf.corp.customerent.ejb;


import java.rmi.*;

import javax.ejb.*;

import com.sun.ecperf.common.*;


/**
 * This is the public face of the customer entity bean in the corporate
 * domain.
 */
public interface CustomerEnt extends EJBObject {

    /**
     * This method is called from the OrderBean in the Customer domain
     * to check whether a customer has sufficient credt.
     * @param amount - amount of credit required
     * @return true if sufficient credit exists, else false
     */
    public boolean hasSufficientCredit(double amount) throws RemoteException;

    /**
     * Method getPercentDiscount
     * Get the percentage discount for this customer based on the rules stored
     * for discounts. Use the rule engine for processing.
     *
     * @param amount
     *
     * @return percentage discount
     *
     * @throws DataIntegrityException
     * @throws RemoteException
     *
     */
    public double getPercentDiscount(double amount)
        throws DataIntegrityException, RemoteException;
}

