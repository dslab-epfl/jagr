
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * akmits@eng.sun.com 03/20/2000
 *
 * $Id: AssemblyBmpEJB.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 */
package com.sun.ecperf.mfg.assemblyent.ejb;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.*;

import java.util.*;

import com.sun.ecperf.common.*;

import java.sql.*;


/**
 * This class is the BMP version of the AssemblyEJB and must be used by
 * vendors who want to run the BMP implementation. The persistence code is
 * allowed to be modified only for non-SQL databases.
 *
 * @author Ajay Mittal
 *
 * @see AssemblyCmpEJB for more details
 * @see AssemblyBmpHelper for more details
 */
public class AssemblyBmpEJB extends AssemblyCmpEJB {

    // though we can declare and instatiate pbh in CMP class
    // because the PartCmp is base class for all BMPs and CMPs,
    // we will declare and instantiate pbh in every BMP separately
    // because declaring it PartCmp will give SQP functionality 
    // in CMPs which is not needed and may not be correct ?ajay?
    // instantiate helper class
    private AssemblyBmpHelper pbh;

    /**
     * Method ejbCreate
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
     * @return
     *
     * @throws CreateException
     * @throws RemoteException
     *
     */
    public String ejbCreate(
            String id, String name, String description, String revision, int planner, int type, int purchased, int lomark, int himark)
                throws CreateException, RemoteException {

        className = "AssemblyBmpEJB";

        // call create of CMP   
        super.ejbCreate(id, name, description, revision, planner, type,
                        purchased, lomark, himark);

        // call helper function to take care of SQL stuff
        pbh.helperEjbCreate();
        return (id);
    }

    /**
     * ejbFindByPrimaryKey
     */
    public String ejbFindByPrimaryKey(String key) throws FinderException {

	if (debugging)
	    debug.println(3, "ejbFindByPrimaryKey");

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

        pbh = new AssemblyBmpHelper(this);
    }
}

