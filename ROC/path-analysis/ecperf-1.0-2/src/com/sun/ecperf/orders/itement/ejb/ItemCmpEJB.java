
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: ItemCmpEJB.java,v 1.1.1.1 2002/11/16 05:35:27 emrek Exp $
 *
 */
package com.sun.ecperf.orders.itement.ejb;


import javax.ejb.*;

import java.rmi.*;

import javax.naming.*;

import java.sql.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.orders.helper.*;


/**
 * This class implements the item entity bean.
 */
public class ItemCmpEJB implements EntityBean {

    public String           id;
    public double           price;
    public String           name;
    public String           description;
    public float            discount;
    protected Debug         debug;
    protected EntityContext entityContext;
    protected boolean       debugging;

    /**
     * Note that a new item is never actually created in ECperf, so
     * this method will never be called.
     */
    public String ejbCreate(
            String id, double price, String name, String description, float discount)
                throws CreateException {

        if(debugging)
            debug.println(3, "ejbCreate");

        this.id          = id;
        this.price       = price;
        this.name        = name;
        this.description = description;
        this.discount    = discount;

        return (this.id);
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param id
     * @param price
     * @param name
     * @param description
     * @param discount
     *
     */
    public void ejbPostCreate(String id, double price, String name,
                              String description, float discount) {}

    /**
     * Method getId
     *
     *
     * @return
     *
     */
    public String getId() {

        if(debugging)
            debug.println(3, "getId");

        return id;
    }

    /**
     * Method getPrice
     *
     *
     * @return
     *
     */
    public double getPrice() {

        if(debugging)
            debug.println(3, "getPrice ");

        return price;
    }

    /**
     * Method getName
     *
     *
     * @return
     *
     */
    public String getName() {

        if(debugging)
            debug.println(3, "getName ");

        return name;
    }

    /**
     * Method getDescription
     *
     *
     * @return
     *
     */
    public String getDescription() {

        if(debugging)
            debug.println(3, "getDescription ");

        return description;
    }

    /**
     * Method getDiscount
     *
     *
     * @return
     *
     */
    public float getDiscount() {

        if(debugging)
            debug.println(3, "getDiscount ");

        return discount;
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
     * @param ctx
     *
     */
    public void setEntityContext(javax.ejb.EntityContext ctx) {

        entityContext = ctx;

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
        if(debugging)
            debug.println(3, "unsetEntityContext ");
    }
}

