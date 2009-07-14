package org.jboss.admin;

// standard imports
import java.util.Properties;
import java.io.Serializable;

import javax.rmi.PortableRemoteObject;

import javax.jms.MessageListener;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicSubscriber;
import javax.jms.TopicSession;
import javax.jms.Topic;
import javax.jms.Session;
import javax.jms.JMSException;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;


/**
 *
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class MetricsConnector implements Serializable {

    protected String topicConnectionFactoryName = 
            "TopicConnectionFactory";
    
    private String topicName =
            "topic/metrics";
    
    private Properties jndiProperties = null;
    private String messageSelector    = null;
        
    private transient Context namingContext = null;
    private transient TopicConnectionFactory topicConnectionFactory = null;
    private transient Topic topic = null;
    
/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */
 
    public MetricsConnector() {}
 
    public MetricsConnector(String topic) {
        setTopic(topic);
    }
 
    public MetricsConnector(String topic, Properties jndiProperties) {
        this(topic);
        
        setJNDIProperties(jndiProperties);
    }
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */
   
    public void connect(MessageListener listener) throws NamingException,
                                                         JMSException {

        // Get name service context. Either use 'jndi.properties' resource
        // from classloader's classpath or explicit naming properties if set.
        namingContext = (jndiProperties == null)
                      ? new InitialContext()
                      : new InitialContext(jndiProperties);
                      
        // Lookup topic connection factory and topic from the name service
        topicConnectionFactory = (TopicConnectionFactory)
                namingContext.lookup(topicConnectionFactoryName);
        
        topic = (Topic)namingContext.lookup(topicName);
        
        // Create a topic subscriber, attach a listener to it and start
        // receiving messages from the topic session.
        final boolean TRANSACTED_SESSION = false;
        final boolean NO_LOCAL_MESSAGES  = false;
        
        TopicConnection connection = topicConnectionFactory.createTopicConnection();
        TopicSession    session    = connection.createTopicSession(
                                        TRANSACTED_SESSION,
                                        Session.DUPS_OK_ACKNOWLEDGE
                                     );
                                     
        TopicSubscriber subscriber = session.createSubscriber(
                                        topic,
                                        messageSelector,
                                        NO_LOCAL_MESSAGES
                                    );
        subscriber.setMessageListener(listener);
    
        connection.start();      
    }

    public void setMessageSelector(String selector) {
        this.messageSelector = selector;
    }
    
    public void setTopic(String topic) {
        this.topicName = topic;
    }
    
    public void setJNDIProperties(Properties properties) {
        this.jndiProperties = properties;
    }
    
}

