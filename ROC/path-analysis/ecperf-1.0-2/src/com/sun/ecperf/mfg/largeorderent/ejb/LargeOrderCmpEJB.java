
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.largeorderent.ejb;


import javax.ejb.*;

import java.rmi.*;

import javax.naming.*;

import java.sql.*;

import com.sun.ecperf.util.sequenceses.ejb.*;
import com.sun.ecperf.common.*;
import com.sun.ecperf.mfg.helper.*;

/**
 * This class implements the LargeOrder Entity Bean. Container managed.
 *
 * @author Ajay Mittal
 *
 */
public class LargeOrderCmpEJB implements EntityBean {

    public Integer              id;
    public int                  salesOrderId;
    public int                  orderLineNumber;
    public String               assemblyId;
    public short                qty;
    public java.sql.Date        dueDate;
    protected EntityContext     entityContext;
    protected SequenceSesHome   sequenceHome;
    private static final String className = "LargeOrderCmpEJB";
    protected Debug             debug;
    protected boolean		debugging;
    protected int		debugLevel;
    /**
     * Constructs the LargeOrder object
     * @param salesOrderId the id of sales order that caused this wo to be created
     * @param orderLineNumber line (row) number in salesOrder identified by salesOrderId
     * @param assemblyId assembly that is going to be manufactured
     * @param qty number of assemblies to be manufactured by this wo
     * @param dueDate date when this order is due
     */
    public Integer ejbCreate(
            int salesOrderId, int orderLineNumber, String assemblyId, short qty, java
                .sql.Date dueDate) throws CreateException {

	if (debugging)
	    debug.println(3, "ejbCreate ");

        try {
            SequenceSes sequence = sequenceHome.create();

            this.id = new Integer(sequence.nextKey("largeorder"));
	    // For Atomicity Test 3
	    if (debugging)
		debug.println(4, "Atomicity Test 3: OrderId " + this.id + " OrderLineId: " + orderLineNumber);
	    
        } catch (CreateException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (RemoteException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        } catch (FinderException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }

        this.salesOrderId    = salesOrderId;
        this.orderLineNumber = orderLineNumber;
        this.assemblyId      = assemblyId;
        this.qty             = qty;
        this.dueDate         = dueDate;

        return null;
    }

    // Container managed methods

    /**
     * Method ejbPostCreate
     *
     *
     * @param salesOrderId
     * @param orderLineNumber
     * @param assemblyId
     * @param qty
     * @param dueDate
     *
     */
    public void ejbPostCreate(int salesOrderId, int orderLineNumber,
                              String assemblyId, short qty,
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
    public void ejbActivate() {}

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
    public void ejbLoad() {}

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {
	// For Atomicity Test 3 as specified in Clause 4.11.3 of ECPerf Spec
	if (debugLevel == 4) {
	    debug.println(4, "Atomicity Test 3: Rolling back large order");
	    entityContext.setRollbackOnly();
	}
	
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
        } catch (NamingException e) {
            e.printStackTrace(System.err);

            throw new EJBException(e);
        }

        try {
            debugLevel =
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
            System.out.println("LargeOrderCmpEJB: debuglevel Property "
                               + "not set. Turning off debug messages");

            debug = new Debug();
        }

        this.entityContext = entityContext;

        try {
            sequenceHome =
                (SequenceSesHome) javax.rmi.PortableRemoteObject.narrow(
                    context.lookup("java:comp/env/ejb/SequenceSes"),
                    SequenceSesHome.class);

	    if (debugging)
		debug.println(3, "found SequenceSesHome interface");
        } catch (NamingException e) {
            debug.printStackTrace(e);

            throw new EJBException("Failure looking up home " + e);
        }
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
     * @return id id of the Large Order EJB
     */
    public Integer getId() {
        return (id);
    }

    /**
     * @return salesOrderId Sales Order ID of the Large Order EJB
     */
    public int getSalesOrderId() {
        return (salesOrderId);
    }

    /**
     * @return orderLineNumber line (row) number in the Sales Order
     */
    public int getOrderLineNumber() {
        return (orderLineNumber);
    }

    /**
     * @return assemblyId Id of the assembly being manufactured
     */
    public String getAssemblyId() {
        return (assemblyId);
    }

    /**
     * @return qty quantity of the assembly being manufactured
     */
    public short getQty() {
        return (qty);
    }

    /**
     * @return dueDate Due date for assembly being manufactured
     */
    public java.sql.Date getDueDate() {
        return (dueDate);
    }

    /**
     * @return LargeOrderInfo assembly being manufactured
     */
    public LargeOrderInfo getLargeOrderInfo() {

        LargeOrderInfo loi = new LargeOrderInfo();

        loi.id              = id;
        loi.salesOrderId    = salesOrderId;
        loi.orderLineNumber = orderLineNumber;
        loi.assemblyId      = assemblyId;
        loi.qty             = qty;
        loi.dueDate         = dueDate;

        return loi;
    }
}

