/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipInputStream;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;

import org.jboss.logging.Logger;
import org.jboss.management.j2ee.J2EEApplication;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.file.JarUtils;
import org.w3c.dom.Element;


import org.jboss.RR.*;


/**
 * Enterprise Archive Deployer.
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.3 $
 */
public class EARDeployer
   extends SubDeployerSupport
   implements EARDeployerMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // <comment author="cgjung">better be protected for subclassing </comment>
   protected String name;
   

//   FMap fmap;

   // Constructors --------------------------------------------------
   
   public EARDeployer()
   {
      super();
//      fmap = new FMap();
   }
   
   public void setDeployerName(final String name)
   {
      this.log = Logger.getLogger(getClass().getName() + "." + name);
      this.name = name;
   }
   
   public String getDeployerName()
   {
      return name.trim();
   }
   
   
   // Public --------------------------------------------------------
   
   public boolean accepts(DeploymentInfo di) 
   {
      String urlStr = di.url.getFile();
      return urlStr.endsWith("ear") || urlStr.endsWith("ear/");
   }
   
   
   public void init(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         if (log.isInfoEnabled())
           log.info("Init J2EE application: " + di.url);
         
         InputStream in = di.localCl.getResourceAsStream("META-INF/application.xml");
         if( in == null )
            throw new DeploymentException("No META-INF/application.xml found");

         XmlFileLoader xfl = new XmlFileLoader();
         Element root = xfl.getDocument(in, "META-INF/application.xml").getDocumentElement();
         J2eeApplicationMetaData metaData = new J2eeApplicationMetaData(root);
         di.metaData = metaData;
         in.close();

         // Check for a jboss-app.xml descriptor
         in = di.localCl.getResourceAsStream("META-INF/jboss-app.xml");
         if( in != null )
         {
            Element jbossApp = xfl.getDocument(in, "META-INF/jboss-app.xml").getDocumentElement();
            in.close();
            //add service archives to metadata
            metaData.importXml(jbossApp, true);
            String repositoryName = MetaData.getOptionalChildContent(jbossApp, "loader-repository");
            if( repositoryName != null )
            {
               // Get the required object name of the repository
               String repositoryClassName = MetaData.getOptionalChildContent(root, "loader-repository-class");
               di.repositoryName = new ObjectName(repositoryName);
               try 
               {
                  ObjectInstance oi = server.getObjectInstance(di.repositoryName);
                  if ((repositoryClassName != null) && !oi.getClassName().equals(repositoryClassName)) 
                  {
                     throw new DeploymentException("Inconsistent LoaderRepository class specification in repository: " + repositoryName);
                  } // end of if ()
               }
               catch (InstanceNotFoundException e)
               {
                  //we are the first, make the repository.
                  if( repositoryClassName == null )
                     repositoryClassName = "org.jboss.mx.loading.HeirarchicalLoaderRepository2";
                  try
                  {
                     // Create the repository loader
                     Object[] args = {this.server, DeploymentInfo.DEFAULT_LOADER_REPOSITORY};
                     String[] sig = {"javax.management.MBeanServer", "javax.management.ObjectName"};
                     log.debug("Creating ear loader repository:"+di.repositoryName);
                     this.server.createMBean(repositoryClassName, di.repositoryName,
                                             args, sig);
                  }
                  catch(Exception e2)
                  {
                     throw new DeploymentException("Failed to create deployment loader repository", e2);
                  }
               } // end of try-catch
            }
         }

         // resolve the watch
         if (di.url.getProtocol().startsWith("http"))
         {
            // We watch the top only, no directory support
            di.watch = di.url;
         }
         
         else if(di.url.getProtocol().startsWith("file"))
         {
            
            File file = new File (di.url.getFile());
            
            // If not directory we watch the package
            if (!file.isDirectory()) di.watch = di.url;
               
            // If directory we watch the xml files
            else di.watch = new URL(di.url, "META-INF/application.xml"); 
         }
         
         // Obtain the sub-deployment list
         File parentDir = null;
         HashMap extractedJars = new HashMap();

         if (di.isDirectory) 
         {
            parentDir = new File(di.localUrl.getFile());
         } 
         else
         {
            /* Extract each entry so that deployment modules can be processed
             and any manifest entries referenced by the ear modules are located
             in the same unpacked directory structure.
            */
            String urlPrefix = "jar:" + di.localUrl + "!/";
            JarFile jarFile = new JarFile(di.localUrl.getFile());
            // For each entry, test if deployable, if so
            // extract it and store the related URL in map
            for (Enumeration e = jarFile.entries(); e.hasMoreElements();)
            {
               JarEntry entry = (JarEntry)e.nextElement();
               String name = entry.getName();
               try 
               {
                  URL url = new URL(urlPrefix + name);
                  if (isDeployable(name, url))
                  {
                     // Obtain a jar url for the nested jar
                    URL nestedURL = JarUtils.extractNestedJar(url, this.tempDeployDir);
                    // and store in it in map
                    extractedJars.put(name, nestedURL);


                  }
               }
               catch (MalformedURLException mue)
               {
                  log.warn("Jar entry invalid. Ignoring: " + name, mue);
               }
               catch (IOException ex)
               {
                  log.warn("Failed to extract nested jar. Ignoring: " + name, ex);
               }
            }
         }

         // Create subdeployments for the ear modules
         for (Iterator iter = metaData.getModules(); iter.hasNext(); )
         {
            J2eeModuleMetaData mod = (J2eeModuleMetaData)iter.next();
            String fileName = mod.getFileName();
            if (fileName != null && (fileName = fileName.trim()).length() > 0)
            {

log.info("FILENAME: " + fileName);

               DeploymentInfo sub = null;
               if (di.isDirectory)
               {
                  File f = new File(parentDir, fileName);
                  sub = new DeploymentInfo(f.toURL(), di, getServer());
               }
               else
               {
                  // The nested jar url was placed into extractedJars above
                  URL nestedURL = (URL) extractedJars.get(fileName);
                  if( nestedURL == null )
                     throw new DeploymentException("Failed to find module file: "+fileName);


                  sub = new DeploymentInfo(nestedURL, di, getServer());


               }
               // Set the context-root on web modules
               if( mod.isWeb() )
                  sub.webContext = mod.getWebContext();
               log.debug("Deployment Info: " + sub + ", isDirectory: " + sub.isDirectory);
            }
         }
      }
      catch (DeploymentException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new DeploymentException("Error in accessing application metadata: ", e);
      }

      // Create the appropriate JSR-77 instance, this has to be done in init
      // EAR create is called after sub-component creates that need this MBean
      ObjectName lApplication = J2EEApplication.create(
         server,
         di.shortName,
         di.localUrl
      );

   }
   
   /**
    * Describe <code>destroy</code> method here.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    */
   public void destroy(DeploymentInfo di) throws DeploymentException
   {
      log.info("Undeploying J2EE application, destroy step: " + di.url);
      // Remove any deployment specific repository
      if( di.repositoryName.equals(DeploymentInfo.DEFAULT_LOADER_REPOSITORY) == false )
      {
         log.debug("Destroying ear loader repository:"+di.repositoryName);
         try
         {
            this.server.unregisterMBean(di.repositoryName);
         }
         catch(Exception e)
         {
            log.warn("Failed to unregister ear loader repository", e);
         }
      }

      // Destroy the appropriate JSR-77 instance
      J2EEApplication.destroy(server, di.shortName);
   }
   
   
   // ServiceMBeanSupport overrides ---------------------------------

   /** 
    * @todo make this.name an attribute rather than appending 
    */
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      return name == null ? new ObjectName(OBJECT_NAME + this.name) : name;
   }

   // Private -------------------------------------------------------
}
