/*
 * $Id: MutableSignOnModel.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.signon.model;

import com.sun.j2ee.blueprints.signon.model.SignOnModel;

/**
 * This class represents the model data for the
 * sign on info. Note that this object is mutable
 * and will be used by the signon EJB only
 */
public class MutableSignOnModel extends SignOnModel {

    public MutableSignOnModel(String id, String pwd) {
        super(id, pwd);
    }

    public void setPassWord(String pwd) {
        password = pwd;
    }
}
