/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.test;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.apache.log4j.Category;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;

/**
 * This is a TestSetup class for jboss junit test cases that provides the
 * jboss test services. It supplies
 * access to log4j logging, the jboss jmx server, jndi, and a method for
 * deploying ejb packages. You may supply the name of the machine the jboss
 * server is on with the system property jbosstest.server.name (default
 * getInetAddress().getLocalHost().getHostName()) and the directory for
 * deployable packages with the system property jbosstest.deploy.dir (default
 * ../lib).
 *
 * Should be sublassed to derive junit support for specific services integrated
 * into JBoss.
 *
 * @author    <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author    <a href="mailto:christoph.jung@jboss.org">Christoph G. Jung</a>
 * @version   $Revision: 1.1.1.1 $
 */
public class JBossTestSetup
extends TestSetup
{
   
   protected JBossTestServices delegate;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   /**
    * Constructor for the JBossTestCase object
    *
    * @param name  Test case name
    */
   public JBossTestSetup(Test test)
   {
      super(test);
      delegate = createTestServices();
   }
   
   
   // Public --------------------------------------------------------
   
   
   /**
    * The JUnit setup method
    *
    * @exception Exception  Description of Exception
    * /
    * protected void setUp() throws Exception
    * {
    * delegate.setUp();
    * }*/
   
   /**
    * The teardown method for JUnit
    *
    * @exception Exception  Description of Exception
    * /
    * protected void tearDown() throws Exception
    * {
    * delegate.tearDown();
    * }*/
   
   
   
   //protected---------
   
   /** factory for testservices delegate */
   protected JBossTestServices createTestServices()
   {
      return new JBossTestServices(getClass().getName());
   }
   
   /**
    * Gets the InitialContext attribute of the JBossTestCase object
    *
    * @return   The InitialContext value
    */
   protected InitialContext getInitialContext() throws Exception
   {
      return delegate.getInitialContext();
   }
   
   /**
    * Gets the Server attribute of the JBossTestCase object
    *
    * @return   The Server value
    */
   protected RMIAdaptor getServer() throws Exception
   {
      return delegate.getServer();
   }
   
   /**
    * Gets the Log attribute of the JBossTestCase object
    *
    * @return   The Log value
    */
   protected Category getLog()
   {
      return delegate.getLog();
   }
   
   /**
    * Gets the DeployerName attribute of the JBossTestCase object
    *
    * @return                                  The DeployerName value
    * @exception MalformedObjectNameException  Description of Exception
    */
   protected ObjectName getDeployerName() throws MalformedObjectNameException
   {
      return delegate.getDeployerName();
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
      return delegate.getDeployURL(filename);
   }

   protected String getResourceURL(final String path)
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL resURL = loader.getResource(path);
      String resPath = resURL != null ? resURL.toString() : null;
      return resPath;
   }
 
   //is this good for something??????
   /**
    * Gets the Deployed attribute of the JBossTestCase object
    *
    * @param name  Description of Parameter
    * @return      The Deployed value
    */
   protected boolean isDeployed(String name)
   {
      return delegate.isDeployed(name);
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
      return delegate.invoke(name, method, args, sig);
   }
   
   /**
    * Deploy a package with the main deployer. The supplied name is
    * interpreted as a url, or as a filename in jbosstest.deploy.lib or ../lib.
    *
    * @param name           filename/url of package to deploy.
    * @exception Exception  Description of Exception
    */
   protected void deploy(String name) throws Exception
   {
      delegate.deploy(name);
   }
   
   /**
    * Undeploy a package with the main deployer. The supplied name is
    * interpreted as a url, or as a filename in jbosstest.deploy.lib or ../lib.
    *
    * @param name           filename/url of package to undeploy.
    * @exception Exception  Description of Exception
    */
   protected void undeploy(String name) throws Exception
   {
      delegate.undeploy(name);
   }
   
   
   
   protected void flushAuthCache() throws Exception
   {
      delegate.flushAuthCache();
   }
   
   
   protected int getThreadCount()
   {
      return delegate.getThreadCount();
   }
   
   protected int getIterationCount()
   {
      return delegate.getIterationCount();
   }
   
   protected int getBeanCount()
   {
      return delegate.getBeanCount();
   }
   
   
   //private methods--------------
   
}
