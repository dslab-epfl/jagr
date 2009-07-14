
/*
 *  Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 *
 * akmits@eng.sun.com 03/16/2000
 *
 * $Id: PartEntHome.java,v 1.1.1.1 2002/11/16 05:35:27 emrek Exp $
 */
package com.sun.ecperf.mfg.partent.ejb;


import javax.ejb.*;

import java.rmi.RemoteException;

import java.util.*;

import com.sun.ecperf.common.*;


/**
 * This interface is the home interface for the EJBean Part.
 *
 * @author Ajay Mittal
 *
 */
public interface PartEntHome extends EJBHome {

    /**
     * This method corresponds to the ejbCreate method in the bean
     * "Part___Bean.java".
     *
     * @param id ID of the Part to uniquely identify it
     * @param name name of the part
     * @param description short description about the part
     * @param revision revision number of the part
     * @param planner planner of the part ?ajay?
     * @param type type of the part
     * @param purchased if purchased else its manufactured
     * @param lomark to indicate the low water mark in inventory
     * @param himark to indicate the hi water mark in inventory
     * @return Part
     * @exception CreateException if the create fails
     * @exception RemoteException if there is a system failure
     */
    PartEnt create(
        String id, String name, String description, String revision,
            int planner, int type, int purchased, int lomark, int himark)
                throws CreateException, RemoteException;

    PartEnt findByPrimaryKey(String id)
        throws RemoteException, FinderException;

    java.util.Enumeration findAll() throws RemoteException, FinderException;
}

