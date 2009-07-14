/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mq.server.jmx;

import java.util.ArrayList;
import javax.jms.JMSException;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.mq.SpyQueue;
import org.jboss.mq.SpyTopic;
import org.jboss.mq.server.JMSDestinationManager;
import org.jboss.mq.server.JMSServerInterceptor;
import org.jboss.mq.sm.StateManager;
import org.jboss.mq.pm.PersistenceManager;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.ServiceControllerMBean;

import org.jboss.util.jmx.MBeanProxy;


/**
 * JMX MBean implementation for JBossMQ.
 *
 * @jmx:mbean extends="org.jboss.mq.server.jmx.InterceptorMBean"
 * @author     Vincent Sheffer (vsheffer@telkel.com)
 * @author     <a href="mailto:jplindfo@helsinki.fi">Juha Lindfors</a>
 * @author     <a href="hiram.chirino@jboss.org">Hiram Chirino</a>
 * @version    $Revision: 1.1.1.1 $
 */
public class DestinationManager
   extends InterceptorMBeanSupport
   implements DestinationManagerMBean
{
   public String jndiBindLocation = "java:/JBossMQServer";
   
   // Attributes ----------------------------------------------------

   /** A proxy to the service controller. */
   private ServiceControllerMBean serviceController;
   
   private ObjectName mqService; 
   
   private JMSDestinationManager jmsServer;
   
   private ObjectName persistenceManager;
   
   private ObjectName stateManager;
   
   private ObjectName mManagementProxy;

   
   
   /**
    * Get the value of PersistenceManager.
    * 
    * @jmx:managed-attribute
    * @return value of PersistenceManager.
    */
   public ObjectName getPersistenceManager() 
   {
      return persistenceManager;
   }
   
   /**
    * Set the value of PersistenceManager.
    * 
    * @jmx:managed-attribute
    * @param v  Value to assign to PersistenceManager.
    */
   public void setPersistenceManager(ObjectName objectName) 
   {
      this.persistenceManager = objectName;
   }
   
   /**
    * Get the value of StateManager.
    * 
    * @jmx:managed-attribute
    * @return value of StateManager.
    */
   public ObjectName getStateManager() 
   {
      return stateManager;
   }
   
   /**
    * Set the value of StateManager.
    * 
    * @jmx:managed-attribute
    * @param v  Value to assign to StateManager.
    */
   public void setStateManager(ObjectName  objectName) 
   {
      this.stateManager = objectName;
   }

   /**
    * @jmx:managed-attribute
    */
   public void createQueue(String name) throws Exception
   {   
      createDestination("org.jboss.mq.server.jmx.Queue",
                        getQueueObjectName(name), null);
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void createTopic(String name) throws Exception
   {
      createDestination("org.jboss.mq.server.jmx.Topic",
                        getTopicObjectName(name), null);
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void createQueue(String name, String jndiLocation) throws Exception
   {   
      createDestination("org.jboss.mq.server.jmx.Queue",
                        getQueueObjectName(name), jndiLocation);
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void createTopic(String name, String jndiLocation) throws Exception
   {
      createDestination("org.jboss.mq.server.jmx.Topic",
                        getTopicObjectName(name), jndiLocation);
   }
   
   // TODO. Should we add any Kind of security configuration for these 
   // dynamicly created destination. For example en optional URL to
   // an xml config file.
   protected void createDestination(String type, ObjectName name, String jndiLocation) throws Exception {
      if (log.isDebugEnabled()) {
         log.debug("Attempting to create destination: " + name + "; type=" + type);
      }
      
      server.createMBean(type, name);
      server.setAttribute(name, new Attribute("DestinationManager", mqService));
      if( jndiLocation != null )
      	server.setAttribute(name, new Attribute("JNDIName", jndiLocation));
      
      // This destination should be stopped when we are stopped
      ArrayList depends = new ArrayList();
      depends.add(serviceName);

      serviceController.create(name, depends);
      serviceController.start(name);
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void destroyQueue(String name) throws Exception
   {
      destroyDestination(getQueueObjectName(name));      
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void destroyTopic(String name) throws Exception
   {
      destroyDestination(getTopicObjectName(name));
   }

   protected void destroyDestination(ObjectName name) throws Exception
   {
      if (log.isDebugEnabled()) {
         log.debug("Attempting to destroy destination: " + name);
      }
      
      serviceController.stop(name);
      
      server.invoke(name, "removeAllMessages", new Object[]{}, new String[]{});      
      serviceController.destroy(name);
      serviceController.remove(name);
   }
      
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      // Save our object name to create destination names based on it
      mqService = name;      
      return mqService;
   }
   
   protected void stopService()
   {
      jmsServer.stopServer();
   }
   
   private ObjectName getTopicObjectName(String name) throws MalformedObjectNameException
   {
      return new ObjectName(mqService.getDomain() + ".destination:service=Topic,name=" + name);
   }
   
   private ObjectName getQueueObjectName(String name) throws MalformedObjectNameException
   {
      return new ObjectName(mqService.getDomain() + ".destination:service=Queue,name=" + name);
   }

   /**
    * @see InterceptorMBean#getInterceptor()
    */
   public JMSServerInterceptor getInterceptor()
   {
      return jmsServer;
   }

   /**
    * @see ServiceMBeanSupport#createService()
    */
   protected void createService() throws Exception
   {
      super.createService();
      jmsServer = new JMSDestinationManager();
      // Create the JSR-77 management representation
   }

   protected void startService() throws Exception
   {
      // Get a proxy to the service controller
      serviceController = (ServiceControllerMBean)
         MBeanProxy.create(ServiceControllerMBean.class,
                           ServiceControllerMBean.OBJECT_NAME,
                           server);

      PersistenceManager pm = (PersistenceManager)
         server.getAttribute(persistenceManager, "Instance");

      jmsServer.setPersistenceManager(pm);

      StateManager sm = (StateManager)
         server.getAttribute(stateManager, "Instance");

      jmsServer.setStateManager(sm);
      
      jmsServer.startServer();
      
      super.startService();
   }
   
}
