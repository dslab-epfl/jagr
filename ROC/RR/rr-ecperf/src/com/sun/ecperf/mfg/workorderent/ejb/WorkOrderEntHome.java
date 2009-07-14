
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.workorderent.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the home interface of the WorkOrder entity bean in the Mfg
 * domain.
 */
public interface WorkOrderEntHome extends EJBHome {

    /**
     * This method corresponds to the ejbCreate method of the bean.
   * @param salesId       Sales order id
   * @param oLineId       Order Line ID
     * @param assemblyId
   * @param origQty       Original Qty
   * @param dueDate       Date when order is due
   * @return WorkOrderEnt
   */
    public WorkOrderEnt create(
        int salesId, int oLineId, String assemblyId, int origQty,
            java.sql.Date dueDate) throws RemoteException, CreateException;

    /**
     * Method create
     *
     *
     * @param assemblyId
     * @param origQty
     * @param dueDate
     *
     * @return
     *
     * @throws CreateException
     * @throws RemoteException
     *
     */
    public WorkOrderEnt create(String assemblyId, int origQty, java.sql
        .Date dueDate) throws RemoteException, CreateException;

    /**
     * Method findByPrimaryKey
     *
     *
     * @param id
     *
     * @return
     *
     * @throws FinderException
     * @throws RemoteException
     *
     */
    public WorkOrderEnt findByPrimaryKey(Integer id)
        throws RemoteException, FinderException;

    /**
     * Method findAll
     *
     *
     * @return
     *
     * @throws FinderException
     * @throws RemoteException
     *
     */
    public java.util.Enumeration findAll()
        throws RemoteException, FinderException;
}

