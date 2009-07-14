/*
 * $Id: ProfileMgrAppLongIdException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.personalization.profilemgr.exceptions;

/**
 * ProfileMgrAppLongIdException is an exception that extends the
 * ProfileMgrAppException class. This is thrown by the personalization
 * component when the user if given is too long
 */
public class ProfileMgrAppLongIdException extends ProfileMgrAppException {

    /**
     * Constructor
     * @param str    a string that explains what the exception condition is
     */
    public ProfileMgrAppLongIdException (String str) {
        super(str);
    }

    /**
     * Default constructor. Takes no arguments
     */
    public ProfileMgrAppLongIdException () {
        super();
    }

}

