package org.jboss.invocation.jrmp.server;

import org.jboss.system.ServiceMBean;

/**
 @author Scott.Stark@jboss.org
 @version $Revision: 1.1.1.1 $
 */
public interface JRMPInvokerMBean
   extends ServiceMBean
{
   
   public void setRMIObjectPort(int rmiPort); 
   public int getRMIObjectPort();
   
   public void setRMIClientSocketFactory(String name);
   public String getRMIClientSocketFactory();
   
   public void setRMIServerSocketFactory(String name);
   public String getRMIServerSocketFactory();
   
   public void setServerAddress(String address);
   public String getServerAddress();

   /** Set the security domain name to use with SSL aware socket factories
    */
   public void setSecurityDomain(String domainName);
   public String getSecurityDomain();

   public String getName();

   public int getBacklog();
   public void setBacklog(int backlog);
   
}
   
