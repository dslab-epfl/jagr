
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: DiscountCmpEJB.java,v 1.1 2004/02/19 14:45:07 emrek Exp $
 *
 *
 */
package com.sun.ecperf.corp.discountent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import java.io.*;

//import mfg.interfaces.*;
import com.sun.ecperf.common.*;


/**
 * This class implements the Order entity bean. It is
 * responsible for performing all the transactions in the Orders
 * application. It also manages the orderlines that are part of the
 * order.
 */
public class DiscountCmpEJB implements EntityBean {

    public String            id;
    public int               percent;
    protected EntityContext  entCtx;
    protected InitialContext initCtx;
    protected Debug          debug;
    protected boolean        debugging;
   protected boolean isDirty = true;

    /**
     * Method ejbCreate
     *
     *
     * @param id
     * @param percent
     *
     * @return
     *
     * @throws CreateException
     *
     */
    public String ejbCreate(String id, double percent)
            throws CreateException {

        if (debugging)
            debug.println(3, "ejbCreate ");

        this.id      = id;
        this.percent = (new Double(percent * 100)).intValue();

        isDirty = false;
        return id;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param id
     * @param percent
     *
     */
    public void ejbPostCreate(String id, double percent) {}

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
     * Method getId
     *
     *
     * @return
     *
     */
    public String getId() {
        return id;
    }

    /**
     * Method getPercent
     *
     *
     * @return
     *
     */
    public double getPercent() {
        return percent/100.00;
    }

    /**
     * Method setPercent
     *
     *
     * @param percent
     *
     */
    public void setPercent(double percent) {
        this.percent = (new Double(percent * 100)).intValue();
        isDirty = true;
    }

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
     * Method setEntityContext
     *
     *
     * @param entCtx
     *
     */
    public void setEntityContext(EntityContext entCtx) {

        this.entCtx = entCtx;

        try {
            initCtx = new InitialContext();

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
            System.out.println("RuleCmpEJB: debuglevel Property not set."
                               + "Turning off debug messages");

            debug = new Debug();
        }
    }

    /**
     * Method unsetEntityContext
     *
     *
     */
    public void unsetEntityContext() {
        if (debugging)
            debug.println(3, "unsetEntityContext ");
    }


   public boolean isModified()
   {
      return isDirty;
   }
}

