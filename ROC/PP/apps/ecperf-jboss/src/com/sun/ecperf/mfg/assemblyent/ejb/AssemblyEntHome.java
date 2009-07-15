
/*
 *  Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 *
 * akmits@eng.sun.com 03/20/2000
 *
 * $Id: AssemblyEntHome.java,v 1.1 2004/02/19 14:45:08 emrek Exp $
 */
package com.sun.ecperf.mfg.assemblyent.ejb;


import javax.ejb.*;

import java.rmi.RemoteException;

import java.util.*;

import com.sun.ecperf.common.*;


/**
 * This interface is the home interface for the EJBean Assembly
 *
 * @author Ajay Mittal
 *
 */
public interface AssemblyEntHome extends javax.ejb.EJBHome {

    AssemblyEnt create(
        String id, String name, String description, String revision,
            int planner, int type, int purchased, int lomark, int himark)
                throws CreateException, RemoteException;

    AssemblyEnt findByPrimaryKey(String id)
        throws RemoteException, FinderException;

    java.util.Enumeration findAll() throws RemoteException, FinderException;
}

