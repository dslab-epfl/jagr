/*
 * $Id: RequestHandlerSupport.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.petstore.control.web.handlers;

import com.sun.j2ee.blueprints.petstore.control.event.EStoreEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;

/**
 * This class is the default implementation of the RequestHandler
 *
*/
public abstract class RequestHandlerSupport implements RequestHandler {

    protected ServletContext context;

    public void setServletContext(ServletContext context) {
        this.context  = context;
    }

    public void doStart(HttpServletRequest request){}
    public void doEnd(HttpServletRequest request){}
}
