/*
 * $Id: Parameter.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstore.control.web;

import java.util.HashMap;

public class Parameter implements java.io.Serializable {

    private String key;
    private String value;
    private boolean direct;

    public Parameter(String key, String value, boolean direct) {
        this.key = key;
        this.value = value;
        this.direct = direct;
    }

    public boolean isDirect() {
        return direct;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return "[Parameter: key=" + key + ", value=" + value + ", direct="+ direct + "]";
    }
}

