/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 * OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of
 * any nuclear facility.
 */

package com.sun.j2ee.blueprints.mailer.ejb;

import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.Session;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;

import com.sun.j2ee.blueprints.mailer.util.JNDINames;
import com.sun.j2ee.blueprints.mailer.exceptions.MailerAppException;

/**
 * A helper class to create and send mail.
 */
public class MailHelper {


    /**
     * This method creates an email message and sends it using the
     * J2EE mail services
     * @param mailContent contains the message contents to send
     */
    public void createAndSendMail(String emailAddress, String subject, String mailContent, Locale locale) throws MailerAppException {
        try {
            InitialContext ic = new InitialContext();
            Session session = (Session) ic.lookup(JNDINames.MAIL_SESSION);
        Message msg = new MimeMessage(session);
            msg.setFrom();
            msg.setRecipients(Message.RecipientType.TO,
                     InternetAddress.parse(emailAddress, false));
            msg.setSubject(subject);
            String contentType = "text/html";
                StringBuffer sb = new StringBuffer(mailContent);
              msg.setDataHandler(new DataHandler(
                                  new ByteArrayDataSource(sb.toString(), contentType)));
            msg.setHeader("X-Mailer", "JavaMailer");
            msg.setSentDate(new Date());
            Transport.send(msg);
        } catch (Exception e) {
            System.err.print("createAndSendMail exception : " + e);
            throw new MailerAppException("Failure while sending mail");
        }
    }
}

