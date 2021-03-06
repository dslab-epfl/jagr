/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.cts.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Properties;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.rmi.PortableRemoteObject;

import org.jboss.test.cts.interfaces.StatefulSession;
import org.jboss.test.cts.interfaces.StatefulSessionHome;
import org.jboss.test.cts.interfaces.BeanContextInfo;
import org.jboss.test.cts.keys.AccountPK;

import org.apache.log4j.Category;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;


/** Tests of stateful session beans
 *
 *   @author kimptoc 
 *   @author Scott.Stark@jboss.org 
 *   @author d_jencks converted to JBossTestCase, added logging.
 *   @version $Revision: 1.1.1.1 $
 */
public class StatefulSessionUnitTestCase
   extends JBossTestCase
{
   public StatefulSessionUnitTestCase (String name)
   {
      super(name);
   }

	/** Create a StatefulSessionBean and then create a local interface to
    an entity in the session. This bean is then passivated by creating another
    session bean and then activated by invoking a business method. The
    purpose is to test that the session is passivated and that the local
    interface is restored.
     */
	public void testLocalInterfacePassivation() throws Exception
	{
      Context ctx = new InitialContext();
      getLog().debug("+++ testLocalInterfacePassivation");
      StatefulSessionHome sessionHome = ( StatefulSessionHome ) ctx.lookup("ejbcts/StatefulSessionBean");
      StatefulSession sessionBean = sessionHome.create("testLocalInterfacePassivation");		

		getLog().debug("Creating local home");
      AccountPK pk = new AccountPK("123456789");
      sessionBean.createLocalEntity(pk, "jduke");

      getLog().debug("Creating a second session bean, forcing the first one to be passivated?");
      // The pool size has been set to 1 in the container
      // config, so creating another ASession here should
      // cause the first one to be passivated.
      StatefulSession anotherSession = sessionHome.create("testLocalInterfacePassivation2");
      getLog().debug("OK");

      getLog().debug("Checking for complete passivation/activation");
      Thread.sleep(1000);
      assertTrue(sessionBean.getWasPassivated() == true);
      getLog().debug("OK");

      // Some other checks
      getLog().debug("Checking reactivation of session bean attributes");
      String name = sessionBean.readAndRemoveEntity();
      assertTrue(name.equals("jduke"));
      getLog().debug("OK");
	}

	/** Create a StatefulSessionBean and then create a handle to a session
    bean in the session. This bean is then passivated by creating another
    session bean and then activated by invoking a business method. The
    purpose is to test that the session is passivated and that the session
    handle can be used to restore the session bean reference.
     */
	public void testSessionRefPassivation() throws Exception
	{
      Context ctx = new InitialContext();
      getLog().debug("+++ testSessionRefPassivation");
      StatefulSessionHome sessionHome = ( StatefulSessionHome ) ctx.lookup("ejbcts/StatefulSessionBean");
      StatefulSession sessionBean = sessionHome.create("testSessionRefPassivation");		

		getLog().debug("Creating session ref");
      sessionBean.createSessionRef();

      getLog().debug("Creating a second session bean, forcing the first one to be passivated?");
      // The pool size has been set to 1 in the container
      // config, so creating another ASession here should
      // cause the first one to be passivated.
      StatefulSession anotherSession = sessionHome.create("testSessionRefPassivation2");
      getLog().debug("OK");

      getLog().debug("Checking for complete passivation/activation");
      Thread.sleep(1000);
      assertTrue(sessionBean.getWasPassivated() == true);
      getLog().debug("OK");

      // Some other checks
      getLog().debug("Checking reactivation of session ref");
      String handle = sessionBean.useSessionRef();
      assertTrue(handle != null);
      getLog().debug("OK");
	}

	/** Create a StatefulSessionBean and then create a handle to a session
    bean in the session. This bean is then passivated by creating another
    session bean and then activated by invoking a business method. The
    purpose is to test that the session is passivated and that the session
    handle can be used to restore the session bean reference.
     */
	public void testSessionHandlePassivation() throws Exception
	{
      Context ctx = new InitialContext();
      getLog().debug("+++ testSessionHandlePassivation");
      StatefulSessionHome sessionHome = ( StatefulSessionHome ) ctx.lookup("ejbcts/StatefulSessionBean");
      StatefulSession sessionBean = sessionHome.create("testSessionHandlePassivation");		

		getLog().debug("Creating session handle");
      sessionBean.createSessionHandle();

      getLog().debug("Creating a second session bean, forcing the first one to be passivated?");
      // The pool size has been set to 1 in the container
      // config, so creating another ASession here should
      // cause the first one to be passivated.
      StatefulSession anotherSession = sessionHome.create("testSessionHandlePassivation2");
      getLog().debug("OK");

      getLog().debug("Checking for complete passivation/activation");
      Thread.sleep(1000);
      assertTrue(sessionBean.getWasPassivated() == true);
      getLog().debug("OK");

      // Some other checks
      getLog().debug("Checking reactivation of session bean handle");
      String name = sessionBean.useSessionHandle("Hello");
      assertTrue(name.equals("Hello"));
      getLog().debug("OK");
	}

	/** Create a StatefulSessionBean and then create a handle to a stateful
    session bean in the session. The handle is serialized into an in memory
    array to test serialization of handles in the absence of custom marshalling
    streams.
     */
	public void testInVMSessionHandlePassivation() throws Exception
	{
      Context ctx = new InitialContext();
      getLog().debug("+++ testInVMSessionHandlePassivation");
      StatefulSessionHome sessionHome = (StatefulSessionHome) ctx.lookup("ejbcts/StatefulSessionBean");
      StatefulSession sessionBean = sessionHome.create("testInVMSessionHandlePassivation");		

		getLog().debug("Creating stateful session handle");
      sessionBean.createStatefulSessionHandle("testInVMSessionHandlePassivation2");
      sessionBean.useStatefulSessionHandle();
   }

   /** Create a StatefulSessionBean and then force passivation by creating
    another session bean and then activated by invoking a business method.
    This relies on a custom container config that specifies
    container-cache-conf/cache-policy-conf/max-capacity=1
     */
   public void testPassivationBySize() throws Exception
   {
      Context ctx = new InitialContext();
      getLog().debug("+++ testPassivationBySize");
      StatefulSessionHome sessionHome = ( StatefulSessionHome ) ctx.lookup("ejbcts/StatefulSessionBean");
      StatefulSession sessionBean1 = sessionHome.create("testPassivationBySize");		
      sessionBean1.method1("hello");

      // Create a second bean to force passivation
      StatefulSession sessionBean2 = sessionHome.create("testPassivationBySize2");
      sessionBean2.method1("hello");

      // Validate that sessionBean1 was passivated and activated
      Thread.sleep(1000);
      boolean passivated = sessionBean1.getWasPassivated();
      assertTrue("sessionBean1 WasPassivated", passivated);
      boolean activated = sessionBean1.getWasActivated();
      assertTrue("sessionBean1 WasActivated", activated);
   }

   /** Create a StatefulSessionBean and then force passivation by waiting
    for 45 seconds.
    This relies on a custom container config that specifies
    container-cache-conf/cache-policy-conf/remover-period=65
    container-cache-conf/cache-policy-conf/overager-period=40
    container-cache-conf/cache-policy-conf/max-bean-age=30
    container-cache-conf/cache-policy-conf/max-bean-life=60
     */
   public void testPassivationByTime() throws Exception
   {
      Context ctx = new InitialContext();
      getLog().debug("+++ testPassivationByTime");
      StatefulSessionHome sessionHome = ( StatefulSessionHome ) ctx.lookup("ejbcts/StatefulSessionBean");
      StatefulSession sessionBean1 = sessionHome.create("testPassivationByTime");		
      sessionBean1.method1("hello");

      getLog().debug("Waiting 41 seconds for passivation...");
      Thread.currentThread().sleep(41*1000);

      // Validate that sessionBean1 was passivated and activated
      boolean passivated = sessionBean1.getWasPassivated();
      assertTrue("sessionBean1 WasPassivated", passivated);
      boolean activated = sessionBean1.getWasActivated();
      assertTrue("sessionBean1 WasActivated", activated);

      getLog().debug("Waiting 90 seconds for removal due to age...");
      Thread.currentThread().sleep(90*1000);
      try
      {
         passivated = sessionBean1.getWasPassivated();
         fail("Was able to invoke getWasPassivated after bean should have been removed");
      }
      catch(Exception e)
      {
         getLog().debug("Bean access failed as expected", e);
      }
   }

   /**
    * EJB 1.1 (Page 40)
    *  "The container is responsible for making the home interfaces
    *   of its deployed enterprise of its deployed enterprise beans
    *   available to the client through JNDI API extension.
    */
   public void testBasicSession ()
      throws Exception
   {
      getLog().debug("+++ testBasicSession()");
      Context ctx = new InitialContext();
      StatefulSessionHome sessionHome        =
         ( StatefulSessionHome ) ctx.lookup("ejbcts/StatefulSessionBean");
      StatefulSession sessionBean = sessionHome.create("testBasicSession");		
      String result = sessionBean.method1("CTS-Test");

      // Test response
      assertTrue(result.equals("CTS-Test"));

      try
      {
         sessionBean.remove();
      }
      catch (Exception ex)
      {
          fail("could not remove stateless session bean" + ex);
      }
   }

   //

   /**
    * Method testEJBHomeInterface
    *  EJB 1.1 (Page 42)
    *  "The home interface allows a client to do the following:"
    *   - Create a new session object
    *   - Remove session object
    *   - Get the javax.ejb.EJBMetaData interface for the
    *     session bean.
    *   - Obtain a handle for the home interface
    *
    * @throws Exception
    *
    */
   public void testEJBHomeInterface ()
      throws Exception
   {
      getLog().debug("+++ testEJBHomeInterface()");

      // Create a new session object
      Context ctx = new InitialContext();
      StatefulSessionHome sessionHome =
         ( StatefulSessionHome ) ctx.lookup("ejbcts/StatefulSessionBean");
      StatefulSession sessionBean = sessionHome.create("testEJBHomeInterface");		

      // Get the EJBMetaData
      javax.ejb.EJBMetaData md = sessionHome.getEJBMetaData();

      getLog().debug("Verify EJBMetaData from home interface");
      assertTrue(md != null);

      // Get the EJBMetaData constructs
      getLog().debug("Get Home interface class");

      java.lang.Class homeInterface = md.getHomeInterfaceClass();

      getLog().debug("home Interface : " + homeInterface.getName());
      assertTrue(homeInterface.getName().equals("org.jboss.test.cts.interfaces.StatefulSessionHome"));
      getLog().debug("Get Remote Interface class");

      java.lang.Class remoteInterface = md.getRemoteInterfaceClass();

      getLog().debug("remote Interface: " + remoteInterface.getName());
      assertTrue(remoteInterface.getName().equals("org.jboss.test.cts.interfaces.StatefulSession"));
      getLog().debug("Verify isSession..");
      assertTrue(md.isSession());

      // EJB 1.1 only
      getLog().debug("Verify is not Stateless session...");
      assertTrue(!md.isStatelessSession());

      try
      {
         sessionBean.remove();
      }
      catch (Exception ex)
      {
          fail("could not remove stateful session bean" + ex);
      }
   }

   /**
    * Method testRemoveSessionObject
    * EJB 1.1 (Page 42)
    * Removing a Session Object.
    * Because session objects do not have primary keys that are
    * accessible to clients, invoking the javax.ejb.Home.remove( Object primaryKey )
    * method on a session results in the javax.ejb.RemoveException.
    *
    */
   public void testRemoveSessionObject ()
      throws Exception
   {
      getLog().debug("+++ testRemoveSessionObject()");
      Context ctx  = new InitialContext();
      StatefulSessionHome home = ( StatefulSessionHome ) ctx.lookup("ejbcts/StatefulSessionBean");
      StatefulSession bean = home.create("testRemoveSessionObject");		
      getLog().debug("OK");
      getLog().debug("Call remove using a primary key");
      try
      {
         home.remove(new AccountPK("pk"));
         fail("[EJB 1.1, p42, section 5.3.2] Expected 'RemoveException' when remove-ing a session object, got NO exception");
      }
      catch(RemoveException e)
      {
         getLog().debug("Remove using a primary key failed as expected");
      }
   }

   //-------------------------------------------------------------------------
   //-------------------------------------------------------------------------

   /**
    * Method testCompareSerializeGetPK
    * EJB 1.1 [5.5] Page 43
    * EJBOjbect.getPrimaryKey results in a RemoteException
    * Get a serializable handle
    * Compare on bean to another for equality
    *
    *
    */
   public void testCompareSerializeGetPK ()
      throws Exception
   {
      getLog().debug("+++ testCompareSerializeGetPK()");

      Context ctx  = new InitialContext();
      StatefulSessionHome home = ( StatefulSessionHome ) ctx.lookup("ejbcts/StatefulSessionBean");
      StatefulSession bean = home.create("testCompareSerializeGetPK");		

      // Get the bean handle
      Handle hn = bean.getHandle();

      assertTrue(hn != null);

      // "Copy" the bean
      StatefulSession theOtherBean =
         ( StatefulSession ) javax.rmi.PortableRemoteObject.narrow(
            hn.getEJBObject(), StatefulSession.class);

      assertTrue(theOtherBean != null);
      assertTrue(bean.isIdentical(theOtherBean));
   }

   /**
    * Method testSerialization
    * EJB 1.1 [5.7] Page 45
    * Session bean must be serializable.
    */
   public void testSerialization ()
      throws Exception
   {
      getLog().debug("+++ testSerialize");
      Context ctx  = new InitialContext();
      StatefulSessionHome home = ( StatefulSessionHome ) ctx.lookup("ejbcts/StatefulSessionBean");
      StatefulSession bean = home.create("testSerialization");		
      getLog().debug("Increment bean, count = 3");

      // put on some state...
      bean.setCounter(1);
      bean.incCounter();
      bean.incCounter();

      // bean should be=3;
      getLog().debug("Bean == 3?");
      assertTrue(bean.getCounter() == 3);
      getLog().debug("passes..");

      // Get handle and serialize
      Handle             beanHandle = bean.getHandle();
      FileOutputStream   out        = new FileOutputStream("abean.ser");
      ObjectOutputStream s          = new ObjectOutputStream(out);

      s.writeObject(beanHandle);
      s.flush();
   }

   /**
    * Method testUnSerialization
    * EJB 1.1 [5.7] Page 45
    * Part II of the test, makes sure the bean can be resurected and used.
    */
   public void testUnSerialization ()
      throws Exception
   {
      //We are deploying for each test, so we need to reserialize first.
      testSerialization();
      getLog().debug("+++ testUnSerialize");

      StatefulSession bean = null;
      getLog().debug("Resurrect bean from .ser file");
      FileInputStream   in         = new FileInputStream("abean.ser");
      ObjectInputStream s          = new ObjectInputStream(in);
      Handle beanHandle = ( Handle ) s.readObject();
      bean = ( StatefulSession ) beanHandle.getEJBObject();

      // Should still equal '3'?
      getLog().debug("Bean reanimated, still equal '3'? bean = "
                         + bean.getCounter());
      assertTrue(bean.getCounter() == 3);
      getLog().debug("Yup, equal to '3'");
      bean.decCounter();
      bean.remove();
   }

   /**
    * Method testProbeBeanContext
    *
    * EJB 1.1 [6.4.1] Page 51
    *
    */
   public void testProbeBeanContext ()
      throws Exception
   {
      getLog().debug("+++ testProbeBeanContext");
      Context ctx  = new InitialContext();
      StatefulSessionHome home = ( StatefulSessionHome ) ctx.lookup("ejbcts/StatefulSessionBean");
      StatefulSession bean = home.create("testProbeBeanContext");		

      getLog().debug("Invoking bean...");

      BeanContextInfo beanCtxInfo = bean.getBeanContextInfo();

      assertTrue(beanCtxInfo != null);

      getLog().debug("remote interface: "
                         + beanCtxInfo.remoteInterface);
      getLog().debug("home interface:   "
                         + beanCtxInfo.homeInterface);

      getLog().debug("Testing rollback only setting...");
      assertTrue(beanCtxInfo.isRollbackOnly.booleanValue());
      getLog().debug(
         "**************************************************************");
   }

   /**
    * Method testLoopback
    * EJB 1.1 [6.5.6]
    * A client call to bean 'A' which calls bean 'B' and bean 'B'
    * in turn calls a method on bean 'A', there should be a
    * RemoteException should be thrown.
    */
   public void testLoopback ()
      throws Exception
   {
      getLog().debug("+++ testLoopback");

      // Create a new session object
      Context ctx  = new InitialContext();
      StatefulSessionHome home = ( StatefulSessionHome ) ctx.lookup("ejbcts/StatefulSessionBean");
      StatefulSession bean = home.create("testLoopback");		
      getLog().debug("Calling loopbackTest( )....");
      try
      {
         bean.loopbackTest();
         fail("Was able to call loopbackTest()");
      }
      catch(Exception e)
      {
         getLog().debug("The loopbackTest( ) failed as expected");
      }
   }

   public void testUserTrx ()
      throws Exception
   {
      getLog().debug("+++ testUsrTrx");

      getLog().debug("Obtain home interface");
      // Create a new session object
      Context ctx  = new InitialContext();
      Object ref = ctx.lookup("ejbcts/StatefulSessionBean");
      StatefulSessionHome home = ( StatefulSessionHome ) PortableRemoteObject.narrow(ref,
         StatefulSessionHome.class);
      StatefulSession bean = home.create("testUserTrx");		

      getLog().debug("Try to instantiate a UserTransaction");
            javax.transaction.UserTransaction uTrx =
              (javax.transaction.UserTransaction)ctx.lookup("UserTransaction");
      bean.remove();
   }


   public static Test suite() throws Exception
   {
      return getDeploySetup(StatefulSessionUnitTestCase.class, "cts.jar");
   }

}
