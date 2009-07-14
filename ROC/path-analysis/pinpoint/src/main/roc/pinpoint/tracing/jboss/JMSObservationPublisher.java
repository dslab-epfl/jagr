package roc.pinpoint.tracing.jboss;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import roc.pinpoint.tracing.Observation;
import roc.pinpoint.tracing.ObservationException;
import roc.pinpoint.tracing.ObservationPublisher;

/**
 * implements ObservationPublisher using JMS message queues
 * @author emrek@cs.stanford.edu
 *
 */
public class JMSObservationPublisher implements ObservationPublisher {

    //public static final String TOPIC_TITLE="topic/pinpointObservationsTopic";
    /**
     * topic name to publish messages to.
     */
    public static final String TOPIC_TITLE = "topic/testTopic";

    private static JMSObservationPublisher globalPublisher;
    private static Object lock = new Object();

    private TopicConnection connection = null;
    private TopicSession session = null;
    private Topic topic = null;
    private TopicPublisher publisher = null;

    /**
     * default constructor.  looks up information about JNDI server (to look up
     * JMS queues) from system properties.  First it will look for a JNDI
     * properties file, then a URL to the JNDI server.
     * @see java.lang.Object#Object()
     */
    public JMSObservationPublisher()
        throws JMSException, NamingException, IOException {
        String jndiPropsFilename =
            System.getProperty(
                "roc.pinpoint.tracing.jboss.jms.JNDIPropsFilename");
        String jndiProviderURL =
            System.getProperty(
                "roc.pinpoint.tracing.jboss.jms.JNDIProviderURL");
        if (jndiPropsFilename != null) {
            System.err.println(
                "roc.pinpoint.tracing.jboss.JMSObservationPublisher:" 
                + " loading JNDI properties from "
                    + jndiPropsFilename);
            setup(new File(jndiPropsFilename));
        }
        else if (jndiProviderURL != null) {
            System.err.println(
                "roc.pinpoint.tracing.jboss.JMSObservationPublisher:" 
                + " using JNDI provider URL "
                    + jndiProviderURL);
            setup(jndiProviderURL);
        }
    }

    /**
     * constructor, use the jndi configuration in this file
     * @param jndiPropertiesFile  configuration file
     * @throws JMSException error occurred with JMS server
     * @throws NamingException couldn't find topic in JNDI server
     * @throws IOException couldn't load configuration file.
     */
    public JMSObservationPublisher(File jndiPropertiesFile)
        throws JMSException, NamingException, IOException {
        setup(jndiPropertiesFile);
    }


    /**
     * constructor, use the jndi provider specified
     * @param jndiProvider URL to jndi provider
     * @throws JMSException error occurred with JMS server
     * @throws NamingException couldn't find topic in JNDI server
     */
    public JMSObservationPublisher(String jndiProvider)
        throws JMSException, NamingException {
        setup(jndiProvider);
    }

    /**
     * constructor, use the jndi properties specified
     * @param jndiProperties jndi properties
     * @throws JMSException error occurred with JMS server
     * @throws NamingException couldn't find topic in JNDI server
     */
    public JMSObservationPublisher(Properties jndiProperties)
        throws JMSException, NamingException {
        setup(jndiProperties);
    }

    void setup(File jndiPropertiesFile)
        throws JMSException, NamingException, IOException {

        Properties jndiProperties = new Properties();
        jndiProperties.load(new FileInputStream(jndiPropertiesFile));
        setup(jndiProperties);
    }

     void setup(String jndiProvider)
        throws JMSException, NamingException {

        Properties jndiProperties = new Properties();
        jndiProperties.setProperty("java.naming.provider.url", jndiProvider);
        setup(jndiProperties);
    }

     void setup(Properties jndiProperties)
        throws JMSException, NamingException {

        InitialContext initCtx = new InitialContext(jndiProperties);
        TopicConnectionFactory tcf =
            (TopicConnectionFactory) initCtx.lookup("ConnectionFactory");

        connection = tcf.createTopicConnection();
        topic = (Topic) initCtx.lookup(TOPIC_TITLE);
        session =
            connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        connection.start();
        publisher = session.createPublisher(topic);
    }

    /**
     * @see roc.pinpoint.tracing.ObservationPublisher#send(Observation)
     */
    public void send(Observation observation) throws ObservationException {
	if( session == null )
	    return;

	try {
            ObjectMessage message = session.createObjectMessage(observation);
            publisher.publish(message);
        }
        catch (Throwable ignore) {
            //throw new ObservationException("send failed " + jmse.getMessage());
        }
    }

    /**
     * Stops publisher and closes JMS connection and session
     * @throws JMSException exception cleaning up JMS state
     */
    public void stop() throws JMSException {
        publisher.close();
        connection.stop();
        session.close();
        connection.close();
    }

    static void SetupGlobalPublisher() {
        synchronized (lock) {
            if (globalPublisher == null) {
                try {
                    globalPublisher = new JMSObservationPublisher();
                }
                catch (Exception e) {
                    System.err.println(
                        "roc.pinpoint.tracing.jboss.JMSObservationPublisher:" 
                        + " Could not initialize global publisher:"
                            + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * get a static publisher, accessible without a JMSObservationPublisher
     * instance
     * @return JMSObservationPublisher
     */
    public static JMSObservationPublisher getGlobalPublisher() {
        if (globalPublisher == null) {
            SetupGlobalPublisher();
        }
        return globalPublisher;
    }

    /**
      * send a message using global JMS publisher, without an
      * explicitJMSObservationPublisher instance
      * @param observation observation to publish
      */
    public static void GlobalSend(Observation observation) {
        if (globalPublisher == null) {
            SetupGlobalPublisher();
        }

        if (globalPublisher != null) {
            try {
                globalPublisher.send(observation);
            }
            catch (ObservationException oe) {
                System.out.println(
                    "Could not send message: " + oe.getMessage());
            }
        }
    }

}
