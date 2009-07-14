
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: SupplierCompCmpEJB.java,v 1.1.1.1 2002/11/16 05:35:30 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.suppliercompent.ejb;


// Import statements
import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import java.sql.*;

import com.sun.ecperf.common.*;


/**
 * THis is the CMP version of the SupplierComp Entity Bean
 *
 *
 * @author Damian Guy
 */
public class SupplierCompCmpEJB implements EntityBean {

    public String           suppCompID;
    public int              suppCompSuppID;
    public double           suppCompPrice;
    public int              suppCompQty;
    public double           suppCompDiscount;
    public int              suppCompDelDate;
    protected EntityContext entityContext;
    protected Debug         debug;
    protected boolean       debugging;

    // SupplierCompEnt methods 

    /**
     * Method getPrice
     *
     *
     * @return
     *
     */
    public double getPrice() {
        return suppCompPrice;
    }

    /**
     * Method getDiscount
     *
     *
     * @return
     *
     */
    public double getDiscount() {
        return suppCompDiscount;
    }

    /**
     * Method getDeliveryDate
     *
     *
     * @return
     *
     */
    public int getDeliveryDate() {
        return suppCompDelDate;
    }

    /**
     * Method getQuantity
     *
     *
     * @return
     *
     */
    public int getQuantity() {
        return suppCompQty;
    }

    /**
     * Method getDiscountedPrice
     *
     *
     * @return
     *
     */
    public double getDiscountedPrice() {
        return (1 - suppCompDiscount) * suppCompPrice;
    }

    // ejbXXXX methods

    /**
     * ejbCreate: Corresponds to create in the Home interface.
     * @param suppCompID - part number.
     * @param suppCompSuppID - supplier id.
     * @param suppCompPrice - price of supplied qty (suppCompQty).
     * @param suppCompQty - quantity that is supplied.
     * @param suppCompDiscount - discount the applies.
     * @param suppCompDelDate - probably should be lead time.
     * @return SuppCompEntPK - primary Key for this object (suppCompID + suppCompSuppID).
     * @exception CreateException - if there is a create failure.
     */
    public SuppCompEntPK ejbCreate(
            String suppCompID, int suppCompSuppID, double suppCompPrice, int suppCompQty, double suppCompDiscount, int suppCompDelDate)
                throws CreateException {

        if (debugging)
            debug.println(3, "ejbCreate");

        this.suppCompID     = suppCompID;
        this.suppCompSuppID  = suppCompSuppID;
        this.suppCompPrice    = suppCompPrice;
        this.suppCompQty      = suppCompQty;
        this.suppCompDiscount = suppCompDiscount;
        this.suppCompDelDate = suppCompDelDate;

        return null;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param suppCompID
     * @param suppCompSuppID
     * @param suppCompPrice
     * @param suppCompQty
     * @param suppCompDiscount
     * @param suppCompDelDate
     *
     */
    public void ejbPostCreate(String suppCompID, int suppCompSuppID,
                              double suppCompPrice, int suppCompQty,
                              double suppCompDiscount, int suppCompDelDate) {}

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {
        if (debugging)
            debug.println(3, "ejbRemove");
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {
        if (debugging)
            debug.println(3, "ejbActivate");
    }

    /**
     * Method ejbPassivate
     *
     *
     */
    public void ejbPassivate() {
        if (debugging)
            debug.println(3, "ejbPassivate");
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {
        if (debugging)
            debug.println(3, "ejbLoad");
    }

    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {
        if (debugging)
            debug.println(3, "ejbStore");
    }

    /**
     * Method setEntityContext
     *
     *
     * @param ec
     *
     */
    public void setEntityContext(EntityContext ec) {

        try {
            Context ic         = new InitialContext();
            int     debugLevel =
                ((Integer) ic.lookup("java:comp/env/debuglevel")).intValue();

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

        this.entityContext = ec;
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {
        entityContext = null;
    }
}

