/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.test.security.test;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.test.util.AppCallbackHandler;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;

/** Test of the secure remote password(SRP) service and its usage via JAAS
login modules.
 
 @author Scott.Stark@jboss.org
 @version $Revision: 1.1.1.1 $
 */
public class SRPLoginModuleUnitTestCase extends JBossTestCase
{
   static final String JAR = "security-srp.sar";
   static String username = "scott";
   static char[] password = "echoman".toCharArray();

   LoginContext lc;
   boolean loggedIn;

   public SRPLoginModuleUnitTestCase(String name)
   {
      super(name);
   }

   /** Test a login against the SRP service using the SRPLoginModule
    */
   public void testSRPLogin() throws Exception
   {
      log.debug("+++ testSRPLogin");
      login("srp-test", username, password, null);
      logout();
   }

   /** Test a login against the SRP service using the SRPLoginModule and
    specify the random number used in the client A public key.
    */
   public void testSRPLoginWithExternalA() throws Exception
   {
      log.debug("+++ testSRPLoginWithExternalA");
      byte[] abytes = "abcdefgh".getBytes();
      login("srp-test-ex", username, password, abytes);
      logout();
   }

   /** Test a login against the SRP service using the SRPLoginModule and
    provide an auxillarly challenge to be validated by the server.
    */
   public void testSRPLoginWithAuxChallenge() throws Exception
   {
      log.debug("+++ testSRPLoginWithAuxChallenge");
      // Check for javax/crypto/SealedObject
      try
      {
         Class.forName("javax.crypto.SealedObject");
         log.debug("Found javax/crypto/SealedObject");
         login("srp-test-aux", username, password, null, "token-123");
      }
      catch(ClassNotFoundException e)
      {
         log.debug("Failed to find javax/crypto/SealedObject, skipping test");
         return;
      }
      catch(NoClassDefFoundError e)
      {
         log.debug("Failed to find javax/crypto/SealedObject, skipping test");
         return;
      }
      catch(LoginException e)
      {
         if( e.getMessage().indexOf("SealedObject") < 0 )
            fail("Non-SealedObject error, msg="+e.getMessage());
         log.debug("Failed to find SealedObject, skipping test");         
         return;
      }
      catch(Exception e)
      {
         log.error("Non CNFE exception during testSRPLoginWithAuxChallenge", e);
         fail("Non CNFE exception during testSRPLoginWithAuxChallenge");
      }

      logout();
   }

   /** Test a login against the SRP service using the SRPLoginModule with
    multiple sessions for the same user.
    */
   public void testSRPLoginWithMultipleSessions() throws Exception
   {
      log.debug("+++ testSRPLoginWithMultipleSessions");

      // Session #1
      AppCallbackHandler handler = new AppCallbackHandler(username, password, null);
      log.debug("Creating LoginContext(srp-test-multi) #1");
      LoginContext lc1 = new LoginContext("srp-test-multi", handler);
      lc1.login();
      log.debug("Created LoginContext, subject="+lc1.getSubject());

      // Session #1
      log.debug("Creating LoginContext(srp-test-multi) #2");
      LoginContext lc2 = new LoginContext("srp-test-multi", handler);
      lc2.login();
      log.debug("Created LoginContext, subject="+lc2.getSubject());

      lc1.logout();
      lc2.logout();
   }

   /** Login using the given confName login configuration with the provided
    username and password credential.
    */
   private void login(String confName, String username, char[] password,
      byte[] data) throws Exception
   {
      this.login(confName, username, password, data, null);
   }
   private void login(String confName, String username, char[] password,
      byte[] data, String text) throws Exception
   {
      if( loggedIn )
         return;

      lc = null;
      AppCallbackHandler handler = new AppCallbackHandler(username, password, data, text);
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
      suite.addTest(new TestSuite(SRPLoginModuleUnitTestCase.class));

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
