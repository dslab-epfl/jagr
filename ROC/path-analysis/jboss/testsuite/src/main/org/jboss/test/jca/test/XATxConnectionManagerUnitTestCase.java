
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.test.jca.test; // Generated package name

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jboss.logging.Logger;
import org.jboss.resource.connectionmanager.BaseConnectionManager2;
import org.jboss.resource.connectionmanager.CachedConnectionManager;
import org.jboss.resource.connectionmanager.InternalManagedConnectionPool;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool;
import org.jboss.resource.connectionmanager.ManagedConnectionPool;
import org.jboss.resource.connectionmanager.XATxConnectionManager;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.adapter.TestConnection;
import org.jboss.test.jca.adapter.TestConnectionRequestInfo;
import org.jboss.test.jca.adapter.TestManagedConnectionFactory;
import org.jboss.tm.TxManager;
import org.jboss.tm.usertx.client.ServerVMClientUserTransaction;

/**
 * XATxConnectionManagerUnitTestCase.java
 *
 *
 * Created: Mon Jan 14 00:43:40 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class XATxConnectionManagerUnitTestCase extends JBossTestCase 
{
   Logger log = Logger.getLogger(getClass());

   private TransactionManager tm;
   private ServerVMClientUserTransaction ut;
   private CachedConnectionManager ccm;
   private TestManagedConnectionFactory mcf;
   private XATxConnectionManager cm;
   private ConnectionRequestInfo cri;

   public XATxConnectionManagerUnitTestCase (String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      tm = TxManager.getInstance();
      ut = new ServerVMClientUserTransaction(tm);
      ccm = new CachedConnectionManager();
      ut.registerTxStartedListener(ccm);

      mcf = new TestManagedConnectionFactory();
      JBossManagedConnectionPool mcp = new JBossManagedConnectionPool();
      mcp.setCriteria("ByNothing");
      mcp.setMinSize(0);
      mcp.setMaxSize(5);
      mcp.setBlockingTimeoutMillis(100);
      mcp.setIdleTimeout(500);
      mcp.start();
      Subject subject = new Subject();
      cri = new TestConnectionRequestInfo();
      mcp.getManagedConnectionPool().setManagedConnectionFactory(mcf);
      cm = new XATxConnectionManager(mcf, ccm, mcp.getManagedConnectionPool(), tm);

   }

   protected void tearDown() throws Exception
   {
   }

   public void testGetConnection() throws Exception
   {
      getLog().info("testGetConnection");
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
      assertTrue("Connection is null", c != null);
      c.close();
   }

   public void testEnlistInExistingTx() throws Exception
   {
      getLog().info("testEnlistInExistingTx");
      ut.begin();
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
      assertTrue("Connection not enlisted in tx!", c.isInTx());
      c.close();
      assertTrue("Connection still enlisted in tx!", !c.isInTx());
      ut.commit();
      assertTrue("Connection still enlisted in tx!", !c.isInTx());
   }

   public void testEnlistCheckedOutConnectionInNewTx() throws Exception
   {
      getLog().info("testEnlistCheckedOutConnectionInNewTx");
      Object key = this;
      Set unshared = new HashSet();
      ccm.pushMetaAwareObject(key, unshared);
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
      assertTrue("Connection already enlisted in tx!", !c.isInTx());
      ut.begin();
      assertTrue("Connection not enlisted in tx!", c.isInTx());

      ut.commit();
      assertTrue("Connection still enlisted in tx!", !c.isInTx());
      c.close();
      ccm.popMetaAwareObject(unshared);
   }

   public void testReconnectConnectionHandlesOnNotification() throws Exception
   {
      getLog().info("testReconnectConnectionHandlesOnNotification");
      Object key1 = new Object();
      Object key2 = new Object();
      Set unshared = new HashSet();
      ccm.pushMetaAwareObject(key1, unshared);
      ut.begin();
      ccm.pushMetaAwareObject(key2, unshared);
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
      assertTrue("Connection not enlisted in tx!", c.isInTx());
      ccm.popMetaAwareObject(unshared);//key2
      ut.commit();
      ut.begin();
      ccm.pushMetaAwareObject(key2, unshared);
      assertTrue("Connection not enlisted in tx!", c.isInTx());

      ccm.popMetaAwareObject(unshared);//key2
      ut.commit();
      assertTrue("Connection still enlisted in tx!", !c.isInTx());
      ccm.pushMetaAwareObject(key2, unshared);
      c.close();
      ccm.popMetaAwareObject(unshared);//key2
      ccm.popMetaAwareObject(unshared);//key1
  }
   
}// XATxConnectionManagerUnitTestCase
