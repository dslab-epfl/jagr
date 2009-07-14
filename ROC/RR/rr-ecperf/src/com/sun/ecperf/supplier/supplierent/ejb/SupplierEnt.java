
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 * $Id: SupplierEnt.java,v 1.1.1.1 2003/04/25 08:04:22 mdelgado Exp $
 *
 */
package com.sun.ecperf.supplier.supplierent.ejb;


//Import statements
import javax.ejb.*;

import java.rmi.*;

import com.sun.ecperf.supplier.helper.*;


/**
 * Remote interface for the supplier entity bean.
 *
 * @author Damian Guy
 */
public interface SupplierEnt extends EJBObject {

    /**
     * getID - get the suppliers ID
     * @return int - id of supplier.
     * @exception RemoteException.
     */
    public int getID() throws RemoteException;

    /**
     * getPartSpec - return information about a part that
     * supplier supplies.
     * @param pID - id of part to get Spec for
     * @return PartSpec
     * @exception RemoteException
     * @exception FinderException
     */
    public PartSpec getPartSpec(String pID)
        throws RemoteException, FinderException;
}

