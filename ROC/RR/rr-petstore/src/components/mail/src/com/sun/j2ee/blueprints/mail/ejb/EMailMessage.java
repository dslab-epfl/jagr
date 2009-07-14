/*
 * $Id: EMailMessage.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.mail.ejb;

import java.util.Locale;

/**
 * This class encapsulates all the info need to send an email
 * message. This object is passed to the MailerEJB
 * sendMail(...) method.
 */
public class EMailMessage implements java.io.Serializable {

    private String subject;
    private String htmlContents;
    private String emailReceiver;
    private Locale locale;

    public EMailMessage( String subject, String htmlContents,
                                         String emailReceiver, Locale locale) {
        this.subject       = subject;
        this.htmlContents  = htmlContents;
        this.emailReceiver = emailReceiver;
        this.locale = locale;
    }

    //subject field of email message
    public String getSubject() {
        return subject;
    }

    //Email address of recipient of email message
    public String getEmailReceiver() {
        return emailReceiver;
    }

    //contents of email message
    public String getHtmlContents() {
        return htmlContents;
    }

    public Locale getLocale() {
        return locale;
    }

    public String toString() {
        return  " locale=" + locale + " subject=" + subject + " " +  emailReceiver + " " + htmlContents;
    }
}

