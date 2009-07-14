/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.system.server;

import javax.management.ObjectName;

import org.jboss.util.jmx.MBeanProxy;

/**
 * A helper for locating the {@link ServerConfig} instance 
 * for the running server.
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ServerConfigLocator
{
   private static volatile ServerConfig instance = null;

   public static ServerConfig locate()
   {
      if (instance == null) {
         instance = (ServerConfig)
            MBeanProxy.create(ServerConfig.class,
                              ServerConfigImplMBean.OBJECT_NAME);
      }

      return instance;
   }
}
