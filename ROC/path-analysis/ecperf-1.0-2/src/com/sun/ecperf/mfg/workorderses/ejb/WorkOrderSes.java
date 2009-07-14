
/*
 *
 * Copyright (c) 1999-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 * @author Ajay Mittal
 *
 * akmits@eng.sun.com 04/03/2000
 */
package com.sun.ecperf.mfg.workorderses.ejb;


import javax.ejb.EJBObject;

import java.rmi.RemoteException;


/**
 * This interface is the remote interface for the WorkOrderSes
 * session bean. This bean is stateless.
 *
 * @author Ajay Mittal
 *
 *
 */
public interface WorkOrderSes extends EJBObject {

    // if called from manufacturing app (driver)

    /**
     * Method to schedule a work order.
     * @param assemblyId    Assembly Id
     * @param qty           Original Qty
     * @param dueDate       Date when order is due
     *
     * @return
     * @exception RemoteException if there is a system failure
     */
    public Integer scheduleWorkOrder(String assemblyId, int qty, java.sql
        .Date dueDate) throws RemoteException;

    // if called from order domain

    /**
     * Method to schedule a work order.
     * @param salesId       Sales order id
     * @param oLineId       Order Line ID
     * @param assemblyId    Assembly Id
     * @param qty           Original Qty
     * @param dueDate       Date when order is due
     *
     * @return
     * @exception RemoteException if there is a system failure
     */
    public Integer scheduleWorkOrder(
        int salesId, int oLineId, String assemblyId, int qty,
            java.sql.Date dueDate) throws RemoteException;

    /**
       * completeWorkOrder: workorder cancel.
       *
       * Transfer completed portion to inventory.
       * @param wid               WorkOrder ID
       * @return                  boolean false if failed to complete
       * @exception               RemoteException if there is
       *                          a communications or systems failure
       */
    public boolean completeWorkOrder(Integer wid) throws RemoteException;

    /**
      * cancelWorkOrder: workorder cancel.
      *
      * Transfer completed portion to inventory. Abort remaining work order
      * @param wid               WorkOrder ID
      * @return                  boolean false if failed to complete
      * @exception               RemoteException if there is
      *                          a communications or systems failure
      */
    public boolean cancelWorkOrder(Integer wid) throws RemoteException;

    /**
     * Get completed Qty in the workorder
     *
     * @param wid                     WorkOrder ID
     * @return             int status
     * @exception          RemoteException if there is
     *                     a communications or systems failure
     */
    public int getWorkOrderCompletedQty(Integer wid) throws RemoteException;

    /**
     * Get status of a workorder
     *
     * @param wid               WorkOrder ID
     * @return             int status
     * @exception          RemoteException if there is
     *                     a communications or systems failure
     */
    public int getWorkOrderStatus(Integer wid) throws RemoteException;

    /**
     * Update status of a workorder
     *
     * @param wid                      WorkOrder ID
     * @exception               RemoteException if there is
     *                          a communications or systems failure
     */
    public void updateWorkOrder(Integer wid) throws RemoteException;
}

