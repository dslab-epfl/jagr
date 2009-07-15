
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: OrderCustomerSesHome.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
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

