/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.deployment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfig;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.util.file.JarUtils;
import org.jboss.util.stream.Streams;

/**
 * An abstract {@link SubDeployer}.  
 *
 * <p>Provides registration with {@link MainDeployer} as well as empty
 *    implementations of init, create, start, stop and destroy.
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public abstract class SubDeployerSupport
   extends ServiceMBeanSupport
   implements SubDeployer, SubDeployerMBean
{
   
   /**
    * Holds the native library <em>suffix</em> for this system.
    * Determined by examining the result of System.mapLibraryName("XxX").
    */
   protected static final String nativeSuffix;
   
   /**
    * Holds the native library <em>prefix</em> for this system.
    * Determined by examining the result of System.mapLibraryName("XxX").
    */
   protected static final String nativePrefix;
   
   /** The temporary directory into which deployments are unpacked */
   protected File tempDeployDir;
   /** The temporary directory where native libs are unpacked. */
   private File tempNativeDir;

   static 
   {
      String nativex = System.mapLibraryName("XxX");
      int xPos = nativex.indexOf("XxX"); //hope "XxX' is not part of any native lib goo!
      nativePrefix = nativex.substring(0, xPos);
      nativeSuffix = nativex.substring(xPos + 3);        
   }
   
   /**
    * The <code>createService</code> method is one of the ServiceMBean lifecyle operations.
    * (no jmx tag needed from superinterface)
    * @exception Exception if an error occurs
    */
   protected void createService() throws Exception
   {
      // watch the deploy directory, it is a set so multiple adds 
      // (start/stop) only one entry is present
      // get the temporary directory to use

      ServerConfig config = ServerConfigLocator.locate();
      File basedir = config.getServerTempDir();

      tempNativeDir = new File(basedir, "native");
      tempDeployDir = new File(basedir, "deploy");
   }

   /**
    * Performs SubDeployer registration.
    */
   protected void startService() throws Exception
   {
      // Register with the main deployer
      server.invoke(org.jboss.deployment.MainDeployerMBean.OBJECT_NAME,
                    "addDeployer",
                    new Object[] {this},
                    new String[] {"org.jboss.deployment.SubDeployer"});
   }
   
   /**
    * Performs SubDeployer deregistration.
    */
   protected void stopService() throws Exception
   {
      // Unregister with the main deployer
      server.invoke(org.jboss.deployment.MainDeployer.OBJECT_NAME,
                    "removeDeployer",
                    new Object[] {this},
                    new String[] {"org.jboss.deployment.SubDeployer"});
   }

   /**
    * Sub-classes should override this method to provide
    * custom 'init' logic.
    * 
    * <p>This method calls the processNestedDeployments(di) method.  This behaviour
    *    can overridden by concrete sub-classes.  If further initialization
    *    needs to be done, and you wish to preserve the functionality, be sure
    *    to call super.init(di) at the end of your implementation.
    */
   public void init(DeploymentInfo di) throws DeploymentException 
   {
      processNestedDeployments(di);
   }
   
   /**
    * Sub-classes should override this method to provide
    * custom 'create' logic.
    * 
    * <p>This method is empty, and is provided for convenience
    *    when concrete service classes do not need to perform
    *    anything specific for this state change.
    */
   public void create(DeploymentInfo di) throws DeploymentException {}
   
   /**
    * Sub-classes should override this method to provide
    * custom 'start' logic.
    * 
    * <p>This method is empty, and is provided for convenience
    *    when concrete service classes do not need to perform
    *    anything specific for this state change.
    */
   public void start(DeploymentInfo di) throws DeploymentException {}

   /**
    * Sub-classes should override this method to provide
    * custom 'stop' logic.
    * 
    * <p>This method is empty, and is provided for convenience
    *    when concrete service classes do not need to perform
    *    anything specific for this state change.
    */
   public void stop(DeploymentInfo di) throws DeploymentException {}

   /**
    * Sub-classes should override this method to provide
    * custom 'destroy' logic.
    * 
    * <p>This method is empty, and is provided for convenience
    *    when concrete service classes do not need to perform
    *    anything specific for this state change.
    */
   public void destroy(DeploymentInfo di) throws DeploymentException {}
   
   /**
    * The <code>processNestedDeployments</code> method searches for any nested and
    * deployable elements.  Only Directories and Zipped archives are processed,
    * and those are delegated to the addDeployableFiles and addDeployableJar
    * methods respectively.  This method can be overridden for alternate
    * behaviour.
    */
   protected void processNestedDeployments(DeploymentInfo di) throws DeploymentException 
   {
      if (di.isXML) {
         // no nested archives in an xml file
         return;
      }
      if (di.isDirectory)
      {
         File f = new File(di.url.getFile());
         if (!f.isDirectory()) 
         {
            // something is screwy
            throw new DeploymentException("Deploy file incorrectly reported " +
               "as a directory: " + di.url);
         }
         addDeployableFiles(di, f);
      }
      else
      {
         try
         {
            // Obtain a jar url for the nested jar
            URL nestedURL = JarUtils.extractNestedJar(di.localUrl, this.tempDeployDir);
            JarFile jarFile = new JarFile(nestedURL.getFile());
            addDeployableJar(di, jarFile);
         }
         catch (Exception e)
         {
            log.warn("operation failed; ignoring", e);
             //maybe this is not a jar nor a directory...
            log.info("deploying non-jar/xml file: " + di.url);
            return;
         }
      }
   }

   /**
    * This method returns true if the name is a recognized archive file.  
    * This can be overridden for alternate behaviour.
    * 
    * @param name The "short-name" of the URL.  It will have any trailing '/'
    *        characters removed, and any directory structure has been removed.
    * @param url The full url.
    * 
    * @return true iff the name ends in a known archive extension: jar, sar,
    *         ear, rar, zip, wsr, war, or if the name matches the native
    *         library conventions.
    */
   protected boolean isDeployable(String name, URL url) 
   {
      return name.endsWith(".jar")
          || name.endsWith(".sar")
          || name.endsWith(".ear")
          || name.endsWith(".rar")
          || name.endsWith(".zip")
          || name.endsWith(".wsr")
          || name.endsWith(".war")
          || name.endsWith(".bsh")
          || (name.endsWith(nativeSuffix) && name.startsWith(nativePrefix));
   }
   
   /**
    * This method recursively searches the directory structure for any files
    * that are deployable (@see isDeployable).  If a directory is found to
    * be deployable, then its subfiles and subdirectories are not searched.
    * 
    * @param di the DeploymentInfo
    * @param dir The root directory to start searching.
    */
   protected void addDeployableFiles(DeploymentInfo di, File dir)
      throws DeploymentException
   {
      File[] files = dir.listFiles();
      for (int i = 0; i < files.length; i++)
      {
         File file = files[i];
         String name = file.getName();
         try
         {
            URL url = file.toURL();
            if (isDeployable(name, url))
            {
               deployUrl(di, url, name);
               // we don't want deployable units processed any further
               continue;
            }
         }
         catch (MalformedURLException e)
         {
            log.warn("File name invalid.  Ignoring: " + file, e);
         }
         if (file.isDirectory())
         {
            addDeployableFiles(di, file);
         }
      }
   }
   
   /**
    * This method searches the entire jar file for any deployable files 
    * (@see isDeployable).
    * 
    * @param di the DeploymentInfo
    * @param jarFile the jar file to process.
    */
   protected void addDeployableJar(DeploymentInfo di, JarFile jarFile)
      throws DeploymentException
   {
      String urlPrefix = "jar:"+di.localUrl.toString()+"!/";
      for (Enumeration e = jarFile.entries(); e.hasMoreElements();)
      {
         JarEntry entry = (JarEntry)e.nextElement();
         String name = entry.getName();
         try 
         {
            URL url = new URL(urlPrefix+name);
            if (isDeployable(name, url))
            {
               // Obtain a jar url for the nested jar
               URL nestedURL = JarUtils.extractNestedJar(url, this.tempDeployDir);
               deployUrl(di, nestedURL, name);
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

   protected void deployUrl(DeploymentInfo di, URL url, String name) 
   	throws DeploymentException
   {
      try
      {
         if (name.endsWith(nativeSuffix) && name.startsWith(nativePrefix))
         {
            File destFile = new File(tempNativeDir, name);
            log.info("Loading native library: " + destFile.toString());
            
            File parent = destFile.getParentFile();
            if (!parent.exists())
            {
               parent.mkdirs();
            }

            InputStream in = new BufferedInputStream(url.openStream());
            OutputStream out = new BufferedOutputStream(new FileOutputStream(destFile));
            Streams.copy(in, out);
            out.flush();
            out.close();
            in.close();
            
            System.load(destFile.toString());
         }
         else
         {
            DeploymentInfo sub = new DeploymentInfo(url, di, getServer());
         }
      }
      catch (Exception ex) 
      { 
         log.error("Error in subDeployment with name "+name, ex);
         throw new DeploymentException
            ("Could not deploy sub deployment "+name+" of deployment "+di.url, ex);
      }
   }
}
