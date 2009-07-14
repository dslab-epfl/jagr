/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.admin.mbean;

// standard imports
import javax.rmi.PortableRemoteObject;
import javax.jms.Topic;
import javax.jms.Session;
import javax.jms.Message;
import javax.jms.TopicSession;
import javax.jms.TopicPublisher;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

// JBoss imports
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.monitor.MetricsConstants;


/**
 *   ... 
 *      
 *   @author Juha Lindfors (jplindfo@helsinki.fi)
 *   @version $Revision: 1.1.1.1 $
 */
public class SystemMonitor extends ServiceMBeanSupport 
    implements SystemMonitorMBean, MetricsConstants {

    // Attributes ----------------------------------------------------        
    private Thread  memoryMonitorThread    = null;
    private boolean isMemoryMonitorEnabled = false;
    private boolean isMemoryMonitorRunning = true;
    private long memoryMonitorInterval     = 2000;
    
    private Thread threadMonitorThread     = null;
    private boolean isThreadMonitorEnabled = false;
    private boolean isThreadMonitorRunning = true;
    private long threadMonitorInterval     = 2000;
    
    private TopicConnection connection     = null;
    private Topic topic                    = null;
    
    // Constructors --------------------------------------------------
    public SystemMonitor() {}

    
    // Method Overrides ----------------------------------------------
    public String getName() {
        return "System Monitor";    
    }
    
    public void startService() throws JMSException, NamingException {
        if (connection != null) 
            connection.start();
            
        else {
           //Well, since initService doesn't exist this should be here!
            // this really should be in the initService() method but
            // I couldn't figure out how to delay the initService()
            // invocation until naming and spyder have completely started...
            // So just checking for null reference, and doing lookup and
            // complete setup of JMS connection here. StopService will close
            // the JMS session and destroyService() will stop the JMS connection.
            InitialContext ctx = new InitialContext();

            Object factoryRef  = ctx.lookup("TopicConnectionFactory");
            Object topicRef    = ctx.lookup("topic/metrics");
            TopicConnectionFactory factory = (TopicConnectionFactory)PortableRemoteObject.narrow(factoryRef, TopicConnectionFactory.class);        
            topic = (Topic)PortableRemoteObject.narrow(topicRef, Topic.class);

            // TODO: create topic connection with username & password?
            connection  = factory.createTopicConnection();            
        }
    
        // start the memory monitor thread
        memoryMonitorThread = new Thread(new MemoryMonitor());
        setMemoryMonitorEnabled(true);
        
        // start the thread monitor thread
        threadMonitorThread = new Thread(new ThreadMonitor());
        setThreadMonitorEnabled(true);
    }
    
    public void stopService() {

        setMemoryMonitorEnabled(false);
        setThreadMonitorEnabled(false);
            
        try {
            connection.stop();
        }
        catch (JMSException ignored) {}
        //try {
        // If we close the connection it won't restart at startService()..
        // Exceptions from JBossMQ.
        //  connection.close();
            connection = null;
        //}
        //catch (JMSException e) {
        //    System.out.println("SystemMonitor destroyService() unable to close JMS connection: " + e.getMessage());
        //}
    }
    
    // Management Interface ------------------------------------------
    public boolean isMemoryMonitorEnabled() {
        return isMemoryMonitorEnabled;
    }
    
    public void setMemoryMonitorEnabled(boolean enable) {
        
        this.isMemoryMonitorEnabled = enable;

        // if this attribute method is called by config service before
        // startService() has been run, just return
        if (memoryMonitorThread == null)
            return;
  
        if (!enable) {
            isMemoryMonitorRunning = false;
            memoryMonitorThread.interrupt();
            
            return;
        }
            
        if (enable && !memoryMonitorThread.isAlive()) {
            try {
                isMemoryMonitorRunning = true;
                memoryMonitorThread = new Thread(new MemoryMonitor());
                memoryMonitorThread.setDaemon(true);
                memoryMonitorThread.setName("JBoss Memory Monitor Daemon");
                memoryMonitorThread.start(); 
            }
            catch (JMSException e) {
                System.out.println("Unable to create Memory Monitor: " + e.getMessage());
            }
        }
    }
    
    public void setMemoryMonitorInterval(long ms) {
        if (ms < 0)
            return;
            
        this.memoryMonitorInterval = ms;

        // thread reference may be null between init() and start() service...
        if (memoryMonitorThread != null)
            memoryMonitorThread.interrupt();
    }
    
    public long getMemoryMonitorInterval() {
        return memoryMonitorInterval;
    }
    
    public long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }
    
    public long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public boolean isThreadMonitorEnabled() {
        return isThreadMonitorEnabled;
    }
    
    public void setThreadMonitorEnabled(boolean enable) {
        
        this.isThreadMonitorEnabled = enable;

        // if this attribute method is called by config service before
        // startService() has been run, just return
        if (threadMonitorThread == null)
            return;
    
        if (!enable) {
            isThreadMonitorRunning = false;
            threadMonitorThread.interrupt();
            
            return;
        }

        if (enable && !threadMonitorThread.isAlive()) {
            try {
                isThreadMonitorRunning = true;
                threadMonitorThread = new Thread(new ThreadMonitor());
                threadMonitorThread.setDaemon(true);
                threadMonitorThread.setName("JBoss Thread Monitor Daemon");
                threadMonitorThread.start();
            }
            catch (JMSException e) {
                System.out.println("Unable to create Thread Monitor: " + e.getMessage());
            }
        }
    }
    
    public void setThreadMonitorInterval(long ms) {
        if (ms < 0)
            return;
            
        this.threadMonitorInterval = ms;
        
        // thread reference may be null between init() and start() service...
        if (threadMonitorThread != null)
            threadMonitorThread.interrupt();
    }
    
    public long getThreadMonitorInterval() {
        return threadMonitorInterval;
    }
    
    public int getThreadCount() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        
        while (group.getParent() != null)
            group = group.getParent();
            
        return group.activeCount();
    }
    
    // Inner Classes -------------------------------------------------
    class MemoryMonitor implements Runnable {
    
        TopicSession session = null;
        TopicPublisher pub   = null;
        
        public MemoryMonitor() throws JMSException {
            
            // create a non-transacted session (false)
            session = connection.createTopicSession(
                            false, Session.DUPS_OK_ACKNOWLEDGE);
                            
            pub = session.createPublisher(topic);
            // no need to persist these messages
            pub.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            // there's no need for the message ids
            pub.setDisableMessageID(true);
        }
        
        public void run() {
            
            while (isMemoryMonitorRunning) {
                
                try {
                    Message msg = session.createMessage();
                    msg.setJMSType(SYSTEM_METRICS);
                    msg.setStringProperty("TYPE", "MemoryMonitor");
                    msg.setLongProperty("FreeMem", getFreeMemory());
                    msg.setLongProperty("TotalMem", getTotalMemory());
                    
                    pub.publish(msg);
                }                    
                catch (JMSException ignored) {}
                
                try {
                    Thread.sleep(memoryMonitorInterval);
                }
                catch (InterruptedException ignored) {}
            }
        }
    }
    
    class ThreadMonitor implements Runnable {

        TopicSession session = null;
        TopicPublisher pub   = null;
        
        public ThreadMonitor() throws JMSException {

            // create a non-transacted session (false)
            session = connection.createTopicSession(
                            false, Session.DUPS_OK_ACKNOWLEDGE);

            pub = session.createPublisher(topic); 
            // no need to persist these messages
            pub.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            // there's no need for message ids
            pub.setDisableMessageID(true);                 
        }
        
        public void run() {
            
            while (isThreadMonitorRunning) {
                
                try {
                    Message msg = session.createMessage();
                    msg.setJMSType(SYSTEM_METRICS);
                    msg.setStringProperty("TYPE", "ThreadMonitor");
                    msg.setIntProperty("ThreadCount", getThreadCount());
                
                    pub.publish(msg);
                }
                catch (JMSException ignored) {}
                
                try {
                    Thread.sleep(threadMonitorInterval);
                }
                catch (InterruptedException ignored) {}
            }
        }
    }
    
    
}

