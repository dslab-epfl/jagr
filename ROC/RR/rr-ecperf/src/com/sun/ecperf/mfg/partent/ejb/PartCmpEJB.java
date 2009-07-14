
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * akmits@eng.sun.com 03/16/2000
 *
 * $Id: PartCmpEJB.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 */
package com.sun.ecperf.mfg.partent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.common.*;


/**
 * This class implements the Part entity bean. It is
 * responsible for performing all the transactions on the Parts
 *
 * @author Ajay Mittal
 *
 */
public class PartCmpEJB implements EntityBean {

    public String id;
    public String name;
    public String description;
    public String revision;
    public int    planner;
    public int    type;
    public int    purchased;
    public int    lomark;
    public int    himark;

    /****
    public String unit;
    public float cost;
    public float price;
    ***/
    protected EntityContext entityContext;
    protected String        className = "PartCmpEJB";
    public Debug            debug;    // need to make public for helper classes
    public boolean	    debugging;
   protected boolean isDirty = true;
    /**
     * The ejbCreate method gets called when a new part needs to
     * be created.
     * @param id ID of the Part to uniquely identify it
     * @param name name of the part
     * @param description short description about the part
     * @param revision revision number of the part
     * @param planner planner of the part ?ajay?
     * @param type type of the part
     * @param purchased if purchased else its manufactured
     * @param lomark to indicate the low water mark in inventory
     * @param himark to indicate the hi water mark in inventory
     * @return String part id
     * @exception CreateException if the create fails
     * @exception RemoteException if there is a system failure
     */
    public String ejbCreate(
            String id, String name, String description, String revision, int planner, int type, int purchased, int lomark, int himark)

    /* ? ajay: Currency ? */
    throws CreateException, RemoteException {

	if (debugging) {
	    debug.println(3, "ejbCreate ");
	    debug.println(2, "ejbCreate call unexpected !");
	}
	
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.planner     = planner;
        this.type        = type;
        this.purchased   = purchased;
        this.lomark      = lomark;
        this.himark      = himark;
        isDirty = false;
        return id;
    }

    /**
     * Method ejbPostCreate
     *
     *
     * @param id
     * @param name
     * @param description
     * @param revision
     * @param planner
     * @param type
     * @param purchased
     * @param lomark
     * @param himark
     *
     */
    public void ejbPostCreate(String id, String name, String description,
                              String revision, int planner, int type,
                              int purchased, int lomark, int himark) {
	if (debugging)
	    debug.println(3, "ejbPostCreate");
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
     * Method getName
     *
     *
     * @return
     *
     */
    public String getName() {
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
        return description;
    }

    /**
     * Method getRevision
     *
     *
     * @return
     *
     */
    public String getRevision() {
        return revision;
    }

    /**
     * Method getPlanner
     *
     *
     * @return
     *
     */
    public int getPlanner() {
        return planner;
    }

    /**
     * Method getType
     *
     *
     * @return
     *
     */
    public int getType() {
        return type;
    }

    /**
     * Method getLomark
     *
     *
     * @return
     *
     */
    public int getLomark() {
        return lomark;
    }

    /**
     * Method getHimark
     *
     *
     * @return
     *
     */
    public int getHimark() {
        return himark;
    }

    /**
     * Method getPurchased
     *
     *
     * @return
     *
     */
    public int getPurchased() {
        return purchased;
    }

    /**
     * Method ejbRemove
     *
     *
     * @throws RemoveException
     *
     */
    public void ejbRemove() throws RemoveException {
	if (debugging)
	    debug.println(3, "ejbRemove ");
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
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

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
                className
                + ":debuglevel Property not set. Turning off debug messages");

            debug = new Debug();
        }

	if (debugging)
	    debug.println(3, "part:setEntityContext");

        this.entityContext = entityContext;
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

   public boolean isModified() { return isDirty; }
}

