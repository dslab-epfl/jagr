/**
    Copyright (C) 2004 Emre Kiciman and Stanford University

    This file is part of Pinpoint

    Pinpoint is free software; you can distribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    Pinpoint is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Pinpoint; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**/
package roc.pinpoint.tracing.jboss;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import roc.pinpoint.tracing.Observation;
import roc.pinpoint.tracing.ObservationException;
import roc.pinpoint.tracing.ObservationSubscriber;

/**
 * observation subscriber, implemented over JMS messages
 * @author emrek
 *
 */
public class JMSObservationSubscriber implements ObservationSubscriber {

    private TopicConnection connection = null;
    private TopicSession session = null;
    private Topic topic = null;
    private TopicSubscriber subscriber = null;

    /**
     * default constructor. looks up information about JNDI server (to look up
     * JMS queues) from system properties.  First it will look for a JNDI
     * properties file, then a URL to the JNDI server.
     * @see java.lang.Object#Object()
     */
    public JMSObservationSubscriber()
        throws JMSException, NamingException, IOException {
        String jndiPropsFilename =
            System.getProperty(
                "roc.pinpoint.tracing.jboss.jms" + ".JNDIPropsFilename");
        String jndiProviderURL =
            System.getProperty(
                "roc.pinpoint.tracing.jboss.jms." + "JNDIProviderURL");
        if (jndiPropsFilename != null) {
            System.err.println(
                "roc.pinpoint.tracing.jboss."
                    + "JMSObservationSubscriber: loading"
                    + " JNDI properties from "
                    + jndiPropsFilename);
            setup(new File(jndiPropsFilename));
        }
        else if (jndiProviderURL != null) {
            System.err.println(
                "roc.pinpoint.tracing.jboss."
                    + "JMSObservationSubscriber: using "
                    + " JNDI provider URL "
                    + jndiProviderURL);
            setup(jndiProviderURL);
        }
    }

    /**
     * construct a new subscriber based on configuration file
     * @param jndiPropertiesFile configuration file
     * @throws JMSException a JMS exception occurred while registering
     * subscription
     * @throws NamingException could not lookup topic in JNDI
     * @throws IOException could not read configuration file
     */
    public JMSObservationSubscriber(File jndiPropertiesFile)
        throws JMSException, NamingException, IOException {
        setup(jndiPropertiesFile);
    }

    /**
     * constructor, use the jndi provider specified
     * @param jndiProviderURL URL to jndi provider
     * @throws JMSException error occurred with JMS server
     * @throws NamingException couldn't find topic in JNDI server
     */
    public JMSObservationSubscriber(String jndiProviderURL)
        throws NamingException, JMSException {
        setup(jndiProviderURL);
    }

    /**
     * constructor, use the jndi properties specified
     * @param jndiProperties jndi properties
     * @throws JMSException error occurred with JMS server
     * @throws NamingException couldn't find topic in JNDI server
     */
    public JMSObservationSubscriber(Properties jndiProperties)
        throws JMSException, NamingException {
        setup(jndiProperties);
    }

    void setup(File jndiPropertiesFile)
        throws NamingException, JMSException, IOException {
        Properties jndiProperties = new Properties();
        jndiProperties.load(new FileInputStream(jndiPropertiesFile));
        setup(jndiProperties);
    }

     void setup(String jndiProviderURL)
        throws NamingException, JMSException {
        System.err.println("JMSOS: setup( jndiProviderURL ) 1");
        Properties jndiProperties = new Properties();
        jndiProperties.setProperty("java.naming.provider.url", jndiProviderURL);
        setup(jndiProperties);
        System.err.println("JMSOS: setup( jndiProviderURL ) done");
    }

    void setup(Properties jndiProperties)
        throws JMSException, NamingException {

        System.err.println("JMSOS: setup( jndiProperties ) 1");

        InitialContext initCtx = new InitialContext(jndiProperties);
        System.err.println("JMSOS: setup( jndiProperties ) 2");
        TopicConnectionFactory tcf =
            (TopicConnectionFactory) initCtx.lookup("ConnectionFactory");
        System.err.println("JMSOS: setup( jndiProperties ) 3");
        connection = tcf.createTopicConnection();
        System.err.println("JMSOS: setup( jndiProperties ) 4");
        topic = (Topic) initCtx.lookup(JMSObservationPublisher.TOPIC_TITLE);
        System.err.println("JMSOS: setup( jndiProperties ) 5");
        session =
            connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        System.err.println("JMSOS: setup( jndiProperties ) 6");
        connection.start();
        System.err.println("JMSOS: setup( jndiProperties ) 7");
        subscriber = session.createSubscriber(topic);
        System.err.println("JMSOS: setup( jndiProperties ) 8 done");
    }

    /**
     * @see roc.pinpoint.tracing.ObservationSubscriber#receive()
     */
    public Observation receive() throws ObservationException {

        try {
            Observation ret = null;

            Message msg = subscriber.receive();
            if (msg instanceof ObjectMessage) {
                ret = (Observation) ((ObjectMessage) msg).getObject();
            }
            else {
                System.err.println(
                    "Received unrecognized message " + msg.toString());
            }

            return ret;
        }
        catch (JMSException jmse) {
            throw new ObservationException(
                "receive failed " + jmse.getMessage());
        }
    }

    /**
     * stop connection and close JMS session and connection.
     * @throws JMSException an error occurred while cleaning up JMS connection
     */
    public void stop() throws JMSException {
        connection.stop();
        session.close();
        connection.close();
    }

}
