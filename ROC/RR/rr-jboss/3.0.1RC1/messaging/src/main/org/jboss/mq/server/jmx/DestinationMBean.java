/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.mq.server.jmx;

import javax.management.ObjectName;
import org.jboss.system.ServiceMBean;
/**
 * MBean interface for destination managers.
 *
 *
 * @author  <a href="pra@tim.se">Peter Antman</a>
 * @version $Revision: 1.1.1.1 $
 */

public interface DestinationMBean extends ServiceMBean  
{
   /**
    * Get the value of JBossMQService.
    * @return value of JBossMQService.
    */
   void removeAllMessages() throws Exception; 
   
   /**
    * Get the value of JBossMQService.
    * @return value of JBossMQService.
    */
   ObjectName getDestinationManager(); 
   
   /**
    * Set the value of JBossMQService.
    * @param v  Value to assign to JBossMQService.
    */
   void setDestinationManager(ObjectName  jbossMQService); 

    /**
    * Sets the JNDI name for this destination
    * @param name Name to bind this topic to in the JNDI tree
    */
   void setJNDIName(String name) throws Exception;

   /**
    * Gets the JNDI name use by this destination.
    * @return  The JNDI name currently in use
    */
   String getJNDIName();
   
   /**
    * Sets the security xml config
    */
   //void setSecurityConf(String securityConf) throws Exception;
   void setSecurityConf(org.w3c.dom.Element securityConf) throws Exception;
   /**
    * Set the object name of the security manager.
    */
   public void setSecurityManager(ObjectName securityManager);

} // DestinationManagerMBean
