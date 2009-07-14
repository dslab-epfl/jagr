
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id:
 *
 */
package com.sun.ecperf.orders.orderlineent.ejb;


import javax.ejb.*;

import java.rmi.*;

import javax.naming.*;

import com.sun.ecperf.orders.helper.*;
import com.sun.ecperf.common.*;


/**
 * OrderLineBean is an entity bean that represents an orderline row
 * in the orders table of the Customer Domain. This class is the CMP version.
 * @see OrderLineBMP.java for the BMP version of the bean.
 */
public class OrderLineCmpEJB implements EntityBean {

    public int              id;
    public int              orderId;
    public String           itemId;
    public int              quantity;
    public java.sql.Date    shipDate;
    protected Debug         debug;
    protected boolean       debugging;
    protected EntityContext entityContext;

    /**
     * This method corresponds to the EJBCreate method in the bean.
     * @param id - Id of orderline
     * @param orderId - Id of order to which this belongs
     * @param itemId - Id of item being ordered
     * @param quantity - Quantity of item
     * @param shipDate - Required ship date for this orderline
     * @return OrderLine
     * @exception CreateException - if the create fails
     */
    public OrderLineEntPK ejbCreate(
            int id, int orderId, String itemId, int quantity, java.sql
                .Date shipDate) throws CreateException {

        if(debugging)
            debug.println(3, "ejbCreate of ol_id = " + id + ", o_id = "
                      + orderId);

        this.id       = id;
        this.orderId  = orderId;
        this.itemId   = itemId;
        this.quantity = quantity;
        this.shipDate = shipDate;

        return null;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param id
     * @param orderId
     * @param itemId
     * @param quantity
     * @param shipDate
     *
     */
    public void ejbPostCreate(int id, int orderId, String itemId,
                              int quantity, java.sql.Date shipDate) {
        if(debugging)
            debug.println(3, "ejbPostCreate");
    }

    /**
     * Method getItemId
     *
     *
     * @return
     *
     */
    public String getItemId() {

        if(debugging)
            debug.println(3, "getItemId ");

        return itemId;
    }

    /**
     * Method getQuantity
     *
     *
     * @return
     *
     */
    public int getQuantity() {

        if(debugging)
            debug.println(3, "getQuantity ");

        return quantity;
    }

    /**
     * Method setQuantity
     *
     *
     * @param quantity
     *
     */
    public void setQuantity(int quantity) {

        if(debugging)
            debug.println(3, "setQuantity of ol_id " + id + " to " + quantity);

        this.quantity = quantity;
    }

    /**
     * Method getShipDate
     *
     *
     * @return
     *
     */
    public java.sql.Date getShipDate() {

        if(debugging)
            debug.println(3, "getShipDate ");

        return shipDate;
    }

    /**
     * Method setShipDate
     *
     *
     * @param date
     *
     */
    public void setShipDate(java.sql.Date date) {
        this.shipDate = shipDate;
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {
        if(debugging)
            debug.println(3, "ejbRemove ");
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        if(debugging)
            debug.println(3, "ejbActivate ");
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
        if(debugging)
            debug.println(3, "ejbPassivate ");
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {
        if(debugging)
            debug.println(3, "ejbLoad ");
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {
        if(debugging)
            debug.println(3, "ejbStore ");
    }

    /**
     * Method setEntityContext
     *
     *
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        this.entityContext = entityContext;

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
                "OrderLineCmpEJB: debuglevel Property not set. Turning off debug messages");

            debug = new Debug();
        }
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {}
}

