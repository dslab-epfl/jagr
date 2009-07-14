
/*
 *
 * Copyright (c) 1999-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 * @author Ajay Mittal
 *
 * akmits@eng.sun.com 04/03/2000
 */
package com.sun.ecperf.mfg.receiveses.ejb;


import javax.ejb.EJBObject;

import java.rmi.RemoteException;


/**
 * This interface is the remote interface for the ReceiveSes
 * session bean. This bean is stateless.
 *
 * @author Ajay Mittal
 *
 *
 */
public interface ReceiveSes extends EJBObject {

    /**
      * Method to add components to the inventory.
      * @param compoID unique component ID
      * @param numComponents number of components to be added
      * @exception RemoteException if there is a system failure
      */
    public void addInventory(String compoID, int numComponents)
        throws RemoteException;
}

