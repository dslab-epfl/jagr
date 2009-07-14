/*
 * $Id: JNDINames.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.personalization.util;

/**
 * The JNDINames interface is the central location to store the static
 * internal JNDI names of various EJBs used by the ProfileMgr. Any change
 * here should also be reflected in the ProfileMgr deployment descriptor.
 */
public interface JNDINames {

    //
    // JNDI names of EJB home objects
    //
    public static final String PROFILE_EJBHOME =
        "java:comp/env/ejb/profilemgr/ProfileMgr";

    //
    // JNDI Names of data sources.
    //
    public static final String ESTORE_DATASOURCE =
        "java:comp/env/jdbc/EstoreDataSource";

    public static final String PROFILEMGR_DAO_CLASS =
        "java:comp/env/ejb/profilemgr/ProfileMgrDAOClass";
}
