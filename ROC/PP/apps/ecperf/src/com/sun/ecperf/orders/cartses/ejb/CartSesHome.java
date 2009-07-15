
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id:
 *
 */
package com.sun.ecperf.orders.cartses.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the Home interface to the CartSession bean.
 *
 * Venkata Yella - yella@eng.sun.com
 *
 */
public interface CartSesHome extends EJBHome {

    /**
     * corresponds to ejbCreate method.
     * @param cust_id  corresponds to customer id
     *
     * @return CartSes
     * @exception RemoteException If there is a system failure.
     * @exception CreateException If there is a create failure.
     */
    public CartSes create(int cust_id)
        throws RemoteException, CreateException;

    /**
     * corresponds to ejbCreate method.
     *
     * @return CartSes
     * @exception RemoteException If there is a system failure.
     * @exception CreateException If there is a create failure.
     */
    public CartSes create() throws RemoteException, CreateException;
}

