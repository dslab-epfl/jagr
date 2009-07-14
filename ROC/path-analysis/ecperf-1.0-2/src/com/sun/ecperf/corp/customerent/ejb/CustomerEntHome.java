
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: CustomerEntHome.java,v 1.1.1.1 2002/11/16 05:35:25 emrek Exp $
 *
 */
package com.sun.ecperf.corp.customerent.ejb;


import com.sun.ecperf.common.CustomerInfo;


/**
 * Interface CustomerEntHome
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface CustomerEntHome extends javax.ejb.EJBHome {

    CustomerEnt create(CustomerInfo info)
        throws java.rmi.RemoteException, javax.ejb.CreateException;

    CustomerEnt findByPrimaryKey(Integer id)
        throws java.rmi.RemoteException, javax.ejb.FinderException;
}

