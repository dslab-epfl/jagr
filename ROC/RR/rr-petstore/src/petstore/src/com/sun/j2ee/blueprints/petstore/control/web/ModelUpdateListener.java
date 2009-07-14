/*
 * $Id: ModelUpdateListener.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstore.control.web;

import com.sun.j2ee.blueprints.petstore.control.exceptions.EStoreAppException;

/**
 * This interface is implemented by objects which are interested in
 * getting the model update events. For example, CustomerWebImpl implements
 * this interface to get itself updated when account model gets updated.
*/

public interface ModelUpdateListener {

    public void performUpdate() throws  EStoreAppException;

}



