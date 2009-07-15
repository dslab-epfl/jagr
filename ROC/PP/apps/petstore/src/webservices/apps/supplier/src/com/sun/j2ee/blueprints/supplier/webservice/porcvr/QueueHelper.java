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

package com.sun.j2ee.blueprints.supplier.webservice.porcvr;

import javax.jms.*;

/**
 * A helper class which takes care of sending a JMS message to a queue
 */
public class QueueHelper {

  private Queue queue;
  private QueueConnectionFactory queueFactory;


  /**
   *
   * @param queueFactory is the connection factory used to get a connection
   * @param queue is the Queue to send the message to
   */
  public QueueHelper(QueueConnectionFactory queueFactory, Queue queue) {
    this.queueFactory = queueFactory;
    this.queue = queue;
    return;
  }

  /**
   * Sends a JMS message to a queue
   * @param xmlMessage is the xml message to put inside the JMS text message
   */
  public void sendMessage(String xmlMessage) throws JMSException {
    QueueConnection connection = null;
        try {
      connection = queueFactory.createQueueConnection();
      QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      QueueSender queueSender = session.createSender(queue);
      TextMessage message = session.createTextMessage();
      message.setText(xmlMessage);
      queueSender.send(message);
        } finally {
      try {
                if(connection != null) {
          connection.close();
                }
      } catch(Exception exception) {
        exception.printStackTrace(System.err);
      }
        }
    return;
  }
}

