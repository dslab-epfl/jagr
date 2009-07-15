
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: LargeOrderSesBean.java,v 1.2 2003/03/22 04:55:02 emrek Exp $
 *
 *
 */
package com.sun.ecperf.webbeans;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;

import java.io.Serializable;

import com.sun.ecperf.mfg.largeorderses.ejb.*;
import com.sun.ecperf.mfg.helper.*;
import com.sun.ecperf.common.*;

import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * This is a helper bean used by jsp files to find all large orders
 * pending.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public class LargeOrderSesBean implements Serializable {

    private final String      jndiname = "java:comp/env/ejb/LargeOrderSes";
    private LargeOrderSesHome large_orderses_home;
    private LargeOrderSes     large_orderses;
    protected Debug           debug;
    protected boolean         debugging;

    /**
     * Constructor LargeOrderSesBean
     *
     *
     * @throws OtherException
     *
     */
    public LargeOrderSesBean() throws OtherException {

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
                debug.println(3, "In constructor of LargeOrderSesBean");

            Object obj = context.lookup(jndiname);

            if (debugging) 
                debug.println(3, "Looked up " + jndiname);

            large_orderses_home =
                (LargeOrderSesHome) PortableRemoteObject.narrow(obj,
                    LargeOrderSesHome.class);
            large_orderses      = large_orderses_home.create();

            if (debugging) 
                debug.println(3, "Successfully created LargeOrderSes Bean");
        } catch (NamingException e) {
            throw new OtherException("Naming Exception in LargeOrderSesBean",
                                     e);
        } catch (ClassCastException e) {
            throw new OtherException(
                "Class cast Exception in LargeOrderSesBean", e);
        } catch (RemoteException e) {
            throw new OtherException("Remote Exception in LargeOrderSesBean",
                                     e);
        } catch (CreateException e) {
            throw new OtherException("Create Exception in LargeOrderSesBean",
                                     e);
        } catch (Exception e) {
            throw new OtherException(
                "Some Other  Exception in LargeOrderSesBean", e);
        }
    }

    /**
     * Method findLargeOrders
     *
     *
     * @return
     *
     * @throws OtherException
     *
     */
    public Vector findLargeOrders() throws OtherException {

        Vector         large_orders;
        LargeOrderInfo large_info;
        LargeOrder     large_order;
        Vector         return_large_orders;

            if (debugging) 
                debug.println(3, "In findLargeOrders method of LargeOrderSesBean ");

        try {
            large_orders = large_orderses.findLargeOrders();

            if (debugging) 
                debug.println(3, "Successfully got " + large_orders.size()
                          + " large  orders");

            return_large_orders = new Vector(large_orders.size());

            for (int i = 0; i < large_orders.size(); i++) {
                large_info  = (LargeOrderInfo) large_orders.elementAt(i);
                large_order =
                    new LargeOrder(large_info.assemblyId, large_info.qty,
                                   String.valueOf(large_info.dueDate),
                                   large_info.orderLineNumber,
                                   large_info.salesOrderId);

                return_large_orders.add(large_order);
            }
        } catch (RemoteException e) {
            throw new OtherException(
                " Remote Exception occured for the request.", e);
        }

        return return_large_orders;
    }
}

