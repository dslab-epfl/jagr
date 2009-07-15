
/*
 *
 * Copyright (c) 1999-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 * @author Ramesh Ramachandran
 *
 * 
 */
package com.sun.ecperf.supplier.supplierauditses.ejb;

import java.util.Properties;

import javax.ejb.EJBObject;

import java.rmi.RemoteException;

/**
 * This interface is the remote interface for the SupplierAuditSes
 * session bean. This bean is stateless.
 *
 * @author Ramesh Ramachandran
 *
 *
 */
public interface SupplierAuditSes extends EJBObject {
     boolean validateInitialValues(int txRate) throws RemoteException;
     public int getPOCount() throws RemoteException;
     public int getPOLineCount() throws RemoteException;
     public int[] getServletTx() throws RemoteException;
}

