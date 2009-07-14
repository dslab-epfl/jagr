
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.workorderent.ejb;


import javax.ejb.*;

import java.rmi.*;

import javax.naming.*;

import java.sql.*;

import java.util.*;

import com.sun.ecperf.util.sequenceses.ejb.*;
import com.sun.ecperf.mfg.helper.*;
import com.sun.ecperf.mfg.helper.ComponentDemand;
import com.sun.ecperf.mfg.largeorderent.ejb.*;
import com.sun.ecperf.mfg.assemblyent.ejb.*;
import com.sun.ecperf.mfg.componentent.ejb.*;
import com.sun.ecperf.mfg.boment.ejb.*;
import com.sun.ecperf.supplier.buyerses.ejb.*;
import com.sun.ecperf.common.*;


/**
 * Class WorkOrderCmpEJB
 *
 *
 * @author
 * @version %I%, %G%
 */
public class WorkOrderCmpEJB implements EntityBean {

    protected EntityContext     entityContext;
    public Integer              id;
    public int                  salesId;
    public int                  oLineId;
    public int                  status;
    public int                  origQty;
    public int                  compQty;
    public String               assemblyId;
    public java.sql.Date        dueDate;
    public java.sql.Timestamp	startDate;
    private SequenceSesHome     sequenceHome;
    private LargeOrderEntHome   loEntHome;
    private AssemblyEntHome     assEntHome;
    private ComponentEntHome    compEntHome;
    private BuyerSesHome        buyerSesHome;
    private Vector              compList;
    private AssemblyEnt         assemblyItem;
    private static final String className = "WorkOrderCmpEJB";
    protected WorkOrderState    wos       = null;
    protected Debug             debug;
    protected boolean		debugging;

    /**
     * Constructs the WorkOrder object (Container managed)
     * and container stores the information into the DB.
     * @param salesId  Sales Order Id for a custom order
     * @param oLineId  Orderline id in sales order
     * @param assemblyId
     * @param origQty  Original qty
     * @param dueDate  Date when order is due
     * @return primary key of WorkOrder which is composed of id.
     */
    public Integer ejbCreate(
            int salesId, int oLineId, String assemblyId, int origQty, java.sql
                .Date dueDate) throws CreateException {

	if (debugging)
	    debug.println(3, "ejbCreate for LargeOrder");

        assemblyItem = null;    // we need to reset this cached ref

        try {
            loEntHome.findByOrderLine(salesId, oLineId).remove();
        } catch (FinderException fe) {
            debug.printStackTrace(fe);

            throw new EJBException("Cannot find LargeOrder for Order: "
                                   + salesId + " Line: " + oLineId);
        } catch (RemoveException de) {
            debug.printStackTrace(de);

            throw new EJBException(de);
        } catch (RemoteException re) {
            debug.printStackTrace(re);

            throw new EJBException(re);
        }

        this.salesId    = salesId;
        this.oLineId    = oLineId;
        this.assemblyId = assemblyId;
        this.origQty    = origQty;
        this.dueDate    = dueDate;

        // Initiate the state
        wos            = WorkOrderState.getInstance(WorkOrderState.OPEN);
        this.status    = wos.getStatus();
        this.startDate = new java.sql.Timestamp(new java.util.Date().getTime());
        this.compQty   = 0;

        try {
            SequenceSes sequence = sequenceHome.create();

            this.id = new Integer(sequence.nextKey("workorder"));
        } catch (CreateException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (FinderException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (Exception e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }
        return null;
    }

    /**
     * Constructs the WorkOrder object (Container managed)
     * and container stores the information into the DB.
     * This version is for forecasted work, no salesId and
     * oLineId.
     *
     * @param assemblyId
     * @param origQty  Original qty
     * @param dueDate  Date when order is due
     * @return primary key of WorkOrder which is composed of id.
     */
    public Integer ejbCreate(String assemblyId, int origQty, java.sql
            .Date dueDate) throws CreateException {

	if (debugging)
	    debug.println(3, "ejbCreate");

        assemblyItem    = null;
        this.salesId    = 0;
        this.oLineId    = 0;
        this.assemblyId = assemblyId;
        this.origQty    = origQty;
        this.dueDate    = dueDate;

        // Initiate the state
        wos            = WorkOrderState.getInstance(WorkOrderState.OPEN);
        this.status    = wos.getStatus();
        this.startDate = new java.sql.Timestamp(new java.util.Date().getTime());
        this.compQty   = 0;

        try {
            SequenceSes sequence = sequenceHome.create();

            this.id = new Integer(sequence.nextKey("workorder"));
        } catch (CreateException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (FinderException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (RemoteException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }

        return null;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param salesId
     * @param oLineId
     * @param assemblyId
     * @param origQty
     * @param dueDate
     *
     */
    public void ejbPostCreate(int salesId, int oLineId, String assemblyId,
                              int origQty, java.sql.Date dueDate) {}

    /**
     * Method ejbPostCreate
     *
     *
     * @param assemblyId
     * @param origQty
     * @param dueDate
     *
     */
    public void ejbPostCreate(String assemblyId, int origQty,
                              java.sql.Date dueDate) {}

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {}

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        assemblyItem = null;
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {}

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {

        // container calls ejbLoad after the DB has been read
	if (debugging)
	    debug.println(3, "ejbLoad");

        wos = wos.getInstance(status);
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {

        // container calls ejbStore before it writes to DB
	if (debugging)
	    debug.println(3, "ejbStore");

        status = wos.getStatus();
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {
        entityContext = null;
    }

    /**
     * Method setEntityContext
     *
     *
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        Context context = null;

        try {
            context = new InitialContext();
        } catch (Exception e) {
            e.printStackTrace(System.err);

            throw new EJBException(e);
        }

        try {
            int debugLevel =
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
            System.out.println("WorkOrderCmpEJB: debuglevel Property "
                               + "not set. Turning off debug messages");

            debug = new Debug();
        }

        this.entityContext = entityContext;

        // we need to get AssemblyEntHome and ComponentEntHome
        //  home objects
        try {
            sequenceHome =
                (SequenceSesHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/SequenceSes"),
                    SequenceSesHome.class);

	    if (debugging)
		debug.println(3, "found SequenceSesHome interface");

            loEntHome =
                (LargeOrderEntHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/LargeOrderEnt"),
                    LargeOrderEntHome.class);

	    if (debugging)
		debug.println(3, "found LargeOrderEntHome interface");

            assEntHome =
                (AssemblyEntHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/AssemblyEnt"),
                    AssemblyEntHome.class);

	    if (debugging)
		debug.println(3, "found AssemblyEntHome interface");

            compEntHome =
                (ComponentEntHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/ComponentEnt"),
                    ComponentEntHome.class);

	    if (debugging)
		debug.println(3, "found ComponentEntHome interface");

            buyerSesHome =
                (BuyerSesHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/BuyerSes"),
                    BuyerSesHome.class);

	    if (debugging)
		debug.println(3, "found BuyerSesHome interface");

        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up home " + e);
        }
    }

    /**
     * Method getId
     *
     *
     * @return
     *
     */
    public Integer getId() {

	if (debugging)
	    debug.println(3, "getId " + id);

        return id;
    }

    /**
     * Method getSalesId
     *
     *
     * @return
     *
     */
    public int getSalesId() {

	if (debugging)
	    debug.println(3, "getId " + id);

        return salesId;
    }

    /**
     * Method getOLineId
     *
     *
     * @return
     *
     */
    public int getOLineId() {

	if (debugging)
	    debug.println(3, "getId " + id);

        return oLineId;
    }

    /**
     * Method getStatus
     *
     *
     * @return
     *
     */
    public int getStatus() {

	if (debugging)
	    debug.println(3, "getStatus " + status);

        return status;
    }

    /**
     * Method getAssemblyId
     *
     *
     * @return
     *
     */
    public String getAssemblyId() {

	if (debugging)
	    debug.println(3, "getAssemblyId " + assemblyId);

        return assemblyId;
    }

    /**
     * Method getOrigQty
     *
     *
     * @return
     *
     */
    public int getOrigQty() {

	if (debugging)
	    debug.println(3, "origQty " + origQty);

        return origQty;
    }

    /**
     * Method getCompQty
     *
     *
     * @return
     *
     */
    public int getCompQty() {

	if (debugging)
	    debug.println(3, "getCompQty " + compQty);

        return compQty;
    }

    /**
     * Method getDueDate
     *
     *
     * @return
     *
     */
    public java.sql.Date getDueDate() {

	if (debugging)
	    debug.println(3, "getDueDate " + dueDate);

        return dueDate;
    }

    /**
     * Method getStartDate
     *
     *
     * @return
     *
     */
    public java.sql.Date getStartDate() {

	if (debugging)
	    debug.println(3, "getStartDate " + startDate);

        return new java.sql.Date(startDate.getTime());
    }

    /**
     * Lazily gets the reference of the AssemblyItem
     * only when needed.
     * @return The assembly referred to by this workorder
     */
    private AssemblyEnt getAssembly()
            throws RemoteException, FinderException {

        if (assemblyItem == null) {
            assemblyItem = assEntHome.findByPrimaryKey(this.assemblyId);
        }

        return assemblyItem;
    }

    /**
     *  This method gets a list of Bill of Materials for
     *  this object based on assemblyId.
     *  @return list of Bill of Materials (ComponentDemand objects -
     *  which is composed of componentId and item qty).
     */
    public java.util.Vector getComponentDemand() {

        try {
            String      cid;
            Enumeration boms;

	    if (debugging)
		debug.println(3, "getComponentDemand");

            boms     = getAssembly().getBoms();
            compList = new Vector();

            while (boms.hasMoreElements()) {
                BomEnt item =
                    (BomEnt) javax.rmi.PortableRemoteObject
                        .narrow(boms.nextElement(), BomEnt.class);

                cid = item.getComponentId();

                compList.addElement(new ComponentDemand(cid, item.getQty()));
            }

            return compList;
        } catch (RemoteException re) {
            throw new EJBException("Remote Exception caught" + re);
        } catch (FinderException e) {
            throw new EJBException("FinderException caught " + e);
        } catch (Exception ex) {
            throw new EJBException("Generic Exception caught " + ex);
        }
    }

    /**
     * Method getQtyToOrder
     *
     *
     * @param comp_id
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public int getQtyToOrder(String comp_id) throws RemoteException {
        // Method currently not in use. May be removed.

        try {
            ComponentEnt comp = compEntHome.findByPrimaryKey(comp_id);
            return 0;
        } catch (FinderException fe) {
            throw new EJBException(fe);
        }
    }

    /**
     *  Processes workOrder by taking components off of inventory
     *  and updates the state. If necessary, this will also create
     *  a purchase order to replenish the inventory.
     */
    public void process() {

	if (debugging)
	    debug.println(3, "process");

        try {
            int         qtyOff = 0;
            BuyerSes buyer = null;
            Enumeration boms;

            boms = getAssembly().getBoms();

            // Check to make sure BOMS list was obtained.
            if (boms == null) {
                throw new EJBException("No BOMS gotten for assemblyID "
                                       + assemblyId);
            }

	    if (debugging)
		debug.println(3, "Obtained BOM");

            // From the boms list, take the number of components
            // off the component inventory.
            for (Enumeration e = boms; e.hasMoreElements(); ) {
                BomEnt       item  =
                    (BomEnt) javax.rmi.PortableRemoteObject
                        .narrow(e.nextElement(), BomEnt.class);
                String cid = item.getComponentId();
                ComponentEnt cItem =
                    compEntHome.findByPrimaryKey(cid);

                // Based on qty from boms, multiply with 
                // requested qty (origQty)
                qtyOff = item.getQty() * origQty;

                // Check if we need to order
                int qtyRequired = cItem.getQtyRequired(qtyOff);

                if (qtyRequired > 0) {
                    if (debugging)
                        debug.println(3, "Purchase needed, contacting supplier");
                    if (buyer == null)
                        buyer = buyerSesHome.create(1);

                    cItem.addOrderedInventory(qtyRequired);
                    buyer.add(cid, qtyRequired);
                }

                // Remove from inventory
                cItem.takeInventory(qtyOff);
		if (debugging)
		    debug.println(3, "Obtain inventory");
            }

	    if (debugging)
		debug.println(3, "Inventory obtained");

            if (buyer != null) {
                if (debugging)
                    debug.println(3, "Sending order");

                buyer.purchase();

                if (debugging)
                    debug.println(3, "Order sent");
            }

            wos = wos.process();

	    if (debugging)
		debug.println(3, "Processed state");

            status = wos.getStatus();

	    if (debugging)
		debug.println(3, "Obtained status");
        } catch (ECperfException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } catch (FinderException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } catch (CreateException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        } catch (RemoteException e) {
            debug.printStackTrace(e);
            throw new EJBException(e);
        }
    }

    /**
     *  Modifies the state of the workOrder to the nextState of process.
     */
    public void update() {

        wos    = wos.nextState();
        status = wos.getStatus();

	if (debugging)
	    debug.println(3, "update Status " + status);
    }

    /**
     *  Cancels the workOrder if possible. If it is unable to
     *  cancel the workOrder because it is further down the
     *  process then an IllegalStateException is caught.
     *  Adds inventory back.
     *  @return true/false if cancel was possible or not.
     */
    public boolean cancel() {

	if (debugging)
	    debug.println(3, "cancel Status " + status);

        try {
            wos = wos.cancel();

            // Add inventory back to component list.
            int         qtyAdd;
            Enumeration boms;

            boms = getAssembly().getBoms();

            // check to make sure boms is not null
            if (boms == null) {
                return false;
            }

            // From the boms list, add the number of components
            // back to inv
            for (Enumeration e = boms; e.hasMoreElements(); ) {
                BomEnt       item  =
                    (BomEnt) javax.rmi.PortableRemoteObject
                        .narrow(e.nextElement(), BomEnt.class);
                ComponentEnt cItem =
                    compEntHome.findByPrimaryKey(item.getComponentId());

                // Based on qty from boms, multiply with 
                // requested qty (origQty) and add back to inv
                qtyAdd = item.getQty() * origQty;

                cItem.addInventory(qtyAdd);
            }

            status = wos.getStatus();

            return true;
        } catch (IllegalStateException e) {
            return false;
        } catch (FinderException e) {
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     *  When workOrder is finished, it will add the new object to
     *  inventory and modify the state of workOrder to finished.
     */
    public void finish() {

        // fulfillOrder is not yet implemented by Order domain
	if (debugging)
	    debug.println(3, "finish Status " + status);
        fulfillOrder();

        try {
            compQty = origQty;

            getAssembly().addInventory(compQty);

            wos    = wos.finish();
            status = wos.getStatus();
        } catch (FinderException fe) {
	    if (debugging)
		debug.println(1, "Unable to find assembly " + assemblyId);

            throw new EJBException(fe);
        } catch (RemoteException re) {
            throw new EJBException(re);
        }
    }

    /**
     *  Will eventually call the OrdersDomain fulfillOrder
     *  to indicate that order has been filled. Not yet implemented
     *  by OrdersDomain.
     */
    public void fulfillOrder() {}
}

