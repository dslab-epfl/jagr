/*
 * $Id: MailHandlerException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.mailerapp;

/**
 * An application exception indicating something has gone wrong
 * with sending e-mail.
 */
public class MailHandlerException extends Exception {

    public MailHandlerException() {}
    public MailHandlerException(String s) { super(s); }
}
