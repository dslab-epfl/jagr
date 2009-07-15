/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.adaptor.rmi;

import java.net.InetAddress;
import javax.management.ObjectName;

/**
 * RMI Adaptor allowing an network aware client
 * to work directly with a remote JMX Agent.
 *
 * @version $Revision: 1.1.1.1 $
 * @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
 * @author Scott.Stark@jboss.org
 */
public interface RMIAdaptorServiceMBean
   extends org.jboss.system.ServiceMBean
{
   /** Get the JNDI name under which the */
   public String getJndiName();
   /** */
   public void setJndiName(String jndiName);

   public void setRMIObjectPort(int rmiPort); 
   public int getRMIObjectPort();

   public void setServerAddress(String address);
   public String getServerAddress();

   public int getBacklog();
   public void setBacklog(int backlog);

   /** The legacy hard-coded JNDI binding name. */
   String getLegacyJndiName();
}
