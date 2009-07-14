/*
 * $Id: FlowHandler.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstore.control.web.handlers;

import com.sun.j2ee.blueprints.petstore.control.event.EStoreEvent;
import com.sun.j2ee.blueprints.petstore.control.exceptions.EStoreEventException;
import javax.servlet.http.HttpServletRequest;

/**
 * This class is the base interface to flow handlers on the
 * web tier.
 *
*/
public interface FlowHandler extends java.io.Serializable {

    public void doStart(HttpServletRequest request);
    public String processFlow(HttpServletRequest request) throws EStoreEventException;
    public void doEnd(HttpServletRequest request);

}
