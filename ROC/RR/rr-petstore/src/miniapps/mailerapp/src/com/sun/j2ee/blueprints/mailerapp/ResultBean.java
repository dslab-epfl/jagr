/*
 * $Id: ResultBean.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package com.sun.j2ee.blueprints.mailerapp;

import java.io.Serializable;

/**
 * A JavaBeans component representing an e-mail.
 */
public class ResultBean implements Serializable {

    protected String emailAddress = null;
    protected String subject = null;
    protected String resultMessage = null;

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String e) { emailAddress = e; }

    public String getSubject() { return subject; }
    public void setSubject(String s) { subject = s; }

    public String getResultMessage() { return resultMessage; }
    public void setResultMessage(String r) { resultMessage = r; }
}
