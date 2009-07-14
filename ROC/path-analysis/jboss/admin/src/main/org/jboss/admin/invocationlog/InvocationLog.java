package org.jboss.admin.invocationlog;

// standard imports
import java.io.Serializable;

import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.transaction.Status;


// jboss imports
import org.jboss.admin.MetricsConnector;
import org.jboss.admin.dataholder.InvocationEntry;

/**
 *
 * @author  <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 */
public class InvocationLog implements MessageListener, Serializable {

/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */

    public InvocationLog() {
        try {
            MetricsConnector connector = new MetricsConnector();
            connector.setTopic("topic/metrics");
            //connector.setMessageSelector("JMSType='BeanCache' OR JMSType='Invocation'");
            connector.connect(this);
            
            try   { Thread.sleep(10000000); }
            catch (InterruptedException ignored) {}
            
        }
        catch (NamingException e) {
            System.err.println("Unable to connect to metrics topic:");
            System.err.println(e);
        }
        catch (JMSException e) {
            System.err.println("Messaging error:");
            System.err.println(e.getMessage());
        }
    }
    
/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */
 
    public void onMessage(Message msg) {
  
System.out.println(msg);
     
        try {
            InvocationEntry entry  = new InvocationEntry(msg);
        
            System.out.println(entry);
        }
        catch (JMSException e) {
            System.err.println("Not a txMetrics message: " + e.getMessage());
        }
    }
}
