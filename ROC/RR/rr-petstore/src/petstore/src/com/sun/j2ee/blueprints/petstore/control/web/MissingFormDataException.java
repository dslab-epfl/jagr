/*
 * $Id: MissingFormDataException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.petstore.control.web;

import java.util.Collection;

/**
 * This exception is thrown by the RequestToEventTranslator
 * when a user fails to provide enough form information. This
 * excption contains list of form fields needed. This exception
 * is used by a JSP page to generate an error page.
 */
public class MissingFormDataException extends Exception implements java.io.Serializable {

    private Collection missingFields;
    private String message;

    public MissingFormDataException(String message, Collection missingFields) {
        this.message = message;
        this.missingFields = missingFields;
    }

    public Collection getMissingFields() {
        return missingFields;
    }

    public String getMessage() {
        return message;
    }

}
