/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jmx.adaptor.rmi;

import java.net.InetAddress;
import javax.naming.InitialContext;

import org.jboss.naming.Util;
import org.jboss.system.ServiceMBeanSupport;

/**
 *   <description>
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
 * @author Scott.Stark@jboss.org
 **/
public class RMIAdaptorService
   extends ServiceMBeanSupport
   implements RMIAdaptorServiceMBean
{
   // Constants -----------------------------------------------------
   public static String JNDI_NAME = "jmx:rmi";
   public static String JMX_NAME = "jmx";
   public static String PROTOCOL_NAME = "rmi";

   // Attributes ----------------------------------------------------
   private String jndiName;
   /** The port the container will be exported on */
   private int rmiPort = 0;
   /** The port the container will be exported on */
   private int backlog = 50;
   /** The address to bind the rmi port on */
   protected String serverAddress;

   private RMIAdaptorImpl adaptor;
   private String host;
   private String name;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public RMIAdaptorService()
   {
      name = null;
   }
   
   public RMIAdaptorService(String name)
   {
      name = name;
   }

   // Public --------------------------------------------------------
   
   public String getJndiName()
   {
      return jndiName;
   }
   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public int getBacklog()
   {
      return backlog;
   }
   public void setBacklog(int back)
   {
      backlog = back;
   }

   public void setRMIObjectPort(final int rmiPort)
   {
      this.rmiPort = rmiPort;
   }
   public int getRMIObjectPort()
   {
      return rmiPort;
   }

   public void setServerAddress(final String address)
   {
      serverAddress = address;
   }

   public String getServerAddress()
   {
      return serverAddress;
   }

   public String getLegacyJndiName()
   {
      if (name != null)
      {
         return JMX_NAME + ":" + host + ":" + PROTOCOL_NAME + ":" + name;
      }
      else
      {
         return JMX_NAME + ":" + host + ":" + PROTOCOL_NAME;
      }
   }

   // Protected -----------------------------------------------------
   
   protected void startService() throws Exception
   {
      // Setup the RMI server object
      InetAddress bindAddress = null;
      if( serverAddress != null && serverAddress.length() > 0 )
         bindAddress = InetAddress.getByName(serverAddress);
      adaptor = new RMIAdaptorImpl(getServer(), rmiPort, bindAddress, backlog);
      log.debug("Created RMIAdaptorImpl: "+adaptor);
      InitialContext iniCtx = new InitialContext();

      // Bind the RMI object under the JndiName attribute
      Util.bind(iniCtx, jndiName, adaptor);
      // Bind under the hard-coded legacy name for compatibility
      host = InetAddress.getLocalHost().getHostName();
      String legacyName = getLegacyJndiName();
      iniCtx.bind(legacyName, adaptor);
   }

   protected void stopService()
   {
      try
      {
         new InitialContext().unbind( getLegacyJndiName() );
      }
      catch( Exception e )
      {
         log.debug("Failed to unbind legacy JNDI name", e);
      }
   }
}
