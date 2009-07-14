
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: POLineEntHome.java,v 1.1.1.1 2002/11/16 05:35:30 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.polineent.ejb;


//Import statements
import javax.ejb.*;

import java.rmi.*;

import java.util.*;


/**
 * This is the home interface for the POLine Entity bean.
 *
 * @author Damian Guy
 */
public interface POLineEntHome extends EJBHome {

    /**
     * create - create a POLine
     * @param poLineNumber
     * @param poID
     * @param pID
     * @param qty
     * @param balance
     * @param leadTime
     * @param message
     * @return POLineEnt
     * @exception RemoteException - if there is a system failure.
     * @exception CreateException - if there is a create failure.
     */
    public POLineEnt create(
        int poLineNumber, int poID, String pID, int qty, double balance,
            int leadTime, String message)
                throws RemoteException, CreateException;

    /**
     * findByPrimaryKey - find the POLIne that matches key.
     * @param key - Key of POLineEnt to find.
     * @return POLineEnt
     * @exception RemoteException - if there is a system failure.
     * @exception FinderException - if there is a find exception.
     */
    public POLineEnt findByPrimaryKey(POLineEntPK key)
        throws RemoteException, FinderException;

    /**
     * findByPO - find all of the PO lines for a
     * given Purchase Order.
     * @param poID - id of the purchase order
     * @return Collection
     * @exception RemoteException - if there is a system failure.
     * @exception FinderException - if there are not any order lines for poID
     */
    public Collection findByPO(int poID)
        throws RemoteException, FinderException;
}

