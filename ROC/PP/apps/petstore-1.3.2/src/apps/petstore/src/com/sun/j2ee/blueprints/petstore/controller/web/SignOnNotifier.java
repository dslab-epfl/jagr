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

import java.util.HashMap;
import java.beans.Beans;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.HashMap;

import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

// signon component
import com.sun.j2ee.blueprints.signon.web.SignOnFilter;

// WAF imports
import com.sun.j2ee.blueprints.waf.event.EventException;
import com.sun.j2ee.blueprints.waf.util.I18nUtil;
import com.sun.j2ee.blueprints.waf.controller.web.WebController;
import com.sun.j2ee.blueprints.waf.controller.web.EventMapping;
import com.sun.j2ee.blueprints.waf.event.Event;

// customer imports
import com.sun.j2ee.blueprints.customer.profile.ejb.ProfileLocal;
import com.sun.j2ee.blueprints.customer.ejb.CustomerLocal;

// petstore imports
import com.sun.j2ee.blueprints.petstore.util.PetstoreKeys;
import com.sun.j2ee.blueprints.petstore.controller.events.SignOnEvent;
import com.sun.j2ee.blueprints.petstore.controller.web.PetstoreComponentManager;

/**
 * This class will bind with the current session and notify the Petstore
 * Back end when a SignOn has occured.
 *
 * This allows for a loose coupling of the SignOn component and the
 * Petstore Application.  Ensure the neccessary setup is done in the applicaiton when a
 * user signs in.
 */
public class SignOnNotifier
   implements java.io.Serializable, HttpSessionAttributeListener {


    public SignOnNotifier() { }


    // do nothing
    public void attributeRemoved(HttpSessionBindingEvent se) {}

    /**
     *
     * Process an attribute added
     *
     */
    public void attributeAdded(HttpSessionBindingEvent se) {
        processEvent(se);
    }

    /**
     * Process the update
     */
    public void attributeReplaced(HttpSessionBindingEvent se) {
        processEvent(se);
    }

    private void processEvent(HttpSessionBindingEvent se) {
        HttpSession session = se.getSession();
        String name = se.getName();

        /* check if the value matches the signon attribute
         * if a macth fire off an event to the ejb tier that the user
         * has signed on and load the account for the user
         */
        if (name.equals(SignOnFilter.SIGNED_ON_USER)) {
            boolean aSignOn  = ((Boolean)se.getValue()).booleanValue();
            if (aSignOn) {

              String userName = (String)session.getAttribute(SignOnFilter.USER_NAME);
              // look up the model manager and webclient controller
              PetstoreComponentManager sl = (PetstoreComponentManager)session.getAttribute(PetstoreKeys.COMPONENT_MANAGER);
              WebController wc =  sl.getWebController(session);
              SignOnEvent soe = new SignOnEvent(userName);
              // set the EJBAction on the Event
              EventMapping em = getEventMapping(session.getServletContext(), soe);
              if (em != null) {
                  soe.setEJBActionClassName(em.getEJBActionClassName());
              }

              try {
                  wc.handleEvent(soe, session);
              } catch (EventException e) {
                  System.err.println("SignOnNotifier Error handling event " + e);
              }
              CustomerLocal customer =  sl.getCustomer(session);
              // ensure the customer object is put in the session
              if (session.getAttribute(PetstoreKeys.CUSTOMER) == null) {
                session.setAttribute(PetstoreKeys.CUSTOMER, customer);
              }
              // set the language to the preferred language and other preferences
              ProfileLocal profile = sl.getCustomer(session).getProfile();
              Locale locale = I18nUtil.getLocaleFromString(profile.getPreferredLanguage());
              session.setAttribute(PetstoreKeys.LOCALE, locale);
            }
        }
    }

    /**
     * The EventMapping object contains information that will match
     * a event class name to an EJBActionClass.
     *
    */

    private EventMapping getEventMapping(ServletContext context, Event eventClass) {
        HashMap eventMappings = (HashMap)context.getAttribute(PetstoreKeys.EVENT_MAPPINGS);
        // get the fully qualified name of the event class
        String eventClassName = eventClass.getClass().getName();
        if ((eventMappings != null) && eventMappings.containsKey(eventClassName)) {
            return (EventMapping)eventMappings.get(eventClassName);
        } else {
            return null;
        }
    }
}


