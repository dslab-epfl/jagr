/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.management.ObjectName;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployerSupport;
import org.jboss.deployment.DeploymentException;

import org.jboss.metadata.XmlFileLoader;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.util.jmx.MBeanProxy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Service that deploys ".rar" files containing resource adapters. Deploying
 * the RAR file is the first step in making the resource adapter available to
 * application components; once it is deployed, one or more connection
 * factories must be configured and bound into JNDI, a task performed by the
 * <code>ConnectionFactoryLoader</code> service.
 *
 * @author     Toby Allsopp (toby.allsopp@peace.com)
 * @author     <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version    $Revision: 1.1.1.1 $
 * @see        org.jboss.resource.ConnectionFactoryLoader <p>
 *
 * <b>Revisions:</b> <p>
 *
 * <b>20010725 Toby Allsopp (patch from David Jencks)</b>
 * <ul>
 *   <li> Implemented <code>getMetaData</code> so that connection factories
 *        can be loaded after RAR deployment</li>
 * </ul>
 *
 * <b>20011219 Marc Fleury</b>
 * <ul>
 *   <li> Make the deployer call create and start on the service it deploys</li>
 * </ul>
 *
 * <b>20011227 Marc Fleury</b>
 * <ul>
 *   <li> Unification of deployers</li>
 * </ul>
 */
public class RARDeployer
   extends SubDeployerSupport
   implements RARDeployerMBean
{
   private static int nextNum = 0;
 
   /** A proxy to the ServiceController. */
   private ServiceControllerMBean serviceController;

   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   /**
    *  The next sequence number to be used in notifications about (un)deployment
    */
   private int nextMessageNum = 0;
   
   /**
    *  Gets the DeployableFilter attribute of the RARDeployer object
    *
    * @return    The DeployableFilter value
    */
   public boolean accepts(DeploymentInfo sdi) 
   {
      String urlStr = sdi.url.toString();
      return urlStr.endsWith("rar") || urlStr.endsWith("rar/");
   }
   
   /**
    * Once registration has finished, create a proxy to the ServiceController
    * for later use.
    */
   public void postRegister(Boolean done) {
      super.postRegister(done);

      serviceController = (ServiceControllerMBean)
	 MBeanProxy.create(ServiceControllerMBean.class,
			   ServiceControllerMBean.OBJECT_NAME,
			   server);
   }
   
   // RARDeployerMBean implementation -------------------------------
   
   /**
    *  Gets the Name attribute of the RARDeployer object
    *
    * @return    The Name value
    */
   public String getName()
   {
      return "RARDeployer";
   }
   
   public void init(DeploymentInfo rdi) 
      throws DeploymentException
   {
      try 
      {
         URL raUrl = rdi.localCl.findResource("META-INF/ra.xml");
         
         Document dd = XmlFileLoader.getDocument(raUrl);
         
         Element root = dd.getDocumentElement();
         
         RARMetaData metadata = new RARMetaData();
         metadata.importXml(root);
         
         metadata.setClassLoader(rdi.ucl);
         rdi.metaData = metadata;
         
         // resolve the watch
         if (rdi.url.getProtocol().startsWith("http"))
         {
            // We watch the top only, no directory support
            rdi.watch = rdi.url;
         }
         
         else if(rdi.url.getProtocol().startsWith("file"))
         {
            
            File file = new File (rdi.url.getFile());
            
            // If not directory we watch the package
            if (!file.isDirectory()) rdi.watch = rdi.url;
               
            // If directory we watch the xml files
            else rdi.watch = new URL(rdi.url, "META-INF/application.xml"); 
         }  
      }
      catch (Exception e) {
	 throw new DeploymentException("problem with init in RARDeployer ", e);
      }
   
      // invoke super-class initialization
      processNestedDeployments(rdi);
   }
   
   /**  
    * The <code>deploy</code> method deploys a rar at the given url.
    *
    * @param url The <code>URL</code> location of the rar to deploy.
    * @return an <code>Object</code> to identify this deployment.
    * @exception IOException if an error occurs
    * @exception DeploymentException if an error occurs
    */
   public void create(DeploymentInfo rdi)
      throws DeploymentException
   {
      if (log.isDebugEnabled()) {
	 log.debug("Attempting to deploy RAR at '" + rdi.url + "'");
      }
      
      try {
         RARMetaData metaData = (RARMetaData) rdi.metaData;
         
         //set up the RARDeployment mbean for dependency management.
         rdi.deployedObject =
	    new ObjectName("jboss.jca:service=RARDeployment,name=" + metaData.getDisplayName());
         server.createMBean("org.jboss.resource.RARDeployment",
                            rdi.deployedObject,
                            new Object[] {metaData},
                            new String[] {"org.jboss.resource.RARMetaData"});
         
         serviceController.create(rdi.deployedObject);
         
         // Create JSR-77 EJB-Module
         // If Parent is not set then this is a standalone Resource Adapter module
         ObjectName lModule = 
           org.jboss.management.j2ee.ResourceAdapterModule.create(
              server,
              (rdi.parent == null)? rdi.shortName:rdi.parent.shortName,
              rdi.shortName,
              rdi.localUrl, 
              rdi.deployedObject
         );
         
         ObjectName lResourceAdaptor = 
            org.jboss.management.j2ee.ResourceAdapter.create(
               server,
               lModule + "",
               metaData.getDisplayName()
         );
      } 
      catch (Exception e) 
      {
         log.error("Problem deploying RARDeployment MBean", e);
         throw new DeploymentException("Problem making RARDeployment MBean", e);  
      }
   }

   public void start(DeploymentInfo rdi) throws DeploymentException
   {
      try 
      {
	 serviceController.start(rdi.deployedObject);
      } 
      catch (Exception e) 
      {
         log.error("Problem deploying RARDeployment MBean", e);
         throw new DeploymentException("Problem making RARDeployment MBean", e);  
      }
   }
   
   public void stop(DeploymentInfo rdi)
      throws DeploymentException
   {
      if (log.isDebugEnabled()) {
	 log.debug("Undeploying RAR at '" + rdi.url + "'");
      }
      
      
      try 
      {
         log.info("About to undeploy RARDeploymentMBean, objectname: " + rdi.deployedObject);
         
	 serviceController.stop(rdi.deployedObject);
      } 
      catch (Exception e) {
         log.error("Problem undeploying RARDeployment MBean", e);
         throw new DeploymentException("Problem undeploying RARDeployment MBean", e);  
      }
   }

   public void destroy(DeploymentInfo rdi)
      throws DeploymentException
   {
      try 
      {
	 serviceController.destroy(rdi.deployedObject);
	 serviceController.remove(rdi.deployedObject);
      } 
      catch (Exception e) {
         log.error("Problem undeploying RARDeployment MBean", e);
         throw new DeploymentException("Problem undeploying RARDeployment MBean", e);  
      }
      
      ((RARMetaData) rdi.metaData).setClassLoader(null);
      
      // Destroy the JSR77 Objects
      org.jboss.management.j2ee.ResourceAdapter.destroy(
         server,
         ((RARMetaData)rdi.metaData).getDisplayName()
      );

      org.jboss.management.j2ee.ResourceAdapterModule.destroy(
         server,
         rdi.shortName
      );
   }
}
