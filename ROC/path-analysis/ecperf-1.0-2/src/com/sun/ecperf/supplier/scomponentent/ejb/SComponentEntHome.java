
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.

 *
 * $Id: SComponentEntHome.java,v 1.1.1.1 2002/11/16 05:35:30 emrek Exp $
 *
 */
package com.sun.ecperf.supplier.scomponentent.ejb;


import javax.ejb.*;

import java.rmi.*;

import java.util.*;


/**
 * This is the home interface for the Component Entity Bean
 *
 *
 * @author Damian Guy
 */
public interface SComponentEntHome extends EJBHome {

    /**
     * create: Create new Component.
     * @param id - Id of component.
     * @param name - component name.
     * @param description - description of the component.
     * @param unit - the unit of measure for this component.
     * @param cost - price per component.
     * @param qtyOnOrder
     * @param qtyDemanded
     * @param leadTime
     * @param containerSize
     * @return SComponentEnt
     * @exception RemoteException - if there is a system failure.
     * @exception CreateException - if the create fails.
     */
    public SComponentEnt create(
        String id, String name, String description, String unit, double cost,
            int qtyOnOrder, int qtyDemanded, int leadTime,
                int containerSize) throws RemoteException, CreateException;

    /**
     * findByPrimaryKey: find the component that matches pk.
     * @param  pk - object that represents the pk.
     * @return SComponentEnt.
     * @exception RemoteException - if there is a system failure.
     * @exception FinderException - if cannot find object for pk.
     */
    public SComponentEnt findByPrimaryKey(String pk)
        throws RemoteException, FinderException;

    /**
     * findAll: retrieves all components.
     * @return Collection - all components.
     * @exception RemoteException - if there is a system failure.
     * @exception FinderException - No components exist in database.
     */
    public Enumeration findAll() throws RemoteException, FinderException;
}

