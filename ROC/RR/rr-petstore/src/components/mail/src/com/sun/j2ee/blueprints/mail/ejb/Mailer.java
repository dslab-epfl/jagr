/*
 * $Id: Mailer.java,v 1.1.1.1 2002/10/03 21:17:36 candea Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits r�serv�s.
 */

package com.sun.j2ee.blueprints.mail.ejb;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;

import com.sun.j2ee.blueprints.mail.exceptions.MailerAppException;

/**
 * This interface provides method to send mail messages such
 * as order confirmation mail to a client
 */
public interface Mailer extends EJBObject {

     /**
     * This method sends an email message.
     *
     * @param  eMess  content  for the email message and also the
     *                information such as recipient required to
     *                send the message.
     * @see    EMailMessage
     */
    public void sendMail(EMailMessage eMess)
        throws RemoteException, MailerAppException;
}
