/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: RuleEnt.java,v 1.1.1.1 2002/11/16 05:35:25 emrek Exp $
 *
 * $Mod: RuleEnt.java,v 1.5 2001/04/14 08:30:00 hogstrom - Modified
 *       bean to support single row entities.
 */
package com.sun.ecperf.corp.ruleent.ejb;


import javax.ejb.*;

import java.rmi.*;


/**
 * This is the public interface of the Rule entity bean. It provides
 * access to the various fields of an item.
 */
public interface RuleEnt extends EJBObject {

    /**
     * Method getId
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public String getId() throws RemoteException;

    /**
     * Method getBytes
     *
     *
     * @return
     *
     * @throws RemoteException
     *
     */
    public byte[] getBytes() throws RemoteException;
}
