/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.test.security.test;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.Handle;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.*;

import org.jboss.test.security.interfaces.StatelessSession;
import org.jboss.test.security.interfaces.StatelessSessionHome;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.test.util.AppCallbackHandler;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;

/** Test of the secure remote password(SRP) session key to perform crypto
operations.

 
 @author Scott.Stark@jboss.org
 @version $Revision: 1.1.1.1 $
 */
public class SRPUnitTestCase extends JBossTestCase
{
   static final String JAR = "security-srp.jar";
   static String username = "scott";
   static char[] password = "echoman".toCharArray();

   LoginContext lc;
   boolean loggedIn;

   public SRPUnitTestCase(String name)
   {
      super(name);
   }

   /** Test that the echo method is secured by the SRPCacheLogin module
    */
   public void testEchoArgs() throws Exception
   {
      log.debug("+++ testEchoArgs");
      login("srp-test", username, password);
      Object obj = getInitialContext().lookup("srp.StatelessSession");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found StatelessSessionHome");
      StatelessSession bean = home.create();
      log.debug("Created spec.StatelessSession");
      log.debug("Bean.echo('Hello') -> "+bean.echo("Hello"));
      bean.remove();
      logout();
   }

   /** Login using the given confName login configuration with the provided
    username and password credential.
    */
   private void login(String confName, String username, char[] password)
      throws Exception
   {
      if( loggedIn )
         return;

      lc = null;
      AppCallbackHandler handler = new AppCallbackHandler(username, password);
      log.debug("Creating LoginContext("+confName+")");
      lc = new LoginContext(confName, handler);
      lc.login();
      log.debug("Created LoginContext, subject="+lc.getSubject());
      loggedIn = true;
   }
   private void logout() throws Exception
   {
      if( loggedIn )
      {
         loggedIn = false;
         lc.logout();
      }
   }

   /**
    * Setup the test suite.
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(SRPUnitTestCase.class));

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            deploy(JAR);
            // Establish the JAAS login config
            String authConfPath = super.getResourceURL("security-srp/auth.conf");
            System.setProperty("java.security.auth.login.config", authConfPath);
         }
         protected void tearDown() throws Exception
         {
            undeploy(JAR);
            super.tearDown();
         }
      };
      return wrapper;
   }

}
