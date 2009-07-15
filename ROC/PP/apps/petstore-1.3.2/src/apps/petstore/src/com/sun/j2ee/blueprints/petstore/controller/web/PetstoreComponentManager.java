/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 * OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of
 * any nuclear facility.
 */
package com.sun.j2ee.blueprints.petstore.controller.web;


import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionEvent;
import javax.naming.InitialContext;

// J2EE imports
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;

// WAF imports
import com.sun.j2ee.blueprints.waf.controller.web.util.WebKeys;
import com.sun.j2ee.blueprints.waf.exceptions.GeneralFailureException;
import com.sun.j2ee.blueprints.waf.exceptions.AppException;
import com.sun.j2ee.blueprints.waf.controller.web.DefaultComponentManager;
import com.sun.j2ee.blueprints.waf.controller.web.WebController;
import com.sun.j2ee.blueprints.util.tracer.Debug;

// petstore imports
import com.sun.j2ee.blueprints.petstore.util.PetstoreKeys;
import com.sun.j2ee.blueprints.petstore.controller.ejb.ShoppingControllerLocal;
import com.sun.j2ee.blueprints.petstore.controller.ejb.ShoppingControllerLocalHome;
import com.sun.j2ee.blueprints.petstore.controller.ejb.ShoppingClientFacadeLocal;
import com.sun.j2ee.blueprints.petstore.util.JNDINames;

// cart component imports
import com.sun.j2ee.blueprints.cart.ejb.ShoppingCartLocal;
import com.sun.j2ee.blueprints.cart.ejb.ShoppingCartLocalHome;


// customer component imports
import com.sun.j2ee.blueprints.customer.ejb.CustomerLocal;

// service locator imports
import com.sun.j2ee.blueprints.servicelocator.web.ServiceLocator;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;

/**
 * This implmentation class of the ServiceLocator provides
 * access to services in the web tier and ejb tier.
 *
 */
public class PetstoreComponentManager extends DefaultComponentManager implements HttpSessionListener {

    private ServiceLocator serviceLocator;

    public PetstoreComponentManager () {
            serviceLocator = ServiceLocator.getInstance();
    }

    /**
     *
     * Initialize another service locator programtically
     *
     */
    public void init(HttpSession session) {
        session.setAttribute(PetstoreKeys.COMPONENT_MANAGER, this);
        session.setAttribute(PetstoreKeys.CART, getShoppingCart(session));
    }
    /**
     *
     * Create the WebClientController which in turn should create the
     * EJBClientController.
     *
     */
    public void sessionCreated(HttpSessionEvent se) {
        super.sessionCreated(se);
        se.getSession().setAttribute(PetstoreKeys.CART, getShoppingCart(se.getSession()));
    }

    public CustomerLocal  getCustomer(HttpSession session) {
        ShoppingControllerLocal scEjb = getShoppingController(session);
        try {
            ShoppingClientFacadeLocal scf = scEjb.getShoppingClientFacade();
            //scf.setUserId(userId);
            return scf.getCustomer();
        } catch (FinderException e) {
            System.err.println("PetstoreComponentManager finder error: " + e);
        } catch (Exception e) {
                System.err.println("PetstoreComponentManager error: " + e);
        }
        return null;
    }

    public  ShoppingControllerLocal getShoppingController(HttpSession session) {
        ShoppingControllerLocal scEjb = (ShoppingControllerLocal)session.getAttribute(PetstoreKeys.EJB_CONTROLLER);
        if (scEjb == null) {
            try {
                ShoppingControllerLocalHome scEjbHome =
                   (ShoppingControllerLocalHome)serviceLocator.getLocalHome(JNDINames.SHOPPING_CONTROLLER_EJBHOME);
                scEjb = scEjbHome.create();
                session.setAttribute(PetstoreKeys.EJB_CONTROLLER, scEjb);
            } catch (CreateException ce) {
                throw new GeneralFailureException(ce.getMessage());
            } catch (ServiceLocatorException ne) {
                 throw new GeneralFailureException(ne.getMessage());
            }
        }
        return scEjb;
    }

    public ShoppingCartLocal getShoppingCart(HttpSession session) {
        ShoppingControllerLocal scEjb = getShoppingController(session);
        ShoppingClientFacadeLocal scf = scEjb.getShoppingClientFacade();
        return  scf.getShoppingCart();
    }
}


