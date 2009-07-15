
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: POEntHome.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.poent.ejb;


//Import statements
import javax.ejb.*;

import java.rmi.*;

import com.sun.ecperf.supplier.helper.*;


/**
 * This is the Home interface for the Purchase Order Entity Bean.
 *
 * @author Damian Guy
 */
public interface POEntHome extends EJBHome {

    /**
     * create: create a new Purchase Order.
     * @param suppID - id of supplier.
     * @param siteID - site id of Mfg.
     * @param orders - Array of Objects containing qty + pricing information for components.
     * @return POEnt
     * @exception RemoteException - if there is a system failure.
     * @exception CreateException - if there is a create failure.
     */
    public POEnt create(int suppID, int siteID, ComponentOrder[] orders)
        throws RemoteException, CreateException;

    /**
     * findByPrimaryKey: find the PO that is identified by pk.
     * @param pk - find PO with the primary key.
     * @return POEnt.
     * @exception RemoteException - if there is a system failure.
     * @exception FinderException - if cannot find PO.
     */
    public POEnt findByPrimaryKey(Integer pk)
        throws RemoteException, FinderException;
}

