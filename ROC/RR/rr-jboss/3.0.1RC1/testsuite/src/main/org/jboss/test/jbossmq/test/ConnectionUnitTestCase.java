/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.jbossmq.test;

import junit.framework.Test;
import junit.framework.TestCase;
import javax.jms.*;
import org.jboss.mq.SpyConnectionFactory;
import org.jboss.test.JBossTestCase;
import java.util.Properties;
import org.jboss.mq.il.oil.OILServerILFactory;
import org.jboss.mq.il.uil.UILServerILFactory;
import org.jboss.mq.SpyXAConnectionFactory;
import javax.naming.*;

/** 
 * Test all the verious ways that a connection can be 
 * established with JBossMQ
 *
 * @author hiram.chirino@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class ConnectionUnitTestCase extends JBossTestCase {

   public ConnectionUnitTestCase(String name) {
      super(name);
   }

   protected void setUp() throws Exception {
   }

   public void testMultipleOILConnectViaJNDI() throws Exception {
      
      getLog().debug("Starting testMultipleOILConnectViaJNDI");

      InitialContext ctx = new InitialContext();

      QueueConnectionFactory cf = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");

		QueueConnection connections[] = new QueueConnection[10];
		
      getLog().debug("Creating "+connections.length+" connections.");
     for( int i=0; i < connections.length; i++ ) {
	     connections[i] = cf.createQueueConnection();
      }   

      getLog().debug("Closing the connections.");
     for( int i=0; i < connections.length; i++ ) {
	     connections[i].close();
      }   
      
      getLog().debug("Finished testMultipleOILConnectViaJNDI");
   }

   public void testOILConnectViaJNDI() throws Exception {
      InitialContext ctx = new InitialContext();

      QueueConnectionFactory cf = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
      QueueConnection c = cf.createQueueConnection();
      c.close();

      XAQueueConnectionFactory xacf = (XAQueueConnectionFactory) ctx.lookup("XAConnectionFactory");
      XAQueueConnection xac = xacf.createXAQueueConnection();
      xac.close();
   }

   public void testOILConnectNoJNDI() throws Exception {

      Properties props = new Properties();
      props.setProperty(OILServerILFactory.SERVER_IL_FACTORY_KEY, OILServerILFactory.SERVER_IL_FACTORY);
      props.setProperty(OILServerILFactory.CLIENT_IL_SERVICE_KEY, OILServerILFactory.CLIENT_IL_SERVICE);
      props.setProperty(OILServerILFactory.PING_PERIOD_KEY, "1000");
      props.setProperty(OILServerILFactory.OIL_ADDRESS_KEY, "localhost");
      props.setProperty(OILServerILFactory.OIL_PORT_KEY, "8090");

      QueueConnectionFactory cf = new SpyConnectionFactory(props);
      QueueConnection c = cf.createQueueConnection();
      c.close();

      XAQueueConnectionFactory xacf = new SpyXAConnectionFactory(props);
      XAQueueConnection xac = xacf.createXAQueueConnection();
      xac.close();

   }

   public void testUILConnectViaJNDI() throws Exception {
      InitialContext ctx = new InitialContext();

      QueueConnectionFactory cf = (QueueConnectionFactory) ctx.lookup("UILConnectionFactory");
      QueueConnection c = cf.createQueueConnection();
      c.close();

      XAQueueConnectionFactory xacf = (XAQueueConnectionFactory) ctx.lookup("UILXAConnectionFactory");
      XAQueueConnection xac = xacf.createXAQueueConnection();
      xac.close();
   }

   public void testUILConnectNoJNDI() throws Exception {

      Properties props = new Properties();
      props.setProperty(UILServerILFactory.SERVER_IL_FACTORY_KEY, UILServerILFactory.SERVER_IL_FACTORY);
      props.setProperty(UILServerILFactory.CLIENT_IL_SERVICE_KEY, UILServerILFactory.CLIENT_IL_SERVICE);
      props.setProperty(UILServerILFactory.PING_PERIOD_KEY, "1000");
      props.setProperty(UILServerILFactory.UIL_ADDRESS_KEY, "localhost");
      props.setProperty(UILServerILFactory.UIL_PORT_KEY, "8091");

      QueueConnectionFactory cf = new SpyConnectionFactory(props);
      QueueConnection c = cf.createQueueConnection();
      c.close();

      XAQueueConnectionFactory xacf = new SpyXAConnectionFactory(props);
      XAQueueConnection xac = xacf.createXAQueueConnection();
      xac.close();
   }

   public static void main(java.lang.String[] args) {
      junit.textui.TestRunner.run(ConnectionUnitTestCase.class);
   }
}