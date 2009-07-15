
/*
 *
 * Copyright (c) 1999-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */
package com.sun.ecperf.mfg.largeorderses.ejb;


import java.rmi.RemoteException;

import javax.ejb.*;


/**
 * This interface is the home interface for the LargeOrder
 * session bean. This bean is stateless.
 * @author Agnes Jacob
 */
public interface LargeOrderSesHome extends EJBHome {

    // since it is a stateless bean. Only create that is needed
    // is one without the args

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
    public LargeOrderSes create() throws RemoteException, CreateException;
}

