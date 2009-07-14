
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * akmits@eng.sun.com 04/03/2000
 * @author Ajay Mittal
 *
 *
 */
package com.sun.ecperf.mfg.receiveses.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.common.*;
import com.sun.ecperf.mfg.componentent.ejb.*;

import java.sql.*;


/**
 * This class is Receivables seesion bean. This bean is stateless and
 * it has only one method addInventory () for adding components to the
 * inventory.
 *
 *
 */
public class ReceiveSesEJB implements SessionBean {

    private String           className = "ReceiveSesEJB";
    private ComponentEntHome componentHome;
    protected Debug          debug;
    protected boolean        debugging;
    
    /**
     * Method ejbCreate
     *
     *
     * @throws CreateException
     *
     */
    public void ejbCreate() throws CreateException {
	if (debugging)
	    debug.println(3, "ejbCreate");
    }

    /**
     * Method addInventory
     *
     *
     * @param compoID
     * @param numComponents
     *
     * @throws RemoteException
     *
     */
    public void addInventory(String compoID, int numComponents)
            throws RemoteException {

	if (debugging)
	    debug.println(3, "addInventory");

        try {
            ComponentEnt ce =
                (ComponentEnt) componentHome.findByPrimaryKey(compoID);

            ce.deliver(numComponents);
        } catch (FinderException e) {
            debug.printStackTrace(e);
            throw (new EJBException(e.getMessage()));
        }
    }

    /**
     * Constructor ReceiveSesEJB
     *
     *
     */
    public ReceiveSesEJB() {}

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
     * Method setSessionContext
     *
     *
     * @param sc
     *
     */
    public void setSessionContext(SessionContext sc) {

        // Set up debug level
        InitialContext initCtx;

        try {
            initCtx       = new InitialContext();
            componentHome =
                (ComponentEntHome) javax.rmi.PortableRemoteObject.narrow(
                    initCtx.lookup("java:comp/env/ejb/ComponentEnt"),
                    ComponentEntHome.class);
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

