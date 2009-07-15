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

package com.sun.j2ee.blueprints.opc.transitions;

import java.util.Collection;
import java.util.Iterator;
import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.jms.*;

import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.processmanager.transitions.*;


/**
 * TransitionDelegate for Order Approval MDB
 */
public class OrderApprovalTD implements TransitionDelegate {

  private QueueConnectionFactory qFactory;
  private Queue mailQueue;
  private Queue supplierPoQueue;
  private QueueHelper supplierQueueHelper;
  private QueueHelper mailQueueHelper;


  public OrderApprovalTD() { }

  public void setup() throws TransitionException {
    try {
      ServiceLocator serviceLocator   = new ServiceLocator();
      qFactory = serviceLocator.getQueueConnectionFactory(JNDINames.QUEUE_CONNECTION_FACTORY);
      mailQueue = serviceLocator.getQueue(JNDINames.CR_MAIL_ORDER_APPROVAL_MDB_QUEUE);
      supplierPoQueue = serviceLocator.getQueue(JNDINames.SUPPLIER_PURCHASE_ORDER_QUEUE);
      mailQueueHelper     = new QueueHelper(qFactory, mailQueue);
      supplierQueueHelper = new QueueHelper(qFactory, supplierPoQueue);
    } catch(ServiceLocatorException se) {
        throw new TransitionException(se);
    }
  }

  /**
   * Send Purchase Orders to supplier to fulfill a customer order and
   * also send a list of approvals/denials to customer service to send emails
   *
   * @param supplierPoList is the list of Purchase Orders to send to supplier
   * @param xmlMailOrderApprovals is the list of approvals/denials to send to customers
   */
  public void doTransition(TransitionInfo info) throws TransitionException {
    String xmlPO = null;
    Collection supplierPoList = info.getXMLMessageBatch();
    String xmlMailOrderApprovals = info.getXMLMessage();

    try {
      Iterator it = supplierPoList.iterator();
      while(it!= null && it.hasNext()) {
        xmlPO = (String) it.next();
        //send PO to supplier for this order
        supplierQueueHelper.sendMessage(xmlPO);
       }//end while

       //send the list of valid order approval/deny to customer relations
       sendMail(xmlMailOrderApprovals);
     } catch(JMSException je) {
        throw new TransitionException(je);
    }

  }

  /**
   * Sends the list of orders and order status to
   * the customer relations which sends an email notice to each customer.
   * This is done as a batch by sending one message to the customer
   * relations, containing multiple customer order status notices
   *
   * @param xmlOrderApproval which is a list of order status for each order to
   * the customer relations to send email notices to each customer.
   */
  private void sendMail(String xmlOrderApproval) throws JMSException {
        mailQueueHelper.sendMessage(xmlOrderApproval);
  }

}

