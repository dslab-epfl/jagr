/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.mq.server.jmx;

import javax.naming.InitialContext;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.InvalidAttributeValueException;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.naming.Util;
import org.jboss.logging.Logger;

import org.jboss.mq.SpyDestination;
import org.jboss.mq.server.JMSDestinationManager;
/**
 * Super class for destination managers.
 *
 * @author     <a href="pra@tim.se">Peter Antman</a>
 * @version $Revision: 1.1.1.1 $
 */

abstract public class DestinationMBeanSupport extends ServiceMBeanSupport
   implements DestinationMBean, MBeanRegistration {
   
   SpyDestination spyDest;
   String destinationName;
   String jndiName;
   boolean jndiBound;
   ObjectName jbossMQService;
   //String securityConf;
   org.w3c.dom.Element securityConf;

   /**
    * A optional security manager. Must be set to use security conf.
    */
   ObjectName securityManager;
   /**
    * Get the value of JBossMQService.
    * 
    * @return value of JBossMQService.
    */
   public ObjectName getDestinationManager() 
   {
      return jbossMQService;
   }
   
   /**
    * Set the value of JBossMQService.
    * 
    * @param v  Value to assign to JBossMQService.
    */
   public void setDestinationManager(ObjectName  jbossMQService) 
   {
      this.jbossMQService = jbossMQService;
   }
   /*
   public void setSecurityConf(String securityConf) throws Exception {
      log.debug("Setting securiyConf: " + securityConf);
      this.securityConf = securityConf;
   }
   */
   public void setSecurityConf(org.w3c.dom.Element securityConf) throws Exception {
      log.debug("Setting securityConf: " + securityConf);
      this.securityConf = securityConf;
   }
   public void setSecurityManager(ObjectName securityManager) {
      this.securityManager = securityManager;
   }
   
   public void startService() throws Exception {

      if (securityManager != null) {
         // Set securityConf at manager
         getServer().invoke(securityManager,"addDestination", new Object[]{spyDest.getName(),securityConf}, new String[] {"java.lang.String","org.w3c.dom.Element"});
         }
      
   }
   
   public void stopService() throws Exception
   {
      // unbind from JNDI
      if (jndiBound) {
         InitialContext ctx = new InitialContext();
         try {
            Util.unbind(ctx, jndiName);
         }
         finally {
            ctx.close();
         }
         jndiName = null;
         jndiBound = false;
      }

      // TODO: need to remove from JMSServer
      if (securityManager != null) {
         // Set securityConf at manager
         getServer().invoke(securityManager,"removeDestination", new Object[]{spyDest.getName()}, new String[] {"java.lang.String"});
      }
   }
   
   protected void destroyService() throws Exception
   {
      JMSDestinationManager jmsServer = (JMSDestinationManager)
         server.getAttribute(jbossMQService, "Interceptor");

      jmsServer.closeDestination(spyDest);      
   }
   
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      destinationName = name.getKeyProperty("name");
      if (destinationName == null || destinationName.length() == 0)
      {
         throw new MalformedObjectNameException("Property 'name' not provided");
      }
      
      // re-setup the logger with a more descriptive name
      log = Logger.getLogger(getClass().getName() + "." + destinationName);
      
      return name;
   }
   
   /**
    * Sets the JNDI name for this topic
    * 
    * @param name Name to bind this topic to in the JNDI tree
    */
   public synchronized void setJNDIName(String name) throws Exception {
      if (spyDest == null) { // nothing to bind yet, startService will recall us
         jndiName = name;
         return;
      }
      
      if (name == null) {
         throw new InvalidAttributeValueException("Destination JNDI names can not be null");
      }
      
      InitialContext ic = new InitialContext();
      try {
         if (jndiName != null && jndiBound){
            Util.unbind(ic,jndiName); //Remove old jndi name
            jndiName = null;
            jndiBound = false;
         }
         
         Util.rebind(ic,name,spyDest);
         jndiName = name;
         jndiBound = true;
      }
      finally {
         ic.close();
      }
      
      log.info("Bound to JNDI name: " + jndiName);
   }
      
   /**
    * Gets the JNDI name use by this topic
    * 
    * @return  The JNDI name currently in use
    */
   public String getJNDIName(){
      return jndiName;
   }
   
} // DestinationManager
