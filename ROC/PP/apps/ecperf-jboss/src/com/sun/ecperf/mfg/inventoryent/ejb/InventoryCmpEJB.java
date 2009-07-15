
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.inventoryent.ejb;


import javax.ejb.*;

import java.rmi.*;

import javax.naming.*;

import java.sql.*;

import com.sun.ecperf.common.*;


/**
 * This class implements the Inventory Entity Bean. Container managed.
 *
 * @author Agnes Jacob
 *
 */
public class InventoryCmpEJB implements EntityBean {

    protected EntityContext     entityContext;
    public String               partId;
    public int                  qty;
    public int                  in_ordered;
    public String               location;
    public int                  accCode;
    public Date                 accDate;
    protected Debug             debug;
    protected boolean		debugging;
   protected boolean isDirty = true;
    private static final String className = "InventoryCmpEJB";

    /**
     * Constructs the Inventory object
     * and stores the information into the DB.
     *
     * @param partId
     * @param qty           Quantity
     * @param in_ordered
     * @param location      Warehouse location
     * @param accCode       Account Finance Code
     * @param accDate       Date/time of last activity
     * @return primary key which is the partId. (String)
     */
    public String ejbCreate(
            String partId, int qty, int in_ordered, String location, int accCode, Date accDate)
                throws RemoteException, CreateException {

	if (debugging)
	    debug.println(3, "ejbCreate");

        this.partId     = partId;
        this.qty        = qty;
        this.in_ordered = in_ordered;
        this.location   = location;
        this.accCode    = accCode;
        this.accDate    = accDate;
        isDirty = false;
        return (partId);
    }

    // Container managed methods

    /**
     * Method ejbPostCreate
     *
     *
     * @param partId
     * @param qty
     * @param in_ordered
     * @param location
     * @param accCode
     * @param accDate
     *
     */
    public void ejbPostCreate(String partId, int qty, int in_ordered,
                              String location, int accCode, Date accDate) {}

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
    public void ejbLoad() 
   {
        isDirty = false;
   }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() 
   {
        isDirty = false;
   }

    /**
     * Method setEntityContext
     *
     *
     * @param ctx
     *
     */
    public void setEntityContext(EntityContext ctx) {

        entityContext = ctx;

        // Set up debug level
        try {
            InitialContext initCtx    = new InitialContext();
            int            debugLevel =
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
            System.out.println(
                "ItemCmpEJB: debuglevel Property not set. Turning off debug messages");

            debug = new Debug();
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
     * Updates the quantity of inventory.  Occurs when an item
     * has been completed
     *
     * @param qty   Number of items to be added.
     */
    public void add(int qty) {

	if (debugging)
	    debug.println(3, "add");

        this.qty = this.qty + qty;
        isDirty = true;
    }

    /**
     * Deletes the specified quantity of inventory.  Occurs when an item
     * has been ordered/shipped.
     *
     * @param qty   Number of items to be removed.
     */
    public void take(int qty) {
        this.qty = this.qty - qty;
        isDirty = true;
    }

    /**
     * Returns the qty value of this object
     *
     * @return the number of items for this object
     */
    public int getOnHand() {
        return this.qty;
    }

    /**
     * Added by DG
     * Get the number on ordered
     */
    public int getOrdered() {
        return this.in_ordered;
    }

    /**
     * Added by DG
     * Add to the number on order.
     */
    public void addOrdered(int qty) {
        this.in_ordered += qty;
        isDirty = true;
    }

    /**
     * Added by DG
     * Subtract from the number ordered.
     */
    public void takeOrdered(int qty) {

        int tmp = in_ordered - qty;

        in_ordered = (tmp < 0)
                     ? 0
                     : tmp;
        isDirty = true;
    }

    // get methods for the instance variables

    /**
     * Method getPartId
     *
     *
     * @return
     *
     */
    public String getPartId() {
        return partId;
    }

    /**
     * Method getQty
     *
     *
     * @return
     *
     */
    public int getQty() {
        return qty;
    }

    /**
     * Method getLocation
     *
     *
     * @return
     *
     */
    public String getLocation() {
        return location;
    }

    /**
     * Method getAccCode
     *
     *
     * @return
     *
     */
    public int getAccCode() {
        return accCode;
    }

    /**
     * Method getAccDate
     *
     *
     * @return
     *
     */
    public java.sql.Date getAccDate() {
        return accDate;
    }

   public boolean isModified()
   {
      return isDirty;
   }
}

