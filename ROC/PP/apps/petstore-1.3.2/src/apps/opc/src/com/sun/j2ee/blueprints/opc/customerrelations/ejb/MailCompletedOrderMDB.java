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

package com.sun.j2ee.blueprints.opc.customerrelations.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.net.URL;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.MessageListener;
import javax.xml.transform.dom.DOMSource;

import com.sun.j2ee.blueprints.xmldocuments.OrderApproval;
import com.sun.j2ee.blueprints.xmldocuments.ChangedOrder;
import com.sun.j2ee.blueprints.xmldocuments.XMLDocumentException;
import com.sun.j2ee.blueprints.opc.transitions.MailCompletedOrderTD;
import com.sun.j2ee.blueprints.processmanager.transitions.*;
import com.sun.j2ee.blueprints.mailer.ejb.Mail;
import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrder;
import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrderLocal;
import com.sun.j2ee.blueprints.purchaseorder.ejb.PurchaseOrderLocalHome;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;



/**
 * MailCompletedOrderMDB receives a JMS message containing an Order
 * xml message for orders that are COMPLETELY shipped. It builds a mail
 * message that it then sends to the mailer service so that
 * the customer gets email
 */
public class MailCompletedOrderMDB implements MessageDrivenBean, MessageListener {

  private static final String MAIL_SUBJECT = "Java Pet Store Order COMPLETED: ";
  private static final String COMPLETED_ORDER_STYLE_SHEET =
  "/com/sun/j2ee/blueprints/opc/rsrc/xsl/CompletedOrder.xsl";
  private Context context;
  private MessageDrivenContext mdc = null;
  private boolean sendConfirmationMail = false;
  private MailContentXDE mailContentXDE;
  private TransitionDelegate transitionDelegate;
  private PurchaseOrderLocalHome  poHome;


  public MailCompletedOrderMDB() {
  }

  public void ejbCreate() {
    try {
      ServiceLocator serviceLocator   = new ServiceLocator();
      sendConfirmationMail = serviceLocator.getBoolean(JNDINames.SEND_COMPLETED_ORDER_MAIL);
      poHome = (PurchaseOrderLocalHome)serviceLocator.getLocalHome(JNDINames.PURCHASE_ORDER_EJB);
      transitionDelegate = new MailCompletedOrderTD();
      transitionDelegate.setup();
      mailContentXDE = new MailContentXDE(COMPLETED_ORDER_STYLE_SHEET);
    } catch (ServiceLocatorException se) {
      throw new EJBException(se);
    } catch (TransitionException te) {
      throw new EJBException(te);
    } catch (MailContentXDE.FormatterException mfe) {
      System.err.println(mfe.toString());
      throw new EJBException(mfe);
    }
  }

  /**
   * Receive a JMS Message containing the Order that is completed,
   * generate Mail xml messages for the customer
   * The Mail xml mesages contain html presentation
   */
  public void onMessage(Message recvMsg) {
    TextMessage recdTM = null;
    String recdText = null;
    String result = null;

    try {
      recdTM = (TextMessage)recvMsg;
      recdText = recdTM.getText();
      if (sendConfirmationMail) {
        result = doWork(recdText);
        doTransition(result);
      }
    } catch(XMLDocumentException xde) {
      throw new EJBException(xde);
    } catch(TransitionException te) {
      throw new EJBException(te);
    } catch  (JMSException je) {
      throw new EJBException(je);
    } catch (MailContentXDE.FormatterException mfe) {
      System.err.println(mfe.toString());
      throw new EJBException(mfe);
    } catch  (FinderException fe) {
      throw new EJBException(fe);
    }
  }

  public void setMessageDrivenContext(MessageDrivenContext mdc) {
    this.mdc = mdc;
  }

  public void ejbRemove() {
  }

  /**
   * update PO EJB based on list of order status updates and approvals
   * Also call the doTransition method for each order, so that the customer
   * will receive an email.
   */
  private String doWork(String orderId) throws JMSException,
    XMLDocumentException,
    MailContentXDE.FormatterException,
    FinderException,
    TransitionException {
      String subject = MAIL_SUBJECT +  orderId;
      PurchaseOrder poData = poHome.findByPrimaryKey(orderId).getData();
      String emailAddress = poData.getEmailId();
      mailContentXDE.setDocument(new DOMSource(poData.toDOM()));
      mailContentXDE.setLocale(poData.getLocale());
      String message = mailContentXDE.getDocumentAsString();

      //build  mail message as xml
      Mail mailMsg = new Mail(emailAddress, subject, message);
      return mailMsg.toXML();
  }

  /**
   * send a Mail message to mailer service, so customer gets an email
   */
  private void doTransition(String orderMail) throws TransitionException {
    TransitionInfo info = new TransitionInfo(orderMail);
    transitionDelegate.doTransition(info);
  }
}

