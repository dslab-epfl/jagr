/*
 * $Id: JNDINames.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.inventory.util;

/**
 * This class is the central location to store the internal
 * JNDI names of various entities. Any change here should
 * also be reflected in the deployment descriptors.
 */
public interface JNDINames {


    public static final String INVENTORY_EJBHOME =
        "java:comp/env/ejb/inventory/Inventory";

    //
    // JNDI Names of data sources.
    //
    public static final String INVENTORY_DATASOURCE =
        "java:comp/env/jdbc/InventoryDataSource";

    public static final String INVENTORY_DAO_CLASS =
        "java:comp/env/ejb/inventory/InventoryDAOClass";
}
