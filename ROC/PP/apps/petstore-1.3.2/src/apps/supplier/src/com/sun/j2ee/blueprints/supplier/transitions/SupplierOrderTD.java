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

package com.sun.j2ee.blueprints.supplier.transitions;

import javax.jms.*;
import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.processmanager.transitions.*;


/**
 * TransitionDelegate for Supplier Order MDB
 */
public class SupplierOrderTD implements TransitionDelegate {

  private TopicConnectionFactory topicFactory;
  private Topic topic;

  private TopicSender invoiceTopicSender;

  public SupplierOrderTD() { }

  public void setup() throws TransitionException {
    try {
      ServiceLocator servicelocator = new ServiceLocator();
      topicFactory = servicelocator.getTopicConnectionFactory(JNDINames.TOPIC_CONNECTION_FACTORY);
      topic = servicelocator.getTopic(JNDINames.INVOICE_MDB_TOPIC);
      invoiceTopicSender = new TopicSender(topicFactory, topic);
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
    String xmlInvoice = info.getXMLMessage();
    try {
      invoiceTopicSender.sendMessage(xmlInvoice);
    } catch(JMSException je) {
        throw new TransitionException(je);
    }
  }

}

