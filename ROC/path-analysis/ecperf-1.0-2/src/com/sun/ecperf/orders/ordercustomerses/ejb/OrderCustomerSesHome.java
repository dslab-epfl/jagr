
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: OrderCustomerSesHome.java,v 1.1.1.1 2002/11/16 05:35:28 emrek Exp $
 *
 */
package com.sun.ecperf.orders.ordercustomerses.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the Home interface of the OrderCustomerSession bean.
 */
public interface OrderCustomerSesHome extends EJBHome {
    OrderCustomerSes create() throws RemoteException, CreateException;
}

