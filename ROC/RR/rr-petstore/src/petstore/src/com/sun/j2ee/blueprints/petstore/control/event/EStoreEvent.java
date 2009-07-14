/*
 * $Id: EStoreEvent.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstore.control.event;


/**
 * This interface determines the required methods for an estore event
 */
public interface EStoreEvent extends java.io.Serializable {
    /**
    *   Specifiy a logical name that is mapped to the event in
    *   in the Universal Remote Controller.
    */
    public String getEventName();
}
