
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */
package com.sun.ecperf.mfg.largeorderses.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.mfg.largeorderent.ejb.*;
import com.sun.ecperf.common.*;

import java.sql.*;


/**
 * This class is Large Order session bean. This bean is stateless
 * and handles the creation and search of Large Orders.
 * @author Agnes Jacob
 */
public class LargeOrderSesEJB implements SessionBean {

    private String            className = "LargeOrderSesEJB";
    private LargeOrderEntHome largeEntHome;
    protected Debug           debug;
    protected boolean	      debugging;
    
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

    /**
     * Creates a LargeOrder request
     * @param orderId       Sales order id
     * @param oLineId       Order line number id
     * @param assemblyId    AssemblyId
     * @param qty           number of assemblies to be manufactured
     * @param dueDate       Date when order is due.
     */
    public Integer createLargeOrder(int orderId, int oLineId,
                                    String assemblyId, short qty,
                                    java.sql.Date dueDate) {

	if (debugging)
	    debug.println(3, "createLargeOrder ");

        LargeOrderEnt loe;
        Integer       loId = null;

        try {

            // Get sequence key from DB; it is generated there
            loe  = largeEntHome.create(orderId, oLineId, assemblyId, qty,
                                       dueDate);
            loId = loe.getId();
        } catch (CreateException e) {
	    if (debugging)
		debug.println(1, "Create exception ");
            debug.printStackTrace(e);

            throw new EJBException("Unable to create large order" + e);
        } catch (RemoteException e) {
            debug.printStackTrace(e);

            throw new EJBException(e);
        }

        return loId;
    }

    /**
     * Find all LargeOrders on the Database
     * @return List of LargeOrderEnt found and removed.
     */
    public java.util.Vector findLargeOrders() {

	if (debugging)
	    debug.println(3, "findLargeOrders ");

        Vector lov = new Vector();

        try {
            Enumeration orders = largeEntHome.findAll();

            while (orders.hasMoreElements()) {
                LargeOrderEnt item =
                    (LargeOrderEnt) javax.rmi.PortableRemoteObject
                        .narrow(orders.nextElement(), LargeOrderEnt.class);

                lov.addElement(item.getLargeOrderInfo());
            }

            return lov;
        } catch (FinderException e) {
            throw new EJBException("Unable to find any orders");
        } catch (RemoteException re) {
            throw new EJBException(re);
        }
    }

    /**
     * Constructor LargeOrderSesEJB
     *
     *
     */
    public LargeOrderSesEJB() {}

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
     * Sets the session context. Looks up context of LargeOrderEntity Bean
     */
    public void setSessionContext(SessionContext sc) {

        InitialContext initCtx;

        try {
            initCtx      = new InitialContext();
            largeEntHome =
                (LargeOrderEntHome) javax.rmi.PortableRemoteObject.narrow(
                    initCtx.lookup("java:comp/env/ejb/LargeOrderEnt"),
                    LargeOrderEntHome.class);
        } catch (NamingException e) {
            e.printStackTrace(System.err);
            throw (new EJBException(e.getMessage()));
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
            System.out.println(
                className
                + ":debuglevel Property not set. Turning off debug messages");

            debug = new Debug();
        }

	if (debugging)
	    debug.println(3, "setSessionContext");
    }
}

