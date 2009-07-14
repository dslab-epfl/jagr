/*
 * $Id: JNDINames.java,v 1.1.1.1 2002/10/03 21:17:37 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.petstoreadmin.control.util;

/**
 * This class is the central location to store the internal
 * JNDI names of various entities. Any change here should
 * also be reflected in the deployment descriptors.
 */
public interface JNDINames {
    public static final String ADMIN_USER_NAME =
        "java:comp/env/user/AdminId";
}
