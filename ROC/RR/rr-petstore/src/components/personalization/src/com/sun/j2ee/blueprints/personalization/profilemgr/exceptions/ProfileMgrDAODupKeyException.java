/*
 * $Id: ProfileMgrDAODupKeyException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.personalization.profilemgr.exceptions;

/**
 * ProfileMgrDAODupKeyException is an exception that extends the
 * ProfileMgrDAOAppException. This is thrown by the DAOs of personalization
 * component when a row is already present in the database with the given
 * primary key
 */

public class ProfileMgrDAODupKeyException extends ProfileMgrDAOAppException {

    /**
     * Constructor
     * @param str    a string that explains what the exception condition is
     */
    public ProfileMgrDAODupKeyException (String str) {
        super(str);
    }

    /**
     * Default constructor. Takes no arguments
     */
    public ProfileMgrDAODupKeyException () {
        super();
    }

}

