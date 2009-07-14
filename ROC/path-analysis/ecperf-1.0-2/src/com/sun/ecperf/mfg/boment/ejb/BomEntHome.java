
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.boment.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the home interface of the BOM entity bean in the Mfg
 * domain.
 */
public interface BomEntHome extends EJBHome {

    /**
     * This method corresponds to the ejbCreate method of the bean.
     * @param assemblyId  Assembly Id of bom
     * @param componentId
     * @param lineNo - Line number in BOM
     * @param qty - Quantity/Assembly
     * @param engChange - Engineering change reference
     * @param opsNo - Op# - which step in the process this is used
     * @param opsDesc - Operation description
     */
    public BomEnt create(
        String assemblyId, String componentId, int lineNo, int qty,
            int opsNo, String engChange, String opsDesc)
                throws RemoteException, CreateException;

    /**
     * Method findByPrimaryKey
     *
     *
     * @param pk
     *
     * @return
     *
     * @throws FinderException
     * @throws RemoteException
     *
     */
    public BomEnt findByPrimaryKey(BomEntPK pk)
        throws RemoteException, FinderException;

    /**
     * Method findAll
     *
     *
     * @return
     *
     * @throws FinderException
     * @throws RemoteException
     *
     */
    public java.util.Enumeration findAll()
        throws RemoteException, FinderException;

    /**
     * Method findBomForAssembly
     *
     *
     * @param assemblyId
     *
     * @return
     *
     * @throws FinderException
     * @throws RemoteException
     *
     */
    public java.util.Enumeration findBomForAssembly(String assemblyId)
        throws RemoteException, FinderException;
}

