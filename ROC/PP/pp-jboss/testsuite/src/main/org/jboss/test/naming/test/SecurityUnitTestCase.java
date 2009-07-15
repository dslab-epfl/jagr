/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.naming.test;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.security.auth.login.LoginContext;

import org.jboss.test.JBossTestCase;
import org.jboss.test.util.AppCallbackHandler;

/** Tests of secured access to the JNDI naming service. This testsuite will
 * be run with the standard security resources available via the classpath.
 */
public class SecurityUnitTestCase extends JBossTestCase
{
   /**
    * Constructor for the SecurityUnitTestCase object
    *
    * @param name  Test name
    */
   public SecurityUnitTestCase(String name)
   {
      super(name);
   }


   /** Test access to the JNDI naming service over a restricted http URL
    */
   public void testSecureHttpInvoker() throws Exception
   {
      getLog().debug("+++ testSecureHttpInvoker");
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.HttpNamingContextFactory");

      // Try without a login to ensure the lookup fails
      try
      {
         getLog().debug("Testing without valid login");
         InitialContext ctx1 = new InitialContext(env);
         getLog().debug("Created InitialContext");
         Object obj1 = ctx1.lookup("invokers");
         getLog().debug("lookup(invokers) : "+obj1);
         fail("Should not have been able to lookup(invokers)");
      }
      catch(Exception e)
      {
         getLog().debug("Lookup failed as expected", e);
      }

      /* Try without a login to ensure that a lookup against "readonly" works.
       *First create the readonly context using the standard JNDI factory
      */
      InitialContext ctx2 = new InitialContext();
      Context readonly = ctx2.createSubcontext("readonly");
      readonly.bind("data", "somedata");

      env.setProperty(Context.PROVIDER_URL, "http://localhost:8080/invoker/ReadOnlyJNDIFactory");
      getLog().debug("Creating InitialContext with env="+env);
      ctx2 = new InitialContext(env);
      Object data = ctx2.lookup("readonly/data");
      getLog().debug("lookup(readonly/data) : "+data);
      try
      {
         // Try to bind into the readonly context
         ctx2.bind("readonly/mydata", "otherdata");
         fail("Was able to bind into the readonly context");
      }
      catch(Exception e)
      {
         getLog().debug("Bind failed as expected", e);
      }
      try
      {
         // Try to access a context other then under readonly
         ctx2.lookup("invokers");
         fail("Was able to lookup(invokers)");
      }
      catch(Exception e)
      {
         getLog().debug("lookup(invokers) failed as expected", e);
      }

      // Specify the login conf file location
      String authConf = super.getResourceURL("security/auth.conf");
      System.setProperty("java.security.auth.login.config", authConf);
      AppCallbackHandler handler = new AppCallbackHandler("invoker", "invoker".toCharArray());
      LoginContext lc = new LoginContext("testSecureHttpInvoker", handler);
      lc.login();

      // Test the secured JNDI factory
      env.setProperty(Context.PROVIDER_URL, "http://localhost:8080/invoker/restricted/JNDIFactory");
      getLog().debug("Creating InitialContext with env="+env);
      InitialContext ctx = new InitialContext(env);
      getLog().debug("Created InitialContext");
      Object obj = ctx.lookup("invokers");
      getLog().debug("lookup(invokers) : "+obj);
      Context invokersCtx = (Context) obj;
      NamingEnumeration list = invokersCtx.list("");
      while( list.hasMore() )
      {
         Object entry = list.next();
         getLog().debug(" + "+entry);
      }
      ctx.close();
      lc.logout();


   }

   /** Test an initial context factory that does a JAAS login to validate the
    * credentials passed in
    */
   public void testLoginInitialContext() throws Exception
   {
      getLog().debug("+++ testLoginInitialContext");
      Properties env = new Properties();
      // Try with a login that should succeed
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.LoginInitialContextFactory");
      env.setProperty(Context.PROVIDER_URL, "jnp://localhost:1099/");
      env.setProperty(Context.SECURITY_CREDENTIALS, "theduke");
      env.setProperty(Context.SECURITY_PRINCIPAL, "jduke");
      env.setProperty(Context.SECURITY_PROTOCOL, "testLoginInitialContext");

      // Specify the login conf file location
      String authConf = super.getResourceURL("security/auth.conf");
      System.setProperty("java.security.auth.login.config", authConf);

      getLog().debug("Creating InitialContext with env="+env);
      InitialContext ctx = new InitialContext(env);
      getLog().debug("Created InitialContext");
      Object obj = ctx.lookup("invokers");
      getLog().debug("lookup(invokers) : "+obj);
      Context invokersCtx = (Context) obj;
      NamingEnumeration list = invokersCtx.list("");
      while( list.hasMore() )
      {
         Object entry = list.next();
         getLog().debug(" + "+entry);
      }
      ctx.close();

      // Try with a login that should fail
      env.setProperty(Context.SECURITY_CREDENTIALS, "badpass");
      try
      {
         getLog().debug("Creating InitialContext with env="+env);
         ctx = new InitialContext(env);
         fail("Was able to create InitialContext with badpass");
      }
      catch(NamingException e)
      {
         getLog().debug("InitialContext failed as expected with exception", e);
      }
   }
}
