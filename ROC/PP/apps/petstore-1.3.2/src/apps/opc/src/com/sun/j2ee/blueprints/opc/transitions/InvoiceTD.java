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

import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.jms.*;

import com.sun.j2ee.blueprints.servicelocator.ServiceLocatorException;
import com.sun.j2ee.blueprints.servicelocator.ejb.ServiceLocator;
import com.sun.j2ee.blueprints.processmanager.transitions.*;

/**
 * TransitionDelegate for Invoice MDB
 */
public class InvoiceTD implements TransitionDelegate {

  private Queue q;
  private QueueConnectionFactory qFactory;
  private QueueHelper queueHelper;

  public InvoiceTD()  { }

  /**
   * sets up all the resources that will be needed to do a transition
   */
  public void setup() throws TransitionException {
    try {
      ServiceLocator serviceLocator = new ServiceLocator();
      qFactory = serviceLocator.getQueueConnectionFactory(JNDINames.QUEUE_CONNECTION_FACTORY);
      q = serviceLocator.getQueue(JNDINames.CR_MAIL_COMPLETED_ORDER_MDB_QUEUE);
      queueHelper = new QueueHelper(qFactory, q);
    } catch(ServiceLocatorException se) {
        throw new TransitionException(se);
    }
  }


  /**
   * Send an order approval to the OrderApproval Queue, since the work
   * for this step in the order fullfillment process is done.
   */
  public void doTransition(TransitionInfo info) throws TransitionException {
    String xmlCompletedOrder = info.getXMLMessage();
    try {
      queueHelper.sendMessage(xmlCompletedOrder);
    } catch (JMSException je) {
        throw new TransitionException(je);
    }
  }

}

