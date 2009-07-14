/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy.ejb;

import java.util.HashMap;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerHA;
import org.jboss.logging.Logger;
import org.jboss.ha.framework.interfaces.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.server.HATarget;
import org.jboss.system.Registry;

/** An extension of ProxyFactory that supports clustering of invoker proxies.
 * For a given EJB container mbean there must be an InvokerHA implementation
 * registered under the container JMX ObjectName hash. This is used to create
 * the HA capablable invoker proxies.
 *
 *  @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *  @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 *  @version $Revision: 1.1.1.1 $
 */
public class ProxyFactoryHA extends ProxyFactory
{
   protected InvokerHA homejrmp;
   protected InvokerHA beanjrmp;
   protected HATarget homeTarget;
   protected HATarget beanTarget;
   
   protected void setupInvokers() throws Exception
   {
      String partitionName = container.getBeanMetaData().getClusterConfigMetaData().getPartitionName();
      HAPartition partition = (HAPartition)new InitialContext().lookup("/HAPartition/" + partitionName);
      
      ObjectName oname;
      
      // Get the local invoker
      oname = new ObjectName(container.getBeanMetaData().getHomeInvoker());
      homejrmp = (InvokerHA)Registry.lookup(oname);
      if (homejrmp == null)
         throw new RuntimeException("home InvokerHA is null: " + oname);
      
      oname = new ObjectName(container.getBeanMetaData().getBeanInvoker());
      beanjrmp = (InvokerHA)Registry.lookup(oname);
      if (beanjrmp == null)
         throw new RuntimeException("bean InvokerHA is null: " + oname);
      
      // If invokers are the same don't register twice.
      if (homejrmp == beanjrmp)
      {
         homeTarget = new HATarget(partition, jmxName.toString(), homejrmp.getStub ());
         beanTarget = homeTarget;
         homejrmp.registerBean(jmxName, homeTarget);
      }
      else
      {
         homeTarget = new HATarget(partition, jmxName + "/home", homejrmp.getStub ());
         homejrmp.registerBean(jmxName, homeTarget);
         beanTarget = new HATarget(partition, jmxName + "/bean", beanjrmp.getStub ());
         beanjrmp.registerBean(jmxName, beanTarget);
      }
      
      
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class clazz;
      LoadBalancePolicy policy;
      
      clazz = cl.loadClass(container.getBeanMetaData().getClusterConfigMetaData().getHomeLoadBalancePolicy());
      policy = (LoadBalancePolicy)clazz.newInstance();
      homeInvoker = homejrmp.createProxy(jmxName, policy);
      
      clazz = cl.loadClass(container.getBeanMetaData().getClusterConfigMetaData().getBeanLoadBalancePolicy());
      policy = (LoadBalancePolicy)clazz.newInstance();
      beanInvoker = beanjrmp.createProxy(jmxName, policy);
   }
   
   public void destroy()
   {
      super.destroy();
      try
      {
         if (homejrmp == beanjrmp)
         {
            homejrmp.unregisterBean(jmxName);
            homeTarget.destroy();
         }
         else
         {
            homejrmp.unregisterBean(jmxName);
            beanjrmp.unregisterBean(jmxName);
            homeTarget.destroy();
            beanTarget.destroy();
         }
      } 
      catch (Exception e)
      {
         // ignore.
      }
   }

}
