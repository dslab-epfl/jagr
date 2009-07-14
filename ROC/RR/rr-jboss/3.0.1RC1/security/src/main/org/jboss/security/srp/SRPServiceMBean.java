/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.security.srp;

import javax.naming.NamingException;

import javax.management.ObjectName;

import org.jboss.util.jmx.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * The JMX mbean interface for the SRP service. This mbean sets up an
 * RMI implementation of the 'Secure Remote Password' cryptographic authentication
 * system developed by Tom Wu (tjw@CS.Stanford.EDU). For more info on SRP
 * see http://www-cs-students.stanford.edu/~tjw/srp/.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public interface SRPServiceMBean
   extends ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=SRPService");

   /**
    * Get the jndi name for the SRPVerifierSource implementation binding.
    */
   String getVerifierSourceJndiName();
   
   /**
    * set the jndi name for the SRPVerifierSource implementation binding.
    */
   void setVerifierSourceJndiName(String jndiName);
   
   /**
    * Get the jndi name under which the SRPServerInterface proxy should be bound
    */
   String getJndiName();
   
   /**
    * Set the jndi name under which the SRPServerInterface proxy should be bound
    */
   void setJndiName(String jndiName);
   
   /**
    * Get the jndi name under which the SRPServerInterface proxy should be bound
    */
   String getAuthenticationCacheJndiName();
   
   /**
    * Set the jndi name under which the SRPServerInterface proxy should be bound
    */
   void setAuthenticationCacheJndiName(String jndiName);

   /**
    * Get the auth cache timeout period in seconds
    */
   int getAuthenticationCacheTimeout();
   
   /**
    * Set the auth cache timeout period in seconds
    */
   void setAuthenticationCacheTimeout(int timeoutInSecs);
   
   /**
    * Get the auth cache resolution period in seconds
    */
   int getAuthenticationCacheResolution();
   
   /**
    * Set the auth cache resolution period in seconds
    */
   void setAuthenticationCacheResolution(int resInSecs);

   /**
    * Get the RMIClientSocketFactory implementation class. If null the default
    * RMI client socket factory implementation is used.
    */
   String getClientSocketFactory();
   
   /**
    * Set the RMIClientSocketFactory implementation class. If null the default
    * RMI client socket factory implementation is used.
    */
   void setClientSocketFactory(String factoryClassName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException;
   
   /**
    * Get the RMIServerSocketFactory implementation class. If null the default
    * RMI server socket factory implementation is used.
    */
   String getServerSocketFactory();
   
   /**
    * Set the RMIServerSocketFactory implementation class. If null the default
    * RMI server socket factory implementation is used.
    */
   void setServerSocketFactory(String factoryClassName)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException;
   
   /**
    * Get the RMI port for the SRPRemoteServerInterface
    */
   int getServerPort();
   
   /**
    * Set the RMI port for the SRPRemoteServerInterface
    */
   void setServerPort(int port);
}
