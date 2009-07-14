/*
 * $Id: SignoutEvent.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstore.control.event;

import java.io.Serializable;

/**
 * This event is sent from the web tier to the EJB Controller to notify
 * the EJB Controller that a user has logged out of the application.
 */
public class SignoutEvent extends EStoreEventSupport {

    public SignoutEvent() {}

    public String toString() {
        return "SignoutEvent()";
    }
}


