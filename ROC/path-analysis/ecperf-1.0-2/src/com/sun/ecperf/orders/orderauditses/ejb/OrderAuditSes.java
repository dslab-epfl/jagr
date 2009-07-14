
/*
 *
 * Copyright (c) 1999-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 * @author 
 *
 * 
 */
package com.sun.ecperf.orders.orderauditses.ejb;


import javax.ejb.EJBObject;

import java.rmi.RemoteException;

/**
 * This interface is the remote interface for the OrderAuditSes
 * session bean. This bean is stateless.
 *
 * @author Ramesh Ramachandran
 *
 *
 */
public interface OrderAuditSes extends EJBObject {
     boolean validateInitialValues(int txRate) throws RemoteException;
     public int getOrderCount(long startTime, long endTime) throws RemoteException;
}

