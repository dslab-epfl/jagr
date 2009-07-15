
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id:
 *
 */
package com.sun.ecperf.orders.orderses.ejb;


import javax.ejb.*;

import java.rmi.*;

import com.sun.ecperf.orders.helper.*;


/**
 * This is the Home interface of the OrderSession bean.
 */
public interface OrderSesHome extends EJBHome {
    OrderSes create() throws RemoteException, CreateException;
}

