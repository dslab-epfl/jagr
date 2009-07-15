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


package com.sun.j2ee.blueprints.petstore.controller.ejb.actions;


import java.util.Collection;
import java.util.Iterator;
import java.util.Date;

// J2EE imports
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.naming.NamingException;

// WAF imports
import com.sun.j2ee.blueprints.waf.event.Event;
import com.sun.j2ee.blueprints.waf.event.EventResponse;
import com.sun.j2ee.blueprints.waf.event.EventException;
import com.sun.j2ee.blueprints.waf.controller.ejb.action.EJBActionSupport;


import com.sun.j2ee.blueprints.petstore.controller.events.CreateUserEvent;
import com.sun.j2ee.blueprints.contactinfo.ejb.ContactInfo;


// SignOn EJB Imports
import com.sun.j2ee.blueprints.signon.ejb.SignOnLocalHome;
import com.sun.j2ee.blueprints.signon.ejb.SignOnLocal;

// service locator imports
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;

// petstore imports
import com.sun.j2ee.blueprints.petstore.controller.exceptions.DuplicateAccountException;
import com.sun.j2ee.blueprints.petstore.controller.ejb.ShoppingClientFacadeLocal;
import com.sun.j2ee.blueprints.petstore.util.JNDINames;
import com.sun.j2ee.blueprints.petstore.util.PetstoreKeys;

public class CreateUserEJBAction extends EJBActionSupport {

  public EventResponse perform(Event e) throws EventException {
      CreateUserEvent cue = (CreateUserEvent)e;

         SignOnLocal signOn = null;
         String userName = cue.getUserName();
         String password = cue.getPassword();
         try {
            ServiceLocator sl = new ServiceLocator();
            SignOnLocalHome home =(SignOnLocalHome)sl.getLocalHome(JNDINames.SIGN_ON_EJBHOME);
            signOn = home.create();
         } catch (ServiceLocatorException  slx) {
             throw new DuplicateAccountException("Failed to Create SignOn EJB: caught " + slx);
         } catch (CreateException cx) {
             throw new DuplicateAccountException("Failed to Create SignOn EJB: caught " + cx);
         }
        try {
             signOn.createUser(userName, password);
             // put a copy of the userName in the statemachine atttirbute table for future retrival
             ShoppingClientFacadeLocal scf = null;
             scf = (ShoppingClientFacadeLocal)machine.getAttribute(PetstoreKeys.SHOPPING_CLIENT_FACADE);
             scf.setUserId(userName);
        } catch (CreateException ce) {
            throw new DuplicateAccountException("Bad UserName or password");
        }
        return null;
  }
}

