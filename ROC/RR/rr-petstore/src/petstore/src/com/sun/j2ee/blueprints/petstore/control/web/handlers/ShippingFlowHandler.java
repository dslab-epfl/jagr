/*
 * $Id: ShippingFlowHandler.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.petstore.control.web.handlers;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

import com.sun.j2ee.blueprints.customer.util.Address;
import com.sun.j2ee.blueprints.petstore.control.event.EStoreEvent;

import com.sun.j2ee.blueprints.petstore.control.event.AccountEvent;
import com.sun.j2ee.blueprints.customer.util.ContactInformation;
import com.sun.j2ee.blueprints.petstore.control.exceptions.EStoreEventException;
import com.sun.j2ee.blueprints.petstore.util.WebKeys;
import com.sun.j2ee.blueprints.util.tracer.Debug;

/**
 * Account Handler
 *
*/
public class ShippingFlowHandler implements FlowHandler {


    public void doStart(HttpServletRequest request) {
    }

    public String processFlow(HttpServletRequest request)
        throws EStoreEventException {
        String nextScreen = null;
        if (request.getAttribute(WebKeys.MissingFormDataKey) != null) {
            nextScreen =  "2";
        } else {
            nextScreen = "1";
        }
        return nextScreen;

    }

    public void doEnd(HttpServletRequest request) {
    }

}


