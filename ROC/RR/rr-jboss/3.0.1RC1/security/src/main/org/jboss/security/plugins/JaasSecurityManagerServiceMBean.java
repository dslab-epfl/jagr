/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.security.plugins;

import javax.management.ObjectName;

import org.jboss.util.jmx.ObjectNameFactory;
import org.jboss.security.SecurityDomain;
import org.jboss.system.ServiceMBean;

/**
 * The interface for the JaasSecurityManagerService mbean.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public interface JaasSecurityManagerServiceMBean
   extends ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.security:service=JaasSecurityManager");

   /**
    * Get the name of the class that provides the security manager implementation.
    */
   String getSecurityManagerClassName();
   
   /**
    * Set the name of the class that provides the security manager implementation.
    * 
    * @exception ClassNotFoundException, thrown if the className cannot be found
    * using the thread context class loader.
    * @exception ClassCastException, thrown if the className does not implement the
    * org.jboss.security.AuthenticationManager interface.
    */
   void setSecurityManagerClassName(String className)
      throws ClassNotFoundException, ClassCastException;
   
   /**
    * Get the name of the class that provides the SecurityProxyFactory implementation.
    */
   String getSecurityProxyFactoryClassName();
   
   /**
    * Set the name of the class that provides the SecurityProxyFactory implementation.
    */
   void setSecurityProxyFactoryClassName(String className)
      throws ClassNotFoundException;
   
   /**
    * Get the jndi name under which the authentication CachePolicy implenentation
    * is found
    */
   String getAuthenticationCacheJndiName();
   
   /**
    * Set the location of the security credential cache policy. This is first treated
    * as a ObjectFactory location that is capable of returning CachePolicy instances
    * on a per security domain basis by appending a '/security-domain-name' string
    * to this name when looking up the CachePolicy for a domain. If this fails then
    * the location is treated as a single CachePolicy for all security domains.
    * 
    * @param jndiName, the name to the ObjectFactory or CachePolicy binding.
    */
   void setAuthenticationCacheJndiName(String jndiName);
   
   /**
    * flush the cache policy for the indicated security domain if the security manager
    * instance supports a flushCache() method.
    */
   
   /**
    * Get the default timed cache policy timeout.
    * @return the default cache timeout in seconds.
    */
   int getDefaultCacheTimeout();
   
   /**
    * Set the default timed cache policy timeout. This has no affect if the
    * AuthenticationCacheJndiName has been changed from the default value.
    * @param timeoutInSecs, the cache timeout in seconds.
    */
   void setDefaultCacheTimeout(int timeoutInSecs);
   
   /**
    * Get the default timed cache policy resolution.
    */
   int getDefaultCacheResolution();
   
   /**
    * Set the default timed cache policy resolution. This has no affect if the
    * AuthenticationCacheJndiName has been changed from the default value.
    * 
    * @param resInSecs, resolution of timeouts in seconds.
    */
   void setDefaultCacheResolution(int resInSecs);

   void flushAuthenticationCache(String securityDomain);
   
   /**
    * Register a SecurityDomain implmentation
    */
   void registerSecurityDomain(String securityDomain, SecurityDomain instance);
}
