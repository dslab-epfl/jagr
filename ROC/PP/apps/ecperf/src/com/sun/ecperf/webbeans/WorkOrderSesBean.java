
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: WorkOrderSesBean.java,v 1.2 2003/03/22 04:55:02 emrek Exp $
 *
 *
 */
package com.sun.ecperf.webbeans;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;

import java.io.Serializable;

import com.sun.ecperf.mfg.workorderses.ejb.*;
import com.sun.ecperf.mfg.helper.*;
import com.sun.ecperf.common.*;

import java.util.Vector;

import javax.servlet.*;

import java.sql.Date;

import javax.servlet.http.*;


/**
 * This is a helper bean used by jsp files to schedule, update,
 * complete, and cancel a work order. This is also used to get
 * work order status.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public class WorkOrderSesBean implements Serializable {

    private final String     jndiname = "java:comp/env/ejb/WorkOrderSes";
    private WorkOrderSesHome work_orderses_home;
    private WorkOrderSes     work_orderses;
    protected Debug          debug;
    protected boolean        debugging;

    /**
     * Constructor WorkOrderSesBean
     *
     *
     * @throws OtherException
     *
     */
    public WorkOrderSesBean() throws OtherException {

        try {
            Context context    = new InitialContext();
            int     debugLevel = 0;

            try {
                debugLevel =
                    ((Integer) context.lookup("java:comp/env/debuglevel"))
                        .intValue();
            } catch (Exception e) {

                // If there's an error looking up debuglevel,
                // just leave it as the default - 0
            }
            if (debugLevel > 0) {
                debug = new DebugPrint(debugLevel, this);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }
            if (debugging) 
                debug.println(3, "In constructor of WorkOrderSesBean");

            Object obj = context.lookup(jndiname);

            if (debugging) 
                debug.println(3, "Looked up " + jndiname);

            work_orderses_home =
                (WorkOrderSesHome) PortableRemoteObject.narrow(obj,
                    WorkOrderSesHome.class);
            work_orderses      = work_orderses_home.create();

            if (debugging) 
                debug.println(3, "Successfully created WorkOrderSes Bean");
        } catch (NamingException e) {
            throw new OtherException("Naming Exception in WorkOrderSesBean",
                                     e);
        } catch (ClassCastException e) {
            throw new OtherException(
                "Class cast Exception in WorkOrderSesBean", e);
        } catch (RemoteException e) {
            throw new OtherException("Remote Exception in WorkOrderSesBean",
                                     e);
        } catch (CreateException e) {
            throw new OtherException("Create Exception in WorkOrderSesBean",
                                     e);
        } catch (Exception e) {
            throw new OtherException(
                "Some Other  Exception in WorkOrderSesBean", e);
        }
    }

    /**
     * Method scheduleWorkOrder - Schedule a work order
     *
     *
     * @param assembly_id  Assembly id
     * @param quantity Quantity to be scheduled
     * @param date Due date
     * @param line_num Order line number
     * @param sales_id Sales order id
     *
     * @return Integer - Order id
     *
     * @throws OtherException
     *
     */
    public Integer scheduleWorkOrder(
            String assembly_id, String quantity, String date, String line_num, String sales_id)
                throws OtherException {

        java.sql.Date due_date;
        int           year, month, day;
        Integer       order_id;
        int           qty;
        int           order_line_num, sales_order_id;

        if (debugging) 
            debug.println(3, "In service method of scheduleWorkOrder");

        try {
            qty = Integer.parseInt(quantity);

            if (qty <= 0) {
                throw new OtherException(" Quantity should be positive");
            }

            if ((date == null) || (line_num == null) || (sales_id == null)) {
                order_id = work_orderses.scheduleWorkOrder(assembly_id, qty,
                                                           null);
            } else {
                order_line_num = Integer.parseInt(line_num);
                sales_order_id = Integer.parseInt(sales_id);
                year           = Integer.parseInt(date.substring(0, 4));
                month          = Integer.parseInt(date.substring(5, 7));
                day            = Integer.parseInt(date.substring(8));
                due_date       = new java.sql.Date(year, month, day);
                order_id       =
                    work_orderses.scheduleWorkOrder(sales_order_id,
                                                    order_line_num,
                                                    assembly_id, qty,
                                                    due_date);
            }
        } catch (RemoteException e) {
            throw new OtherException(
                "Remote Exception in scheduleWorkOrder of WorkOrderSesBean",
                e);
        } catch (NumberFormatException e) {
            throw new OtherException(
                "NumberFormatExcepion in scheduleWorkOrder of WorkOrderSesBean");
        }

        if (debugging) 
            debug.println(3, "Finished service method of scheduleWorkOrder");

        return order_id;
    }

    /**
     * Method getWorkOrderStatus - Get work order status
     *
     *
     * @param order_id Work order id
     *
     * @return String - Work order status
     *
     * @throws OtherException
     *
     */
    public String getWorkOrderStatus(Integer order_id) throws OtherException {

        String status;
        int    status_val;

        try {
            status_val = work_orderses.getWorkOrderStatus(order_id);
        } catch (RemoteException e) {
            throw new OtherException(
                "Remote Exception in getWorkOrderStatus of WorkOrderSesBean",
                e);
        }

        status = WorkOrderStateConstants.woStates[status_val];

        return status;
    }

    /**
     * Method updateWorkOrder - Update work order
     *
     *
     * @param order_id Work order id
     *
     * @return boolean - Return true if work order can be updated, false otherwise.
     *
     * @throws OtherException
     *
     */
    public boolean updateWorkOrder(Integer order_id) throws OtherException {

        int status_val;

        try {
            status_val = work_orderses.getWorkOrderStatus(order_id);

            if (!((status_val == WorkOrderStateConstants.STAGE1)
                    || (status_val == WorkOrderStateConstants.STAGE2))) {
                return false;
            }

            work_orderses.updateWorkOrder(order_id);

            return true;
        } catch (RemoteException e) {
            throw new OtherException(
                "Remote Exception in updateWorkOrder of WorkOrderSesBean", e);
        }
    }

    /**
     * Method completeWorkOrder - Complete work order
     *
     *
     * @param order_id Order id
     *
     * @return boolean - true if work order can be completed, false otherwise
     *
     * @throws OtherException
     *
     */
    public boolean completeWorkOrder(Integer order_id) throws OtherException {

        int status_val;

        try {
            status_val = work_orderses.getWorkOrderStatus(order_id);

            if (status_val == WorkOrderStateConstants.STAGE1) {
                work_orderses.updateWorkOrder(order_id);
                work_orderses.updateWorkOrder(order_id);
            } else if (status_val == WorkOrderStateConstants.STAGE2) {
                work_orderses.updateWorkOrder(order_id);
            }

            return work_orderses.completeWorkOrder(order_id);
        } catch (RemoteException e) {
            throw new OtherException(
                "Remote Exception in completeWorkOrder of WorkOrderSesBean",
                e);
        }
    }

    /**
     * Method cancelWorkOrder - Cancel work order
     *
     *
     * @param order_id Order id
     *
     * @return boolean - false if failed to cancel
     *
     * @throws OtherException
     *
     */
    public boolean cancelWorkOrder(Integer order_id) throws OtherException {

        int status_val;

        try {
            status_val = work_orderses.getWorkOrderStatus(order_id);

            if (status_val == WorkOrderStateConstants.STAGE1) {
                return work_orderses.cancelWorkOrder(order_id);
            } else {
                return false;
            }
        } catch (RemoteException e) {
            throw new OtherException(
                "Remote Exception in cancelWorkOrder of WorkOrderSesBean", e);
        }
    }
}

