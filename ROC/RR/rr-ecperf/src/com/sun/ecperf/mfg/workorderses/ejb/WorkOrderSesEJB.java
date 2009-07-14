
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * akmits@eng.sun.com 04/03/2000
 * @author Ajay Mittal
 *
 *
 */
package com.sun.ecperf.mfg.workorderses.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.mfg.workorderent.ejb.*;
import com.sun.ecperf.mfg.componentent.ejb.*;
import com.sun.ecperf.mfg.helper.*;
import com.sun.ecperf.common.*;

import java.sql.*;


/**
 * This class is WorkOrderSesEJB seesion bean.
 *
 * This bean is stateless.
 *
 */
public class WorkOrderSesEJB implements SessionBean {

    private String           className = "WorkOrderSesEJB";
    private WorkOrderEntHome workOrderHome;
    private ComponentEntHome compEntHome;
    protected Debug          debug;
    protected boolean        debugging;

    /**
     * Method ejbCreate
     *
     *
     * @throws CreateException
     *
     */
    public void ejbCreate() throws CreateException {
	if (debugging)
	    debug.println(3, "ejbCreate ");
    }

    // if called from manufacturing app (driver)

    /**
     * Method to schedule a work order.
     * @param assemblyId    Assembly Id
     * @param qty           Original Qty
     * @param dueDate       Date when order is due
     *
     * @return Workorder id
     * @exception RemoteException if there is a system failure
     */
    public Integer scheduleWorkOrder(String assemblyId, int qty, java.sql
            .Date dueDate) throws RemoteException {

	if (debugging)
	    debug.println(3, "scheduleWorkOrder ");

        WorkOrderEnt workOrder;
        Integer      woId = null;

        try {
            workOrder = workOrderHome.create(assemblyId, qty, dueDate);

            if (workOrder != null) {
		if (debugging)
		    debug.println(3, "Work Order created");
            }

            woId = workOrder.getId();

        } catch (CreateException e) {
	    if (debugging)
		debug.println(1, "Exception for workorder " + assemblyId
                          + " Exception is : " + e);
            throw new EJBException("Unable to create " + e);
        } catch (RemoteException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        }

        try {
            workOrder.process();
	    if (debugging)
		debug.println(3, "Stage 1 done");
        } catch (IllegalStateException e) {
	    if (debugging)
		debug.println(1, "Illegal State exception ");

            throw new EJBException("Unable to process " + e);
        }

        return woId;
    }

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
            int salesId, int oLineId, String assemblyId, int qty, java.sql
                .Date dueDate) throws RemoteException {

	if (debugging)
	    debug.println(3, "scheduleWorkOrder ");

        WorkOrderEnt workOrder;
        Integer      woId = null;

        try {
            workOrder = workOrderHome.create(salesId, oLineId, assemblyId,
                                             qty, dueDate);
            woId      = workOrder.getId();

        } catch (CreateException e) {
	    if (debugging)
		debug.println(1, "create exception ");
            debug.printStackTrace(e);
            throw new EJBException(e);
        } catch (RemoteException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        }

        try {
            workOrder.process();
        } catch (IllegalStateException e) {
	    if (debugging)
		debug.println(1, "Illegal State exception ");

            throw new EJBException("Unable to process " + e);
        }

        return woId;
    }

    /**
     * completeWorkOrder: workorder cancel.
     *
     * Transfer completed portion to inventory.
     * @param wid               WorkOrder ID
     * @return                  boolean false if failed to complete
     * @exception               RemoteException if there is
     *                          a communications or systems failure
     */
    public boolean completeWorkOrder(Integer wid) throws RemoteException {

	if (debugging)
	    debug.println(3, "completeWorkOrder ");

        try {
            WorkOrderEnt workOrder = workOrderHome.findByPrimaryKey(wid);

            workOrder.finish();

            return true;
        } catch (IllegalStateException e) {
	    if (debugging)
		debug.println(1, "Illegal State exception " + e);

            return false;
        } catch (FinderException e) {
	    if (debugging)
		debug.println(1, "Could not find bean for wid=" + wid);

            return false;
        }
    }

    /**
     * cancelWorkOrder: workorder cancel.
     *
     * Transfer completed portion to inventory. Abort remaining work order
     * @param wid               WorkOrder ID
     * @return                  boolean false if failed to complete
     * @exception               RemoteException if there is
     *                          a communications or systems failure
     */
    public boolean cancelWorkOrder(Integer wid) throws RemoteException {

	if (debugging)
	    debug.println(3, "cancelWorkOrder ");

        try {
            WorkOrderEnt workOrder = workOrderHome.findByPrimaryKey(wid);
            boolean      bret      = workOrder.cancel();

            workOrder.remove();

            return (bret);
        } catch (IllegalStateException e) {
	    if (debugging)
		debug.println(1, "Illegal State exception ");

            return false;
        } catch (RemoveException e) {
	    if (debugging)
		debug.println(1, "Remove exception ");

            return false;
        } catch (FinderException e) {
	    if (debugging)
		debug.println(1, "Could not find bean for wid=" + wid);

            return false;
        }
    }

    /**
     * Get completed Qty in the workorder
     *
     * @param wid                     WorkOrder ID
     * @return             int status
     * @exception          RemoteException if there is
     *                     a communications or systems failure
     */
    public int getWorkOrderCompletedQty(Integer wid) throws RemoteException {

	if (debugging)
	    debug.println(3, "getWorkOrderCompletedQty ");

        try {
            WorkOrderEnt workOrder = workOrderHome.findByPrimaryKey(wid);

            return (workOrder.getCompQty());
        } catch (IllegalStateException e) {
	    if (debugging)
		debug.println(1, "could not get completed qty ");

            throw new EJBException("could not get completed qty  " + e);
        } catch (FinderException e) {
	    if (debugging)
		debug.println(1, "Could not find bean for id=" + wid);

            throw new EJBException("Could not find bean for id=  " + wid
                                   + " " + e);
        }
    }

    /**
     * Get status of a workorder
     *
     * @param wid               WorkOrder ID
     * @return             int status
     * @exception          RemoteException if there is
     *                     a communications or systems failure
     */
    public int getWorkOrderStatus(Integer wid) throws RemoteException {

	if (debugging)
	    debug.println(3, "getWorkOrderStatus ");

        try {
            WorkOrderEnt workOrder = workOrderHome.findByPrimaryKey(wid);

            return (workOrder.getStatus());
        } catch (IllegalStateException e) {
	    if (debugging)
		debug.println(1, "can not get status ");

            throw new EJBException("can not get status " + e);
        } catch (FinderException e) {
	    if (debugging)
		debug.println(1, "Could not find bean for id=" + wid);

            throw new EJBException("Could not find bean for id=  " + wid
                                   + " " + e);
        }
    }

    /**
     * Update status of a workorder
     *
     * @param wid                      WorkOrder ID
     * @exception               RemoteException if there is
     *                          a communications or systems failure
     */
    public void updateWorkOrder(Integer wid) throws RemoteException {

	if (debugging)
	    debug.println(3, "updateWorkOrder ");

        try {
            WorkOrderEnt workOrder = workOrderHome.findByPrimaryKey(wid);
            workOrder.update();
        } catch (IllegalStateException e) {
	    if (debugging)
		debug.println(1, "can not update ");

            throw new EJBException("can not update " + e);
        } catch (FinderException e) {
	    if (debugging)
		debug.println(1, "Could not find bean for id=" + wid);

            throw new EJBException("Could not find bean for id=  " + wid
                                   + " " + e);
        }
    }
    /**
     * Constructor WorkOrderSesEJB
     *
     *
     */
    public WorkOrderSesEJB() {}

    /**
     * Method ejbRemove
     *
     *
     */
    public void ejbRemove() {}

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {}

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {}

    /**
     * Method setSessionContext
     *
     *
     * @param sc
     *
     */
    public void setSessionContext(SessionContext sc) {

        InitialContext initCtx;

        try {
            initCtx       = new InitialContext();
            workOrderHome =
                (WorkOrderEntHome) javax.rmi.PortableRemoteObject.narrow(
                    initCtx.lookup("java:comp/env/ejb/WorkOrderEnt"),
                    WorkOrderEntHome.class);
            compEntHome   =
                (ComponentEntHome) javax.rmi.PortableRemoteObject.narrow(
                    initCtx.lookup("java:comp/env/ejb/ComponentEnt"),
                    ComponentEntHome.class);
        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }

        try {
            int debugLevel =
                ((Integer) initCtx.lookup("java:comp/env/debuglevel"))
                    .intValue();

            if (debugLevel > 0) {
                debug = new DebugPrint(debugLevel, this);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }
        } catch (NamingException ne) {
            System.out.println(className + ":debuglevel Property not set. "
                               + "Turning off debug messages");

            debug = new Debug();
        }

	if (debugging)
	    debug.println(3, "setSessionContext");
    }
}

