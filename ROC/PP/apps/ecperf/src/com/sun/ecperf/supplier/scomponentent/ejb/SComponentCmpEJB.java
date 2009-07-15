
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: SComponentCmpEJB.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.scomponentent.ejb;


//Import statements
import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import com.sun.ecperf.common.*;

import java.util.*;

import java.sql.*;


/**
 * This class implements the Component Entity bean
 * container managed.
 *
 * @author Damian Guy
 */
public class SComponentCmpEJB implements EntityBean {

    public String           compID;
    public String           compName;
    public String           compDesc;
    public String           compUnit;
    public double           compCost;
    public int              qtyOnOrder;
    public int              qtyDemanded;
    public int              leadTime;
    public int              containerSize;
    protected EntityContext entityContext;
    protected Debug         debug;
    protected boolean       debugging;
   protected boolean isDirty = true;

    /**
     * getID - get the id of this component.
     * @return String
     */
    public String getID() {
        return compID;
    }

    /**
     * getQtyOnOrder: get method for qtyOnOrder instance variable.
     * @return int - the quantity on order for this component.
     */
    public int getQtyOnOrder() {
        return qtyOnOrder;
    }

    /**
     * getQtyDemanded: get methof for qtyDemanded instance variable.
     * @return int - the quantity currently demanded for this component.
     */
    public int getQtyDemanded() {
        return qtyDemanded;
    }

    /**
     * getContainerSize: get method for containerSize instance variable.
     * @return int - the size of the container (How many parts to order)
     */
    public int getContainerSize() {
        return containerSize;
    }

    /**
     * getLeadTime: get the maximum allowable lead time
     * for this component.
     * @return int -the maximum lead time.
     */
    public int getLeadTime() {
        return leadTime;
    }

    /**
     * checkForPO: check if there is an outstanding PO
     * for this component, and if the qtyOnOrder will
     * satisfy the qtyDemanded + the current qty required.
     *
     * @param qtyRequired
     * @return boolean - true if above condition satisified.
     */
    public boolean checkForPO(int qtyRequired) {

        if ((qtyDemanded + qtyRequired) <= qtyOnOrder) {
            return true;
        }

        return false;
    }

    /**
     * updateDemand: update the qtyDemanded for a component.
     * @param qtyRequired - quantity to add to existing qtyDemanded.
     */
    public void updateDemand(int qtyRequired) {
        qtyDemanded += qtyRequired;
        isDirty = true;
    }

    /**
     * updateQuantities: update the qtyOnOrder and qtyDemanded fields.
     *
     * @param qtyOrdered
     * @param qtyDemanded - qty to add to qtyDemanded.
     */
    public void updateQuantities(int qtyOrdered, int qtyDemanded) {
        this.qtyDemanded += qtyDemanded;
        qtyOnOrder      += qtyOrdered;
        isDirty = true;
    }

    /**
     * deliveredQuantity: used to update the qtyOnOrder and
     * qtyDemanded fields when an order has been delivered.
     */
    public void deliveredQuantity(int quantityDelivered) {

        int tmpDemanded = qtyDemanded - quantityDelivered;

        qtyDemanded = (tmpDemanded < 0)
                       ? 0
                       : tmpDemanded;
        qtyOnOrder -= quantityDelivered;
        isDirty = true;
    }

    /**
     * ejbCreate: Create new Component.
     *
     * @param compID
     * @param compName
     * @param compDesc
     * @param compUnit
     * @param compCost
     * @param qtyOnOrder
     * @param qtyDemanded
     * @param leadTime
     * @param containerSize
     * @return ComponentEnt
     * @exception CreateException - if the create fails.
     */
    public String ejbCreate(
            String compID, String compName, String compDesc, String compUnit, double compCost, int qtyOnOrder, int qtyDemanded, int leadTime, int containerSize)
                throws CreateException {

        if (debugging)
            debug.println(3, "ejbCreate");

        this.compID        = compID;
        this.compName      = compName;
        this.compDesc      = compDesc;
        this.compUnit      = compUnit;
        this.compCost      = compCost;
        this.qtyOnOrder   = qtyOnOrder;
        this.qtyDemanded   = qtyDemanded;
        this.leadTime      = leadTime;
        this.containerSize = containerSize;

        isDirty = false;
        return this.compID;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param compID
     * @param compName
     * @param compDesc
     * @param compUnit
     * @param compCost
     * @param qtyOnOrder
     * @param qtyDemanded
     * @param leadTime
     * @param containerSize
     *
     */
    public void ejbPostCreate(String compID, String compName,
                              String compDesc, String compUnit,
                              double compCost, int qtyOnOrder,
                              int qtyDemanded, int leadTime,
                              int containerSize) {}

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        if (debugging)
            debug.println(3, "ejbActivate ");
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
        if (debugging)
            debug.println(3, "ejbPassivate ");
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {
        if (debugging)
            debug.println(3, "ejbLoad ");
        isDirty = false;
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {
        if (debugging)
            debug.println(3, "ejbStore ");
        isDirty = false;
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {}

    /**
     * Method setEntityContext
     *
     *
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        try {
            Context context    = new InitialContext();
            int     debugLevel =
                ((Integer) context.lookup("java:comp/env/debuglevel"))
                    .intValue();

            if (debugLevel > 0) {
                debug = new DebugPrint(debugLevel, this);
                debugging = true;
            } else {
                debug = new Debug();
                debugging = false;
            }
        } catch (NamingException ne) {
            debug = new Debug();
        }

        this.entityContext = entityContext;
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {}

   public boolean isModified() { return isDirty; }
}

