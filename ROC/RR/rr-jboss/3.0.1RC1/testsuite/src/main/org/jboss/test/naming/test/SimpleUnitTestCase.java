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
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.security.auth.login.LoginContext;

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.AppCallbackHandler;

/**
 * simple unit tests for the jndi implementation.
 */
public class SimpleUnitTestCase extends JBossTestCase
{
   /**
    * Constructor for the SimpleUnitTestCase object
    *
    * @param name  Test name
    */
   public SimpleUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Tests that the second time you create a subcontext you get an exception.
    *
    * @exception Exception  Description of Exception
    */
   public void testCreateSubcontext() throws Exception
   {
      getLog().debug("+++ testCreateSubcontext");
      InitialContext ctx = getInitialContext();
      ctx.createSubcontext("foo");
      try
      {
         ctx.createSubcontext("foo");
         fail("Second createSubcontext(foo) did NOT fail");
      }
      catch (NameAlreadyBoundException e)
      {
         getLog().debug("Second createSubcontext(foo) failed as expected");
      }
      ctx.createSubcontext("foo/bar");
      ctx.unbind("foo/bar");
      ctx.unbind("foo");
   }

   public void testHaInvoker() throws Exception
   {
      getLog().debug("+++ testHaInvoker");
      Properties env = new Properties();
      env.setProperty(Context.PROVIDER_URL, "http://localhost:1100/");
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
   }

   public void testHttpInvoker() throws Exception
   {
      getLog().debug("+++ testHttpInvoker");
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.HttpNamingContextFactory");
      env.setProperty(Context.PROVIDER_URL, "http://localhost:8080/invoker/JNDIFactory");
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
   }

}
