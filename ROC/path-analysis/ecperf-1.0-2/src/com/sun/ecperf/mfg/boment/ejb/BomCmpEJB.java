
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.boment.ejb;


import javax.ejb.*;

import java.rmi.*;

import javax.naming.*;

import java.sql.*;

import com.sun.ecperf.common.*;


/**
 * This class implements the Bom Entity Bean. Container managed.
 *
 * @author Agnes Jacob
 *
 */
public class BomCmpEJB implements EntityBean {

    protected EntityContext     entityContext;
    public String               assemblyId;
    public String               componentId;
    public int                  lineNo;
    public int                  qty;
    public String               engChange;
    public int                  opsNo;
    public String               opsDesc;
    public BomEntPK		pkey;
    protected Debug             debug;
    protected boolean		debugging;
    private static final String className = "BomCmpEJB";

    /**
     * Constructs the BOM object (Container managed)
     * and container stores the information into the DB.
     * @param assemblyId    Assembly Id of bom
     * @param componentId
     * @param lineNo        Line No
     * @param qty
     * @param engChange - Engineering change reference
     * @param opsNo - Op# - which step in the process this is used
     * @param opsDesc - Operation description
     * @return primary key of BOM which is composed of componentId,
     *              assemblyId, and lineNo (BomEntPK).
     */
    public BomEntPK ejbCreate(
            String assemblyId, String componentId, int lineNo, int qty, int opsNo, String engChange, String opsDesc)
                throws RemoteException, CreateException {

	if (debugging)
	    debug.println(3, "ejbCreate");

        this.assemblyId  = assemblyId;
        this.componentId = componentId;
        this.lineNo      = lineNo;
        this.qty         = qty;
        this.engChange   = engChange;
        this.opsNo       = opsNo;
        this.opsDesc     = opsDesc;
	this.pkey = new BomEntPK(assemblyId, componentId, lineNo);
        return ( pkey );
    }

    /**
     * Container calls the ejbPostCreate method after an ejbCreate
     */
    public void ejbPostCreate(String assemblyId, String componentId,
                              int lineNo, int qty, int opsNo,
                              String engChange, String opsDesc) {}

    /**
     * Container managed methods.
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
    public void ejbStore() {}

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
     * Get methods for the instance methods
     */
    public String getAssemblyId() {

	if (debugging)
	    debug.println(3, "getAssemblyId " + assemblyId);

        return assemblyId;
    }

    /**
     * Method getComponentId
     *
     *
     * @return
     *
     */
    public String getComponentId() {

	if (debugging)
	    debug.println(3, "getComponentId " + componentId);

        return componentId;
    }

    /**
     * Method getLineNo
     *
     *
     * @return
     *
     */
    public int getLineNo() {

	if (debugging)
	    debug.println(3, "getLineNo " + lineNo);

        return lineNo;
    }

    /**
     * Method getQty
     *
     *
     * @return
     *
     */
    public int getQty() {

	if (debugging)
	    debug.println(3, "getQty " + qty);

        return qty;
    }

    /**
     * Method getEngChange
     *
     *
     * @return
     *
     */
    public String getEngChange() {

	if (debugging)
	    debug.println(3, "getEngChange " + engChange);

        return engChange;
    }

    /**
     * Method getOpsNo
     *
     *
     * @return
     *
     */
    public int getOpsNo() {

	if (debugging)
	    debug.println(3, "getOpsNo " + opsNo);

        return opsNo;
    }

    /**
     * Method getOpsDesc
     *
     *
     * @return
     *
     */
    public String getOpsDesc() {

	if (debugging)
	    debug.println(3, "opsDesc" + opsDesc);

        return opsDesc;
    }
}

