
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


import java.rmi.RemoteException;

import javax.ejb.*;


/**
 * This interface is the home interface for the ReceiveSes
 * session bean. This bean is stateless.
 *
 * @author Ajay Mittal
 *
 *
 */
public interface ReceiveSesHome extends EJBHome {

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
    public ReceiveSes create() throws RemoteException, CreateException;
}

