/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.test;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.InetAddress;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.naming.InitialContext;

import org.apache.log4j.Category;

/**
 * This is provides services for jboss junit test cases and TestSetups. It supplies
 * access to log4j logging, the jboss jmx server, jndi, and a method for
 * deploying ejb packages. You may supply the name of the machine the jboss
 * server is on with the system property jbosstest.server.name (default
 * getInetAddress().getLocalHost().getHostName()) and the directory for
 * deployable packages with the system property jbosstest.deploy.dir (default
 * ../lib).
 *
 * Should be subclassed to derive junit support for specific services integrated
 * into jboss.
 *
 * @author    <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author    <a href="mailto:christoph.jung@jboss.org">Christoph G. Jung</a>
 * @version   $Revision: 1.1.1.1 $
 */
public class JBossTestServices
{
   
   // Constants -----------------------------------------------------
   //private final static String serviceDeployerName = "jboss.system:service=ServiceDeployer";
   //private final static String j2eeDeployerName = "jboss.j2ee:service=J2eeDeployer";
   
   protected final static String DEPLOYER_NAME = "jboss.system:service=MainDeployer";

   protected final static int DEFAULT_THREADCOUNT = 10;
   protected final static int DEFAULT_ITERATIONCOUNT = 1000;
   protected final static int DEFAULT_BEANCOUNT = 100;
   
   // Attributes ----------------------------------------------------
   protected org.jboss.jmx.adaptor.rmi.RMIAdaptor server;
   protected Category log;
   protected InitialContext initialContext;
   
   protected java.util.HashSet deployed = new java.util.HashSet();
   
   
   // Static --------------------------------------------------------
   // Constructors --------------------------------------------------
   /**
    * Constructor for the JBossTestCase object
    *
    * @param name  Test case name
    */
   public JBossTestServices(String className)
   {
      log = Category.getInstance(className);
      log.debug("JBossTestServices(), className="+className);
      setUp();
   }
   
   // Public --------------------------------------------------------
   
   
   /**
    * The JUnit setup method
    *
    * @exception Exception  Description of Exception
    */
   public void setUp()
   {
      log.debug("JBossTestServices.setUp()");
      log.info("jbosstest.beancount: " + System.getProperty("jbosstest.beancount"));
      log.info("jbosstest.iterationcount: " + System.getProperty("jbosstest.iterationcount"));
      log.info("jbosstest.threadcount: " + System.getProperty("jbosstest.threadcount"));
      log.info("jbosstest.nodeploy: " + System.getProperty("jbosstest.nodeploy"));
   }
   
   /**
    * The teardown method for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void tearDown() throws Exception
   {
      // server = null;
      log.debug("JBossTestServices.tearDown()");
   }
   
   
   //protected---------
   
   /**
    * Gets the InitialContext attribute of the JBossTestCase object
    *
    * @return   The InitialContext value
    */
   InitialContext getInitialContext() throws Exception
   {
      init();
      return initialContext;
   }
   
   /**
    * Gets the Server attribute of the JBossTestCase object
    *
    * @return   The Server value
    */
   org.jboss.jmx.adaptor.rmi.RMIAdaptor getServer () throws Exception
   {
      init();
      return server;
   }
   
   /**
    * Gets the Log attribute of the JBossTestCase object
    *
    * @return   The Log value
    */
   Category getLog()
   {
      return log;
   }
   
   /**
    * Gets the Main Deployer Name attribute of the JBossTestCase object
    *
    * @return                                  The Main DeployerName value
    * @exception MalformedObjectNameException  Description of Exception
    */
   ObjectName getDeployerName() throws MalformedObjectNameException
   {
      return new ObjectName(DEPLOYER_NAME);
   }
   
   
   /**
    * Returns the deployment directory to use. This does it's best to figure out
    * where you are looking. If you supply a complete url, it returns it.
    * Otherwise, it looks for jbosstest.deploy.dir or if missing ../lib. Then it
    * tries to construct a file url or a url.
    *
    * @param filename                   name of the file/url you want
    * @return                           A more or less canonical string for the
    *      url.
    * @exception MalformedURLException  Description of Exception
    */
   protected String getDeployURL(final String filename) throws MalformedURLException
   {
      //First see if it is already a complete url.
      try
      {
         return new URL(filename).toString();
      }
      catch (MalformedURLException mue)
      {
      }
      //OK, lets see if we can figure out what it might be.
      String deployDir = System.getProperty("jbosstest.deploy.dir");
      if (deployDir == null)
      {
         deployDir = "../lib";
      }
      String url = deployDir + "/" + filename;
      //try to canonicalize the strings a bit.
      if (new File(url).exists())
      {
         return new File(url).toURL().toString();
      }
      else
      {
         return new URL(url).toString();
      }
   }
   
   
   //is this good for something??????
   /**
    * Gets the Deployed attribute of the JBossTestCase object
    *
    * @param name  Description of Parameter
    * @return      The Deployed value
    */
   boolean isDeployed(String name)
   {
      return deployed.contains(name);
   }
   
   /**
    * invoke wraps an invoke call to the mbean server in a lot of exception
    * unwrapping.
    *
    * @param name           ObjectName of the mbean to be called
    * @param method         mbean method to be called
    * @param args           Object[] of arguments for the mbean method.
    * @param sig            String[] of types for the mbean methods parameters.
    * @return               Object returned by mbean method invocation.
    * @exception Exception  Description of Exception
    */
   protected Object invoke(ObjectName name, String method, Object[] args, String[] sig) throws Exception
   {
      return invoke (getServer(), name, method, args, sig);
   }
   
   protected Object invoke (org.jboss.jmx.adaptor.rmi.RMIAdaptor server, ObjectName name, String method, Object[] args, String[] sig) throws Exception
   {
      try
      {
         return server.invoke(name, method, args, sig);
      }
      catch (javax.management.MBeanException e)
      {
         log.error("MbeanException", e.getTargetException());
         throw e.getTargetException();
      }
      catch (javax.management.ReflectionException e)
      {
         log.error("ReflectionException", e.getTargetException());
         throw e.getTargetException();
      }
      catch (javax.management.RuntimeOperationsException e)
      {
         log.error("RuntimeOperationsException", e.getTargetException());
         throw e.getTargetException();
      }
      catch (javax.management.RuntimeMBeanException e)
      {
         log.error("RuntimeMbeanException", e.getTargetException());
         throw e.getTargetException();
      }
      catch (javax.management.RuntimeErrorException e)
      {
         log.error("RuntimeErrorException", e.getTargetError());
         throw e.getTargetError();
      }
   }
   

   /**
    * Deploy a package with the main deployer. The supplied name is
    * interpreted as a url, or as a filename in jbosstest.deploy.lib or ../lib.
    *
    * @param name           filename/url of package to deploy.
    * @exception Exception  Description of Exception
    */
   public void deploy(String name) throws Exception
   {
      if( Boolean.getBoolean("jbosstest.nodeploy") == true )
      {
         log.debug("Skipping deployment of: "+name);
         return;
      }

      String deployURL = getDeployURL(name);
      log.debug("Deploying "+name+", url="+deployURL);
      invoke(getDeployerName(),
         "deploy",
         new Object[] {deployURL},
         new String[] {"java.lang.String"});
      setDeployed(deployURL);
   }

   /**
    * Undeploy a package with the main deployer. The supplied name is
    * interpreted as a url, or as a filename in jbosstest.deploy.lib or ../lib.
    *
    * @param name           filename/url of package to undeploy.
    * @exception Exception  Description of Exception
    */
   public void undeploy(String name) throws Exception
   {
      if( Boolean.getBoolean("jbosstest.nodeploy") == true )
         return;
      String deployName = getDeployURL(name);
      invoke(getDeployerName(),
         "undeploy",
         new Object[]
         {deployName},
         new String[]
         {"java.lang.String"});
      setUnDeployed(deployName);
   }

   /** Flush all authentication credentials for the java:/jaas/other security
    domain
    */
   void flushAuthCache() throws Exception
   {
      ObjectName jaasMgr = new ObjectName("jboss.security:service=JaasSecurityManager");
      Object[] params = {"other"};
      String[] signature = {"java.lang.String"};
      invoke(jaasMgr, "flushAuthenticationCache", params, signature);
   }
   
   int getThreadCount()
   {
      return Integer.getInteger("jbosstest.threadcount", DEFAULT_THREADCOUNT).intValue();
   }
   
   int getIterationCount()
   {
      return Integer.getInteger("jbosstest.iterationcount", DEFAULT_ITERATIONCOUNT).intValue();
   }

   int getBeanCount()
   {
      return Integer.getInteger("jbosstest.beancount", DEFAULT_BEANCOUNT).intValue();
   }
   
   
   
   //private methods--------------
   
   protected void setDeployed(String name)
   {
      deployed.add(name);
   }
   
   protected void setUnDeployed(String name)
   {
      deployed.remove(name);
   }
   
   protected void init() throws Exception
   {
      if (initialContext == null)
      {
         initialContext = new InitialContext();
      }
      if (server == null)
      {
         String serverName = System.getProperty("jbosstest.server.name");
         if (serverName == null)
         {
            serverName = InetAddress.getLocalHost().getHostName();
         }
         server = (org.jboss.jmx.adaptor.rmi.RMIAdaptor)initialContext.lookup("jmx:" + serverName + ":rmi");
      }
   }
   
}
