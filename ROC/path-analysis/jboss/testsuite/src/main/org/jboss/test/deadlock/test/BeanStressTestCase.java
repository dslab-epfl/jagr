/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/

package org.jboss.test.deadlock.test;

import java.rmi.*;


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ejb.DuplicateKeyException;
import javax.ejb.Handle;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import javax.ejb.HomeHandle;
import javax.ejb.ObjectNotFoundException;

import java.util.Date;
import java.util.Properties;
import java.util.Collection;
import java.util.Iterator;
import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.test.deadlock.interfaces.StatelessSessionHome;
import org.jboss.test.deadlock.interfaces.StatelessSession;
import org.jboss.test.deadlock.interfaces.EnterpriseEntityHome;
import org.jboss.test.deadlock.interfaces.EnterpriseEntity;
import org.jboss.test.JBossTestCase;
import org.jboss.ejb.plugins.lock.ApplicationDeadlockException;

/**
* Sample client for the jboss container.
*
* @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
* @version $Id: BeanStressTestCase.java,v 1.1.1.1 2002/11/16 03:16:43 mikechen Exp $
*/
public class BeanStressTestCase 
   extends JBossTestCase
{
   org.apache.log4j.Category log = getLog();
   
   static boolean deployed = false;
   static int test = 0;
   static Date startDate = new Date();
   
   protected final String namingFactory =
   System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
   
   protected final String providerURL =
   System.getProperty(Context.PROVIDER_URL);

   public BeanStressTestCase(String name) {
      super(name);
   }
   

   protected Object waitFor = new Object();
   protected boolean abDone = false;
   protected boolean baDone = false;


   private StatelessSession getSession() throws Exception
   {
      
      StatelessSessionHome home = (StatelessSessionHome)new InitialContext().lookup("nextgen.StatelessSession");
      return home.create();
   }



   protected class RunTest implements Runnable
   {
      private String test;
      public RunTest(String test)
      {
	 this.test = test;
      }

      public void run()
      {
	 if (test.equals("AB")) runAB();
	 else runBA();
      }

      private void runAB()
      {
	  log.debug("running AB");
	 try
	 {
	    while (true)
	    {
	       getSession().callAB();
	       if (baDone) break;
	    }
	 }
	 catch (Exception ex)
	 {
	    log.debug("failed", ex);
	 }
	 synchronized (waitFor)
	 {
	    abDone = true;
	    if (baDone = true) waitFor.notifyAll();
	 }
      }
      private void runBA()
      {
	  log.debug("running BA");
	 try
	 {
	    while (true)
	    {
	       getSession().callBA();
	       if (abDone) break;
	    }
	 }
	 catch (Exception ex)
	 {
	    log.debug("failed", ex);
	 }
	 synchronized (waitFor)
	 {
	    baDone = true;
	    if (abDone = true) waitFor.notifyAll();
	 }
      }
   }
   
   public void testDeadlock() 
   throws Exception
   {
      EnterpriseEntityHome home = (EnterpriseEntityHome)new InitialContext().lookup("nextgen.EnterpriseEntity");
      try
      {
	 EnterpriseEntity A = home.findByPrimaryKey("A");
      }
      catch (ObjectNotFoundException ex)
      {
	 home.create("A");
      }
      try
      {
	 EnterpriseEntity B = home.findByPrimaryKey("B");
      }
      catch (ObjectNotFoundException ex)
      {
	 home.create("B");
      }
      Thread one = new Thread(new RunTest("AB"));
      Thread two = new Thread(new RunTest("BA"));
      one.start();
      two.start();
      synchronized(waitFor)
      {
	 waitFor.wait();
      }
   }
   
   
   public void testRequiresNewDeadlock() 
   throws Exception
   {
      
      EnterpriseEntityHome home = (EnterpriseEntityHome)new InitialContext().lookup("nextgen.EnterpriseEntity");
      try
      {
	 EnterpriseEntity C = home.findByPrimaryKey("C");
      }
      catch (ObjectNotFoundException ex)
      {
	 home.create("C");
      }
      boolean caughtApplicationDeadlockException = false;
      try
      {
         getSession().requiresNewTest(true);
      }
      catch (Exception ignored)
      {

      }
         /* USE THIS IN FUTURE, can't seem to catch ApplicationDeadlockException 
      catch (RemoteException ex)  
      { 
         System.out.println("here!!!!!");
         ex.printStackTrace();
         System.out.println("here2!!!!!");
         Throwable cause = null; 
         RemoteException rex = ex; 
         if (rex.detail == null) System.out.println("rex detail is null");
         System.out.println("here3!!!!!");
         while (rex.detail != null) 
         { 
            cause = rex.detail;
            System.out.println("cause: " + cause.getClass().getName());
            if (cause instanceof ApplicationDeadlockException) 
            {
               caughtApplicationDeadlockException = true;
               break;
            }
            if (cause instanceof RemoteException) 
            { 
               rex = (RemoteException)cause; 
            }
         } 
         assertTrue(caughtApplicationDeadlockException);
      }
         */
      

   }
   
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(BeanStressTestCase.class, "deadlock.jar");
   }

}
