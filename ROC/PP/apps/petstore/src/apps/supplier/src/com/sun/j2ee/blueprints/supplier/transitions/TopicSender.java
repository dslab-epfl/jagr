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

import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicPublisher;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.JMSException;

/**
 * A helper class which takes care of sending a message to the
 * Invoice MDB Topic.
 */
public class TopicSender implements java.io.Serializable {

    private TopicConnectionFactory topicFactory;
    private Topic topic;

    /**
     * constructor that initializes the topic related resources
     */
    public TopicSender(TopicConnectionFactory topicFactory, Topic topic) {
      this.topicFactory = topicFactory;
      this.topic = topic;
    }

    /**
     * Helper method that can be uses to send string message to the topic
     * @param xmlMessage a String that represents the text message to be sent
     * @throws <Code>JMSException</Code> on failure to send
     */
    public void sendMessage(String xmlMessage) throws JMSException {
     TopicConnection topicConnect  = null;
     TopicSession pubSession       = null;
     TopicPublisher topicPublisher = null;

      try {
        topicConnect = topicFactory.createTopicConnection();
        pubSession = topicConnect.createTopicSession(false,
                                              Session.AUTO_ACKNOWLEDGE);
        topicPublisher = pubSession.createPublisher(topic);
        //topicConnect.start();
        TextMessage jmsMsg = pubSession.createTextMessage();
        jmsMsg.setText(xmlMessage);
        topicPublisher.publish(jmsMsg);
      } finally {
        if( topicConnect != null )
          topicConnect.close();
      }
      return;
    }
}

