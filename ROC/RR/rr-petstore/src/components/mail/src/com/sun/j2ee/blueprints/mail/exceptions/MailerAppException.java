/*
 * $Id: MailerAppException.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.mail.exceptions;

/**
 * MailerAppException is an exception that extends the standrad
 * Exception. This is thrown by the mailer component when there is some
 * failure while sending the mail
 */

public class MailerAppException extends Exception {

    /**
     * Default constructor. Takes no arguments
     */
    public MailerAppException() {}

    /**
     * Constructor
     * @param str    a string that explains what the exception condition is
     */
    public MailerAppException(String str) {
        super(str);
    }
}
