
/*
 *  Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 *
 * akmits@eng.sun.com 04/04/2000
 */
package com.sun.ecperf.mfg.largeorderent.ejb;


import javax.ejb.*;

import java.rmi.RemoteException;

import java.util.*;

import com.sun.ecperf.common.*;


/**
 * This interface is the home interface for the EJBean LargeOrder
 *
 * @author Ajay Mittal
 *
 *
 */
public interface LargeOrderEntHome extends javax.ejb.EJBHome {

    /**
     * This method corresponds to the ejbCreate method in the bean
     * "LargeOrder___EJB.java".
     *
     * @param salesOrderId the id of sales order that caused this wo to be created
     * @param orderLineNumber line (row) number in salesOrder identified by salesOrderId
     * @param assemblyId assembly that is going to be manufactured
     * @param qty number of assemblies to be manufactured by this wo
     * @param dueDate date when this order is due
    */
    public LargeOrderEnt create(
        int salesOrderId, int orderLineNumber, String assemblyId, short qty,
            java.sql.Date dueDate) throws RemoteException, CreateException;

    /**
     * This method finds the workorder that is uniquely identfied by the id
     * @param id ID of the large order to uniquely identify it
    */
    public LargeOrderEnt findByPrimaryKey(Integer id)
        throws RemoteException, FinderException;

    /**
     * Finds the LargeOrder that refers to the Order's id and line item.
     * @param salesId The order id of the sales order representing this
     *                LargeOrder
     * @param oLineId The order line item in the order it refers to
     */
    public LargeOrderEnt findByOrderLine(int salesId, int oLineId)
        throws RemoteException, FinderException;

    /**
     * This method finds the all the unprocessed workorders
    */
    public java.util.Enumeration findAll()
        throws RemoteException, FinderException;
}

