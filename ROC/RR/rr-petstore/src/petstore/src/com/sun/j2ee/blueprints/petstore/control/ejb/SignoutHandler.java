/*
 * $Id: SignoutHandler.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstore.control.ejb;


import com.sun.j2ee.blueprints.util.tracer.Debug;

import com.sun.j2ee.blueprints.petstore.control.event.SignoutEvent;
import com.sun.j2ee.blueprints.petstore.control.event.EStoreEvent;

import com.sun.j2ee.blueprints.petstore.control.exceptions.EStoreEventException;

public class SignoutHandler extends StateHandlerSupport {



  public void perform(EStoreEvent event) throws EStoreEventException {
      SignoutEvent se = (SignoutEvent)event;
      Debug.println("SignoutEvent: " + se);
      // do whatever cleanup you may want to do
  }
}

