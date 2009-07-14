/*
 * $Id: JNDINames.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.shoppingcart.util;

/**
 * This class is the central location to store the internal
 * JNDI names of various entities. Any change here should
 * also be reflected in the deployment descriptors.
 */
public interface JNDINames {

    //
    // JNDI names of EJB home objects
    //
    public static final String CATALOG_EJBHOME =
        "java:comp/env/ejb/catalog/Catalog";

    //
    // JNDI Names of data sources.
    //

    public static final String ESTORE_DATASOURCE =
        "java:comp/env/jdbc/EstoreDataSource";

    //
    // JNDI Names of application properties
    //
    public static final String USE_CATALOG_EJB =
        "java:comp/env/ejb/shoppingcart/useCatalogEJB";

    public static final String CATALOG_DAO_CLASS =
        "java:comp/env/ejb/catalog/CatalogDAOClass";
}
