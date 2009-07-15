/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package test.implementation.modelmbean;

import java.net.URL;

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.mx.modelmbean.XMBean;
import org.jboss.mx.modelmbean.XMBeanConstants;

import test.implementation.modelmbean.support.User;
import test.implementation.modelmbean.support.Trivial;
import test.implementation.modelmbean.support.TrivialMBean;

/**
 * Here are some basic XMBean tests, mainly to demonstrate the use of the
 * XMBean class and the MBean creation (this is the doc ;)
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 */
public class XMBeanTEST extends TestCase
{
   public XMBeanTEST(String s)
   {
      super(s);
   }

   public void testCreateXMBean() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      XMBean mmb = new XMBean(new User(), "file:./src/main/test/implementation/modelmbean/support/xml/UserManagementInterface.xml");
      
      ObjectName name = new ObjectName(":test=test");
      
      server.registerMBean(mmb, name);     
      assertTrue(server.isRegistered(name));
      
      server.setAttribute(name, new Attribute("Name", "Juha"));
      assertTrue(server.getAttribute(name, "Name").equals("Juha"));
      
      server.setAttribute(name, new Attribute("Address", "StrawBerry Street"));
      assertTrue(server.getAttribute(name, "Address").equals("StrawBerry Street"));
      
      assertTrue(server.invoke(name, "printInfo", null, null) instanceof String);
   }

   public void testCreateWithJBossXMBean10DTD() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      URL url = new URL("file:./src/main/test/implementation/modelmbean/support/xml/User.xml");
      
      XMBean mmb = new XMBean(new User(), url);
      server.registerMBean(mmb, new ObjectName(":test=test"));     
      
      assertTrue(server.isRegistered(new ObjectName(":test=test")));
      
      server.setAttribute(new ObjectName(":test=test"), new Attribute("Name", "Juha"));
      
      assertTrue(server.getAttribute(new ObjectName(":test=test"), "Name").equals("Juha"));
      
   }
   
   public void testCreateWithStandardInterface() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      Trivial trivial = new Trivial();
      ObjectName name = new ObjectName(":foo=bar");
      
      XMBean mmb = new XMBean(trivial, XMBeanConstants.STANDARD_INTERFACE);
      server.registerMBean(mmb, name);
      
      assertTrue(server.isRegistered(new ObjectName(":foo=bar")));
      
      server.setAttribute(name, new Attribute("Something", "foobar"));
      assertTrue(server.getAttribute(name, "Something").equals("foobar"));
      
      Boolean b = (Boolean)server.invoke(name, "doOperation", new Object[] { "" }, new String[] { "java.lang.String" });
      assertTrue(b.booleanValue() == true);
   }
   
   
}
