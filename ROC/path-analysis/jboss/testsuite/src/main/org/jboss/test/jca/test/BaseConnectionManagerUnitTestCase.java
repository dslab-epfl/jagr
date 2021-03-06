
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.test.jca.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jboss.logging.Logger;
import org.jboss.resource.connectionmanager.BaseConnectionManager2;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool;
import org.jboss.resource.connectionmanager.CachedConnectionManager;
import org.jboss.resource.connectionmanager.NoTxConnectionManager;
import org.jboss.resource.connectionmanager.InternalManagedConnectionPool;
import org.jboss.resource.connectionmanager.ManagedConnectionPool;
import org.jboss.test.jca.adapter.TestConnectionRequestInfo;
import org.jboss.test.jca.adapter.TestManagedConnectionFactory;

/**
 *  Unit Test for class ManagedConnectionPool
 *
 *
 * Created: Wed Jan  2 00:06:35 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */
public class BaseConnectionManagerUnitTestCase extends TestCase 
{

   Logger log = Logger.getLogger(getClass());


   Subject subject = new Subject();
   ConnectionRequestInfo cri = new TestConnectionRequestInfo();
   CachedConnectionManager ccm = new CachedConnectionManager();


   /** 
    * Creates a new <code>BaseConnectionManagerUnitTestCase</code> instance.
    *
    * @param name test name
    */
   public BaseConnectionManagerUnitTestCase (String name)
   {
      super(name);
   }


   private BaseConnectionManager2 getCM(
      InternalManagedConnectionPool.PoolParams pp, 
      String poolingStrategyName)
      throws Exception
   {
      ManagedConnectionFactory mcf = new TestManagedConnectionFactory();
      JBossManagedConnectionPool mcp = new JBossManagedConnectionPool();
      mcp.setCriteria(poolingStrategyName);
      mcp.setMinSize(pp.minSize);
      mcp.setMaxSize(pp.maxSize);
      mcp.setBlockingTimeoutMillis(pp.blockingTimeout);
      mcp.setIdleTimeout(pp.idleTimeout);
      mcp.start();
      mcp.getManagedConnectionPool().setManagedConnectionFactory(mcf);
      BaseConnectionManager2 cm = new NoTxConnectionManager(mcf, ccm, mcp.getManagedConnectionPool());
      return cm;
   }

   public void testGetManagedConnections() throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = 5;
      pp.blockingTimeout = 100;
      pp.idleTimeout = 500;
      BaseConnectionManager2 cm = getCM(pp, "ByNothing");
      ArrayList cs = new ArrayList();
      for (int i = 0; i < pp.maxSize; i++)
      {
         ManagedConnection mc = cm.getManagedConnection(null, null);
         assertTrue("Got a null connection!", mc != null);
         cs.add(mc);
      } // end of for ()
      assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.maxSize);
      try 
      {
         cm.getManagedConnection(null, null);
         fail("Got a connection more than maxSize!");         
      }
      catch (ResourceException re)
      {
         //expected
      } // end of try-catch
      for (Iterator i = cs.iterator(); i.hasNext();)
      {
         cm.returnManagedConnection((ManagedConnection)i.next(), false);
      } // end of for ()
      cm.shutdown();
      assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == 0);
      
   }


   
   public void testIdleTimeout() throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = 5;
      pp.blockingTimeout = 10;
      pp.idleTimeout = 10;
      BaseConnectionManager2 cm = getCM(pp, "ByNothing");
      Collection mcs = new ArrayList(pp.maxSize);
      for (int i = 0 ; i < pp.maxSize; i++)
      {
         mcs.add(cm.getManagedConnection(subject, cri));
      } // end of for ()
      for (Iterator i =  mcs.iterator(); i.hasNext(); )
      {
         cm.returnManagedConnection((ManagedConnection)i.next(), false);
      } // end of for ()
      
      assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.maxSize);
      Thread.sleep((long)pp.idleTimeout * 10);
      //      cm.removeTimedOut();
      assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == 0);
      

   }

   public void testFillToMin() throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 3;
      pp.maxSize = 5;
      pp.blockingTimeout = 10;
      pp.idleTimeout = 20;
      BaseConnectionManager2 cm = getCM(pp, "ByNothing");
      ManagedConnection mc = cm.getManagedConnection(subject, cri);
      cm.returnManagedConnection(mc, false);
      Thread.sleep(10);//allow filltoMin to work
      assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.minSize);
      Thread.sleep((long)(pp.idleTimeout * 7)/2);//try to get in the middle of cleanups
      //      cm.removeTimedOut();
      assertTrue("Wrong number of connections counted: " + cm.getConnectionCount(), cm.getConnectionCount() == pp.minSize);
      cm.shutdown();

   }



}// 
