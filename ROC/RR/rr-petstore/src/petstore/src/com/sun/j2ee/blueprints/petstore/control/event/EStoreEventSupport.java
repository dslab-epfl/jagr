/*
 * $Id: EStoreEventSupport.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstore.control.event;

import java.io.Serializable;

/**
 * This is the base class for all events used by the application.
 * Currently this class only implements Serializable to ensure that
 * all events may be sent the the EJB container via RMI-IIOP.
 */
public class EStoreEventSupport implements EStoreEvent {

    public String getEventName() {
        return null;
    }

}
