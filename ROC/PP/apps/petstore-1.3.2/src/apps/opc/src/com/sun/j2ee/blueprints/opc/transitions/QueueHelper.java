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

import javax.jms.*;

/**
 * A helper class which takes care of sending a JMS message to a queue
 */
public class QueueHelper implements java.io.Serializable {

    private Queue q;
    private QueueConnectionFactory qFactory;

    /**
     *
     * @param qFactory is the connection factory used to get a connection
     * @param q is the Queue to send the message to
     */
    public QueueHelper(QueueConnectionFactory qFactory, Queue q) {
        this.qFactory = qFactory;
        this.q = q;
    }

    /**
     * Sends a JMS message to a queue
     * @param xmlMessage is the xml message to put inside the JMS text message
     */
    public void sendMessage(String xmlMessage) throws JMSException {
      QueueConnection qConnect = null;
      QueueSession session = null;
      QueueSender qSender = null;

        try {
            qConnect = qFactory.createQueueConnection();
            session = qConnect.createQueueSession(false, 0);
            qSender = session.createSender(q);
          TextMessage jmsMsg = session.createTextMessage();
            jmsMsg.setText(xmlMessage);
            qSender.send(jmsMsg);
        } finally {
            try {
                if(qConnect != null) {
                    qConnect.close();
                }
            } catch(Exception e) {
            System.err.println("OPC.QueueHelper GOT EXCEPTION closing connection" + e);
          }
        }
    }
}

