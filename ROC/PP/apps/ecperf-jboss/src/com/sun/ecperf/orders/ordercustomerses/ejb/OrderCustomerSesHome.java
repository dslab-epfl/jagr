
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: OrderCustomerSesHome.java,v 1.1 2004/02/19 14:45:07 emrek Exp $
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

