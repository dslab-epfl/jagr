
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id:
 *
 */
package com.sun.ecperf.supplier.buyerses.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the Home interface to the BuyerSession bean.
 */
public interface BuyerSesHome extends EJBHome {

    /**
     * corresponds to ejbCreate method.
     * @param siteID - site that components should be delivered to.
     *
     * @return
     * @exception RemoteException - if there is a system failure.
     * @exception CreateException - if there is a create failure.
     */
    public BuyerSes create(int siteID)
        throws RemoteException, CreateException;
}

