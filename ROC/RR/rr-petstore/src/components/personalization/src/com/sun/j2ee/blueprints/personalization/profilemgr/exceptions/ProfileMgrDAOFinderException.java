/*
 * $Id: ProfileMgrDAOFinderException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.personalization.profilemgr.exceptions;

/**
 * ProfileMgrDAOFinderException is an exception that extends the
 * ProfileMgrDAOAppException. This is thrown by the DAOs of personalization
 * component when no row is found with the given primary key in a given table
 */

public class ProfileMgrDAOFinderException extends ProfileMgrDAOAppException {

    /**
     * Constructor
     * @param str    a string that explains what the exception condition is
     */
    public ProfileMgrDAOFinderException (String str) {
        super(str);
    }

    /**
     * Default constructor. Takes no arguments
     */
    public ProfileMgrDAOFinderException () {
        super();
    }

}

