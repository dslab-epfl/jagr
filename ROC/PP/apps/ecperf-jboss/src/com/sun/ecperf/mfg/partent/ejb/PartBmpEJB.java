
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * akmits@eng.sun.com 03/17/2000
 *
 * $Id: PartBmpEJB.java,v 1.1 2004/02/19 14:45:12 emrek Exp $
 */
package com.sun.ecperf.mfg.partent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.common.*;

import java.sql.*;


/**
 * This class is the BMP version of the PartEJB and must be used by
 * vendors who want to run the BMP implementation.
 *
 * @author Ajay Mittal
 *
 * @see PartCmpEJB for more details
 * @see PartBmpHelper for more details
 */
public class PartBmpEJB extends PartCmpEJB {

    // though we can declare and instatiate pbh in CMP class
    // because the PartCmp is base class for all BMPs and CMPs,
    // we will declare and instantiate pbh in every BMP separately
    // because declaring it in PartCmp will give SQP functionality 
    // in CMPs which is not needed and may not be correct ?ajay?
    private PartBmpHelper pbh;

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
     * @return String
     * @exception CreateException if the create fails
     * @exception RemoteException if there is a system failure
     */
    public String ejbCreate(
            String id, String name, String description, String revision, int planner, int type, int purchased, int lomark, int himark)
                throws CreateException, RemoteException {

        className = "PartBmpEJB";

        super.ejbCreate(id, name, description, revision, planner, type,
                        purchased, lomark, himark);

        // call helper function to take care of SQL stuff
        return pbh.helperEjbCreate();
    }

    /**
     * ejbFindByPrimaryKey
     */
    public String ejbFindByPrimaryKey(String key) throws FinderException {

	if (debugging)
	    debug.println(3, "ejbFindByPrimaryKey: " + key);

        return pbh.helperEjbFindByPrimaryKey(key);
    }

    /**
     * ejbFindAll
     */
    public java.util.Enumeration ejbFindAll() throws FinderException {

	if (debugging)
	    debug.println(3, "ejbFindAll");

        return pbh.helperEjbFindAll();
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
	    debug.println(3, "ejbRemove");
        super.ejbRemove();
        pbh.helperEjbRemove();
    }

    /**
     * Method ejbActivate
     *
     *
     */
    public void ejbActivate() {

	if (debugging)
	    debug.println(3, "ejbActivate");
        pbh.helperEjbActivate();
        super.ejbActivate();
    }

    /**
     * Method ejbLoad
     *
     *
     */
    public void ejbLoad() {

	if (debugging)
	    debug.println(3, "ejbLoad ");
        pbh.helperEjbLoad();
        super.ejbLoad();
    }


    /**
     * Method ejbStore
     *
     *
     */
    public void ejbStore() {

	if (debugging)
	    debug.println(3, "ejbStore ");
        super.ejbStore();
        pbh.helperEjbStore();
    }

    /**
     * Method setEntityContext
     *
     *
     * @param entityContext
     *
     */
    public void setEntityContext(EntityContext entityContext) {

        super.setEntityContext(entityContext);

        pbh = new PartBmpHelper(this);
    }
}

