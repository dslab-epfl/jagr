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

import java.util.Locale;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.naming.Context;

import javax.jms.TextMessage;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Message;

import com.sun.j2ee.blueprints.mailer.exceptions.MailerAppException;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;


public class MailerMDB implements MessageDrivenBean, MessageListener {

  private Context context;
  private MessageDrivenContext mdc = null;

  public MailerMDB() {
  }

  public void ejbCreate() {
  }

  /**
   * When a message arrives in the mailer queue, onMeesage is notified
   * with the message, which contains the emailadress, the subject,
   * and the contents of the message. It is assumed that the message
   * is already formatted for any presentation since this mailer service
   * simly just sends the mails.
   */
  public void onMessage(Message recvMsg) {
    TextMessage textMessage = null;
    String xmlMailMessage = null;
    Mail recMail = null;
    try {
      textMessage = (TextMessage)recvMsg;
      xmlMailMessage = textMessage.getText();
      recMail = Mail.fromXML(xmlMailMessage);
      sendMail(recMail.getAddress(), recMail.getSubject(), recMail.getContent(),  Locale.getDefault());
    } catch (MailerAppException me) {
      //throw new EJBException("MailerMDB.onMessage" + me);
      //ignore since user probably forgot to set up mail server
    } catch (XMLDocumentException xde) {
      throw new EJBException("MailerMDB.onMessage" + xde);
    } catch(JMSException je) {
      throw new EJBException("MailerMDB.onMessage" + je);
    }
  }

  public void setMessageDrivenContext(MessageDrivenContext mdc) {
    this.mdc = mdc;
  }

  public void ejbRemove() {
  }

  //business helper methods

  private void sendMail(String emailAddress, String subject, String mailContent, Locale locale) throws MailerAppException {
    getMailHelper().createAndSendMail(emailAddress, subject, mailContent, locale);
  }

  private MailHelper getMailHelper() {
    return (new MailHelper());
  }
}

