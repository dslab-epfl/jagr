/*
 * $Id: Screen.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstore.control.web;

import java.util.HashMap;

public class Screen implements java.io.Serializable {

    private String name;
    private HashMap parameters;

    public Screen(String name, HashMap parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public HashMap getParameters() {
        return parameters;
    }

    public Parameter getParameter(String key) {
        return (Parameter)parameters.get(key);
    }

    public String toString() {
        return "[Screen: name=" + name + ", parameters=" + parameters + "]";
    }
}

