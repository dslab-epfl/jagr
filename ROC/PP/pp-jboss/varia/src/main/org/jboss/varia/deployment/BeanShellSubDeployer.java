/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.varia.deployment;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployerSupport;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.util.jmx.MBeanProxy;
import org.jboss.util.jmx.ObjectNameConverter;

/**
 * <description>
 *
 * @see <related>
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>6 janv. 2003 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

/**
 * @jmx.mbean name="jboss.system:service=BeanShellSubDeployer"
 *            extends="org.jboss.deployment.SubDeployerMBean"
 */
public class BeanShellSubDeployer 
   extends SubDeployerSupport
   implements BeanShellSubDeployerMBean
{
   
   // Constants -----------------------------------------------------
   
   public static final String BEANSHELL_EXTENSION = ".bsh";
   public static final String BASE_SCRIPT_OBJECT_NAME = "jboss.scripts:type=BeanShell";

   // Attributes ----------------------------------------------------
   
   protected ServiceControllerMBean serviceController;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Z implementation ----------------------------------------------
   
   // ServiceMBeanSupport overrides ---------------------------------------------------
   
   /**
    * Get a reference to the ServiceController
    */
   protected void startService() throws Exception
   {
      serviceController = (ServiceControllerMBean)
      MBeanProxy.create(ServiceControllerMBean.class,
         ServiceControllerMBean.OBJECT_NAME, server);

      // register with MainDeployer
      super.startService();
   }

   // SubDeployerSupport overrides ---------------------------------------------------

   protected void processNestedDeployments(DeploymentInfo di) throws DeploymentException 
   {
      // no sub-deployment!      
   }

   /**
    * Returns true if this deployer can deploy the given DeploymentInfo.
    *
    * @return   True if this deployer can deploy the given DeploymentInfo.
    * 
    * @jmx:managed-operation
    */
   public boolean accepts(DeploymentInfo sdi)
   {
      String urlStr = sdi.url.toString();
      return urlStr.toLowerCase().endsWith(BEANSHELL_EXTENSION);
   }

   /**
    * Describe <code>init</code> method here.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    * @jmx:managed-operation
    */
   public void init(DeploymentInfo di)
      throws DeploymentException
   {
      super.init(di);
      di.watch = di.url;
   }

   /**
    * Describe <code>create</code> method here.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    * @jmx:managed-operation
    */
   public void create(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         // install the MBeans in this descriptor
         log.debug("Deploying BeanShell script, create step: url " + di.url);
         
         String lURL = di.url.toString();
         int lIndex = lURL.lastIndexOf( "/" );
         di.shortName = lURL.substring( lIndex >= 0 ? lIndex + 1 : 0 );
                  
         BeanShellScript script = new BeanShellScript (di);
         ObjectName bshScriptName = script.getPreferedObjectName();
         ObjectName[] depends = script.getDependsServices();
         
         if (bshScriptName == null)
         {            
            bshScriptName = ObjectNameConverter.convert(
               BASE_SCRIPT_OBJECT_NAME + ",url=" + di.url);
         }

         di.deployedObject = bshScriptName;
         try
         {
            server.unregisterMBean(bshScriptName);
         } catch(Exception e) { log.info(e);}
         server.registerMBean(script, bshScriptName);

         log.debug( "Deploying: " + di.url );

         // Init application
         if (depends == null)
            serviceController.create(bshScriptName);
         else
            serviceController.create(bshScriptName, Arrays.asList(depends));
      }
      catch (Exception e)
      {
         destroy(di);
         throw new DeploymentException("create operation failed for script "
            + di.url, e);
      }
   }

   public synchronized void start(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         // Start application
         log.debug( "start script, deploymentInfo: " + di +
                    ", short name: " + di.shortName +
                    ", parent short name: " +
                    (di.parent == null ? "no parent" : di.parent.shortName) );

         serviceController.start(di.deployedObject);

         log.debug( "Deployed: " + di.url );
      }
      catch (Exception e)
      {
         throw new DeploymentException( "Could not deploy " + di.url, e );
      }
   }

   public void stop(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         serviceController.stop(di.deployedObject);
      }
      catch (Exception e)
      {
         throw new DeploymentException( "problem stopping ejb module: " +
            di.url, e );
      }
   }

   public void destroy(DeploymentInfo di) 
      throws DeploymentException
   {
      try
      {
         serviceController.destroy( di.deployedObject );
         serviceController.remove( di.deployedObject );
      }
      catch (Exception e)
      {
         throw new DeploymentException( "problem destroying BSH Script: " +
            di.url, e );
      }
   }
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

}
