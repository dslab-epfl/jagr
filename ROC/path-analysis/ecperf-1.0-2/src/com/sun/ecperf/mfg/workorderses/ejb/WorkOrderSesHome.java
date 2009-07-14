
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
package com.sun.ecperf.mfg.workorderses.ejb;


import java.rmi.RemoteException;

import javax.ejb.*;


/**
 * This interface is the home interface for the WorkOrder
 * session bean. This bean is stateless
 *
 * @author Ajay Mittal
 *
 *
 */
public interface WorkOrderSesHome extends EJBHome {

    /**
     * Method create
     *
     *
     * @return
     *
     * @throws CreateException
     * @throws RemoteException
     *
     */
    public WorkOrderSes create() throws RemoteException, CreateException;
}

