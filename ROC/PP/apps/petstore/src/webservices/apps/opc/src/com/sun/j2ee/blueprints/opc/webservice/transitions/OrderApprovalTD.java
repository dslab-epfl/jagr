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

package com.sun.j2ee.blueprints.opc.webservice.transitions;

import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.jms.*;

import com.sun.j2ee.blueprints.processmanager.transitions.*;
import com.sun.j2ee.blueprints.opc.webservice.posender.*;
import com.sun.j2ee.blueprints.opc.transitions.*;
import com.sun.j2ee.blueprints.opc.transitions.JNDINames;
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;


/**
 * TransitionDelegate for Order Approval MDB
 */
public class OrderApprovalTD implements TransitionDelegate {

  private Queue mailQueue;
  private QueueHelper mailQueueHelper;
  private SupplierOrderSender supplierOrderSender;


  public OrderApprovalTD() {}

  public void setup() throws TransitionException {
    try {
      ServiceLocator serviceLocator = new ServiceLocator();
      QueueConnectionFactory queueFactory
        = serviceLocator.getQueueConnectionFactory(JNDINames.QUEUE_CONNECTION_FACTORY);
      mailQueue = serviceLocator.getQueue(JNDINames.CR_MAIL_ORDER_APPROVAL_MDB_QUEUE);
      mailQueueHelper = new QueueHelper(queueFactory, mailQueue);
      supplierOrderSender
        = new SupplierOrderSender(serviceLocator.getUrl(com.sun.j2ee.blueprints.opc.webservice.transitions.JNDINames.SUPPLIER_WEB_SERVICE_ENDPOINT_URL));
    } catch (ServiceLocatorException exception) {
      throw new TransitionException(exception);
    }
    return;
  }

  /**
   * Send Purchase Orders to supplier to fulfill a customer order and
   * also send a list of approvals/denials to customer service to send emails
   *
   * @param info contains the list of Purchase Orders to send to supplier and
   *        the list of approvals/denials to send to customers
   */
  public void doTransition(TransitionInfo info) throws TransitionException {
    Collection supplierPoList = info.getXMLMessageBatch();
    String xmlMailOrderApprovals = info.getXMLMessage();

    String xmlPO = null;

    try {
      Iterator it = supplierPoList.iterator();
      while(it != null && it.hasNext()) {
        xmlPO = (String) it.next();
        //send a PO to supplier for this order
        if(xmlPO != null) {
          supplierOrderSender.submitOrder(xmlPO);
        }
      }
      //send the list of valid order approval/deny to customer relations
      sendMail(xmlMailOrderApprovals);
    } catch (Exception exception) {
      throw new TransitionException(exception);
    }
    return;
  }

  /**
   * This transition sends the list of order status for each order to
   * the customer relations to send email notices to each customer.
   * This is done as a batch by sending one message to the customer
   * relations, containing multiple customer order status notices
   *
   * @param xmlOrderApproval which is a list of order status for each order to
   * the customer relations to send email notices to each customer.
   */
  private void sendMail(String xmlOrderApproval) throws JMSException {
    mailQueueHelper.sendMessage(xmlOrderApproval);
    return;
  }
}

