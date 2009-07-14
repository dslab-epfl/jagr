package org.jboss.admin.monitor;

// standard imports
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.JMSException;

import javax.transaction.SystemException;


/**
 * Main class for the monitoring tool. Starts the app, that's all.
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class Main {

    public static void main(String[] args) {
        MonitorFrame frame = new MonitorFrame();
        frame.setVisible(true);
    }
}

