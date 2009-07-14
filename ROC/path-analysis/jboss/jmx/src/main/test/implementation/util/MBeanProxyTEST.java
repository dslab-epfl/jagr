/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package test.implementation.util;

import junit.framework.TestCase;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import test.implementation.util.support.Trivial;
import test.implementation.util.support.TrivialMBean;

import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.AgentID;


public class MBeanProxyTEST extends TestCase
{
   public MBeanProxyTEST(String s)
   {
      super(s);
   }

   public void testCreate()
   {
      try 
      {   
         MBeanServer server = MBeanServerFactory.createMBeanServer();
         ObjectName oname   = new ObjectName("test:name=test");
      
         server.registerMBean(new Trivial(), oname);
      
         TrivialMBean mbean = (TrivialMBean)MBeanProxy.get(
               TrivialMBean.class, oname, AgentID.get(server));      
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         fail("unexpected error: " + t.toString());
      }
   }

   public void testProxyInvocations()
   {
      try
      {
         MBeanServer server = MBeanServerFactory.createMBeanServer();
         ObjectName oname   = new ObjectName("test:name=test");
         
         server.registerMBean(new Trivial(), oname);
         
         TrivialMBean mbean = (TrivialMBean)MBeanProxy.get(
               TrivialMBean.class, oname, AgentID.get(server));
         
         mbean.doOperation();
         mbean.setSomething("JBossMX");
         
         assertEquals("JBossMX", mbean.getSomething());
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         fail("unexpected error: " + t.toString());
      }
   }
}
