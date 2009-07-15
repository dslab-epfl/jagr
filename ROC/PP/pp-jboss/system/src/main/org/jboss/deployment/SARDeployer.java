/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.deployment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.server.ServerConfig;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.util.jmx.MBeanProxy;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * This is the main Service Deployer API.
 *
 * @see org.jboss.system.Service
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:David.Maplesden@orion.co.nz">David Maplesden</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.1.1.1 $
 *
 * @jmx:mbean name="jboss.system:service=ServiceDeployer"
 *            extends="org.jboss.deployment.SubDeployerMBean"
 */
public class SARDeployer
   extends SubDeployerSupport
   implements SubDeployer, SARDeployerMBean
{
   /** A proxy to the ServiceController. */
   private ServiceControllerMBean serviceController;

   /** The system data directory. */
   private File dataDir;

   /** The server configuration base URL. For example,
    file:/<jboss_dist_root>/server/default. Relative service
    descriptor codebase elements are relative to this URL.
    */
   private URL serverHomeURL;

   /** The system library URL. */
   private URL libraryURL;
   
   // Public --------------------------------------------------------
   
   /**
    * Gets the FilenameFilter that the AutoDeployer uses to decide which files
    * will be deployed by the ServiceDeployer. Currently .sar, and files
    * ending in service.xml are accepted.
    *
    * @return   The FileNameFilter for use by the AutoDeployer.
    * @jmx:managed-operation
    */
   public boolean accepts(DeploymentInfo di) 
   {
      String urlStr = di.url.toString();
      return urlStr.endsWith("sar") || urlStr.endsWith("sar/") ||
         urlStr.endsWith("service.xml");
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
      try 
      {
         // resolve the watch
         if (di.url.getProtocol().startsWith("http"))
         {
            // We watch the top only, no directory support
            di.watch = di.url;
         }
         else if(di.url.getProtocol().startsWith("file"))
         {
            File file = new File(di.url.getFile());
            
            // If not directory we watch the package
            if (!file.isDirectory())
            {
               di.watch = di.url;
            }
            // If directory we watch the xml files
            else
            {
               di.watch = new URL(di.url, "META-INF/jboss-service.xml");
            }
         }
         
         // Get the document
         parseDocument(di);     
         
         
         // In case there is a dependent classpath defined parse it
         parseXMLClasspath(di);
         
         //Copy local directory if local-directory element is present
         
         NodeList lds = di.document.getElementsByTagName("local-directory");
         log.debug("about to copy " + lds.getLength() + " local directories");
         
         for (int i = 0; i< lds.getLength(); i++)
         {
            Element ld = (Element)lds.item(i);
            String path = ld.getAttribute("path");
            log.debug("about to copy local directory at " + path);

            // Get the url of the local copy from the classloader.
            log.debug("copying from " + di.localUrl + path + " -> " + dataDir);            
            inflateJar(di.localUrl, dataDir, path);
         }
      }
      catch (DeploymentException e)
      {
         throw e;
      }
      catch (Exception e) 
      {
         throw new DeploymentException(e);
      }

      // invoke super-class initialization
      processNestedDeployments(di);
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
         log.debug("Deploying SAR, create step: url " + di.url);

         // Register the SAR UCL as an mbean so we can use it as the service loader
         ObjectName uclName = di.ucl.getObjectName();
         if( getServer().isRegistered(uclName) == false )
         {
            log.debug("Registering service UCL="+uclName);
            getServer().registerMBean(di.ucl, uclName);
         }

         // Parse the service definitions
         List mbeans = di.mbeans;
         mbeans.clear();
         List descriptorMbeans = serviceController.install(di.document.getDocumentElement(), uclName);
         mbeans.addAll(descriptorMbeans);
         // create the services
         for (Iterator iter = di.mbeans.iterator(); iter.hasNext(); )
         {
            ObjectName service = (ObjectName)iter.next();

            // The service won't be created until explicitly dependent mbeans are created
            serviceController.create(service);
         }
      }
      catch (DeploymentException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         log.debug("create operation failed for package "+ di.url, e);
         destroy(di);
         throw new DeploymentException(e);
      }
   }

   /**
    * The <code>start</code> method starts all the mbeans in this DeploymentInfo..
    *
    * @param di a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    * @jmx:managed-operation
    */
   public void start(DeploymentInfo di) throws DeploymentException
   {
      log.debug("Deploying SAR, start step: url " + di.url);
      try 
      {
         // start the services
         for (Iterator iter = di.mbeans.iterator(); iter.hasNext(); ) 
         {
            ObjectName service = (ObjectName)iter.next();
            
            // The service won't be started until explicitely dependent mbeans are started
            serviceController.start(service);
         }
      }
      catch (DeploymentException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         log.error("start operation failed on package " + di.url, e);
         stop(di);
         destroy(di);
         throw new DeploymentException(e);
      }
   }

   /**
    * @todo integrate the changes from main to allow non file/http URLs
    * @param di
    * @throws Exception
    */
   protected void parseXMLClasspath(DeploymentInfo di)
      throws DeploymentException
   {
      Set classpath = new HashSet();
      
      NodeList children = di.document.getDocumentElement().getChildNodes();
      for (int i = 0; i < children.getLength(); i++)
      {
         if (children.item(i).getNodeType() == Node.ELEMENT_NODE) 
         {
            Element classpathElement = (Element)children.item(i);
            if (classpathElement.getTagName().equals("classpath")) 
            {
               log.debug("Found classpath element: " + classpathElement);
               if (!classpathElement.hasAttribute("codebase") 
                   || !classpathElement.hasAttribute("archives")) 
               {
                  throw new DeploymentException("Invalid classpath element missing codebase or archives: " + classpathElement);
               } // end of if ()

               // Load the codebase
               String codebase = classpathElement.getAttribute("codebase").trim();
               log.debug("Setting up classpath from raw codebase: " + codebase);
            
               if ("".equals(codebase) || ".".equals(codebase))
               {  
                  //does this work with http???
                  // marcf: for http we could just get the substring to the last "/"
                  codebase = new File(di.url.getProtocol() + "://" + di.url.getFile()).getParent();
               }
               // Do we have a relative codebase?
               if (!(codebase.startsWith("http:") || codebase.startsWith("file:"))) 
               {
                  // Resolve relative paths with respect to the serverHomeURL
                  try
                  {
                     codebase = new URL(serverHomeURL, codebase).toString();
                  }
                  catch (MalformedURLException e)
                  {
                     throw new DeploymentException("Failed to build codebase URL", e);
                  }
               }

               // Let's make sure the formatting of the codebase ends with the /
               if (codebase.startsWith("file:") && !codebase.endsWith("/"))
               {
                  codebase += "/";
               }
               else if (codebase.startsWith("http:") && !codebase.endsWith("/"))
               {
                  codebase += "/";
               }

               log.debug("codebase is " + codebase);
            
               //get the archives string
               String archives = classpathElement.getAttribute("archives").trim();
               log.debug("archives are " + archives);

               if (codebase.startsWith("file:") && archives.equals("*"))
               {
                  try
                  {
                     URL fileURL = new URL(codebase);
                     File dir = new File(fileURL.getFile());
                     // The patchDir can only be a File one, local
                     File[] jars = dir.listFiles(
                        new java.io.FileFilter()
                        {
                           /**
                            * filters for jar and zip files in the local directory.
                            *
                            * @param pathname  Path to the candidate file.
                            * @return          True if the file is a jar or zip
                            *                  file.
                            */
                           public boolean accept(File pathname)
                           {
                              String name2 = pathname.getName();
                              return 
                                 (name2.endsWith(".jar") || name2.endsWith(".zip"));
                           }
                        });
                     
                     for (int j = 0; jars != null && j < jars.length; j++)
                     {
                        classpath.add(jars[j].getCanonicalFile().toURL());
                     }
                  }
                  catch (Exception e)
                  {
                     log.error("problem listing files in directory", e);
                     throw new DeploymentException("problem listing files in directory", e);
                  }
               }
               // A directory that is to be added to the classpath
               else if(codebase.startsWith("file:") && archives.equals(""))
               {
                  try
                  {
                     URL fileURL = new URL(codebase);
                     File dir = new File(fileURL.getFile());
                     classpath.add(dir.getCanonicalFile().toURL());
                  }
                  catch(Exception e)
                  {
                     log.error("Failed to add classpath dir", e);
                     throw new DeploymentException("Failed to add classpath dir", e);
                  }
               }
               // We have an archive whatever the codebase go ahead and load the libraries
               else if (!archives.equals(""))
               {
                  // Still no codebase? safeguard
                  if (codebase.equals(""))
                  {
                     codebase = libraryURL.toString();
                  }
                  
                  if (archives.equals("*")) 
                  {
                     // Safeguard
                     if (!codebase.startsWith("file:") && archives.equals("*")) {
                        throw new DeploymentException
                           ("No wildcard permitted in non-file URL deployment you must specify individual jars");
                     }
                     
                     try
                     {
                        URL fileURL = new URL(codebase);
                        File dir = new File(fileURL.getFile());
                        // The patchDir can only be a File one, local
                        File[] jars = dir.listFiles(
                           new java.io.FileFilter()
                           {
                              /**
                               * filters for jar and zip files in the local directory.
                               *
                               * @param pathname  Path to the candidate file.
                               * @return          True if the file is a jar or zip
                               *                  file.
                               */
                              public boolean accept(File pathname)
                              {
                                 String name2 = pathname.getName();
                                 return name2.endsWith(".jar") || name2.endsWith(".zip");
                              }
                           });
                        
                        for (int j = 0; jars != null && j < jars.length; j++)
                        {
                           classpath.add(jars[j].getCanonicalFile().toURL());
                        }
                     }
                     catch (Exception e)
                     {
                        log.error("problem listing files in directory", e);
                        throw new DeploymentException("problem listing files in directory", e);
                     }
                  }
                  
                  else // A real archive listing (as opposed to wildcard)
                  {
                     StringTokenizer jars = new StringTokenizer(archives, ",");
                     //iterate through the packages in archives
                     while (jars.hasMoreTokens())
                     {
                        // The format is simple codebase + jar
                        try
                        {
                           String archive = codebase + jars.nextToken().trim();
                           URL archiveURL = new URL(archive);
                           classpath.add(archiveURL);
                        }    
                        catch (MalformedURLException mfue)
                        {
                           log.error("couldn't resolve package reference: ", mfue);
                        } // end of try-catch
                     }
                  }
               }
               //codebase is empty and archives is empty but we did have a classpath entry
               else
               {
                  throw new DeploymentException
                     ("A classpath entry was declared but no non-file codebase " +
                      "and no jars specified. Please fix jboss-service.xml in your configuration");
               }
               
            } // end of if ()
            
         } // end of if ()
      } //end of for
      
      // Ok, now we've found the list of urls we need... deploy their classes.
      Iterator jars = classpath.iterator();
      while (jars.hasNext())
      {
         URL neededURL = (URL) jars.next();
         di.addLibraryJar(neededURL);
         log.debug("deployed classes for " + neededURL);
      }
   }

   /**
    * Undeploys the package at the url string specified. This will: Undeploy
    * packages depending on this one. Stop, destroy, and unregister all the
    * specified mbeans Unload this package and packages this package deployed
    * via the classpath tag. Keep track of packages depending on this one that
    * we undeployed so that they can be redeployed should this one be
    * redeployed.
    *
    *
    * @param service the <code>DeploymentInfo</code> value to stop.
    * @exception DeploymentException    Thrown if the package could not be
    *                                   undeployed
    * @jmx:managed-operation
    */
   public void stop(DeploymentInfo di)
      //throws DeploymentException
   {
      log.debug("undeploying document " + di.url);
      
      List services = di.mbeans;
      int lastService = services.size();

      // stop services in reverse order.
      for (ListIterator i = services.listIterator(lastService); i.hasPrevious();)
      {
         ObjectName name = (ObjectName)i.previous();
         log.debug("stopping mbean " + name);
         try 
         {
            serviceController.stop(name);
         }
         catch (Exception e)
         {
            log.error("Could not stop mbean: " + name, e);
         } // end of try-catch
      }
   }

   /**
    * Describe <code>destroy</code> method here.
    *
    * @param service a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    * @jmx:managed-operation
    */
   public void destroy(DeploymentInfo di)
      //throws DeploymentException
   {
      List services = di.mbeans;
      int lastService = services.size();


      for (ListIterator i = services.listIterator(lastService); i.hasPrevious();)
      {
         ObjectName name = (ObjectName)i.previous();
         log.debug("destroying mbean " + name);
         try 
         {
            serviceController.destroy(name);
         }
         catch (Exception e)
         {
            log.error("Could not destroy mbean: " + name, e);
         } // end of try-catch
         
      }
      
      for (ListIterator i = services.listIterator(lastService); i.hasPrevious();)
      {
         ObjectName name = (ObjectName)i.previous();
         log.debug("removing mbean " + name);
         try 
         {
            serviceController.remove(name);
         }
         catch (Exception e)
         {
            log.error("Could not remove mbean: " + name, e);
         } // end of try-catch
      }

      // Unregister the SAR UCL
      try
      {
         ObjectName uclName = di.ucl.getObjectName();
         if( getServer().isRegistered(uclName) == true )
         {
            log.debug("Unregistering service UCL="+uclName);
            getServer().unregisterMBean(uclName);
         }
      }
      catch(Exception ignore)
      {
      }
   }

   /**
    * The startService method gets the mbeanProxies for MainDeployer
    * and ServiceController, used elsewhere.
    *
    * @exception Exception if an error occurs
    */
   protected void startService() throws Exception
   {
      super.startService();

      // get the controller proxy
      serviceController = (ServiceControllerMBean) MBeanProxy.create(ServiceControllerMBean.class,
         ServiceControllerMBean.OBJECT_NAME,
         server);
      
      ServerConfig config = ServerConfigLocator.locate();

      // get the data directory, server home url & library url
      dataDir = config.getServerDataDir();
      serverHomeURL = config.getServerHomeURL();
      libraryURL = config.getServerLibraryURL();
   }

   /**
    * Supply our default object name
    *
    * @param server    Our mbean server.
    * @param name      Our proposed object name.
    * @return          Our actual object name
    * 
    * @throws Exception    Thrown if we are supplied an invalid name.
    */
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      super.preRegister(server, name);
      log.debug("ServiceDeployer preregistered with mbean server");
      return name == null ? OBJECT_NAME : name;
   }

   /** Parse the META-INF/jboss-service.xml descriptor
    */
   protected void parseDocument(DeploymentInfo di)
      throws DeploymentException
   {
      InputStream stream=null;
      try
      {
         DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         URL docURL = di.localUrl;
         // Load jboss-service.xml from the jar or directory
         if( di.isXML == false )
            docURL = di.localCl.findResource("META-INF/jboss-service.xml");
         // Validate that the descriptor was found
         if( docURL == null )
            throw new DeploymentException("Failed to find META-INF/jboss-service.xml");

         stream = docURL.openStream();
         InputSource is = new InputSource(stream);
         is.setSystemId(docURL.toString());
         di.document = parser.parse(is);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Exception getting document", e);
      }
      finally
      {
         // close the stream to get around "Too many open files"-errors
         try
         {
            stream.close();
         }
         catch (IOException e)
         {
            // ignore
         }
      }
   }

   // Private --------------------------------------------------------
   
   /**
    * The <code>inflateJar</code> copies the jar entries
    * from the jar url jarUrl to the directory destDir.
    * It can be used on the whole jar, a directory, or
    * a specific file in the jar.
    *
    * @param jarUrl    the <code>URL</code> if the directory or entry to copy.
    * @param destDir   the <code>File</code> value of the directory in which to
    *                  place the inflated copies.
    *
    * @exception DeploymentException if an error occurs
    * @exception IOException if an error occurs
    */
   protected void inflateJar(URL url, File destDir, String path)
      throws DeploymentException, IOException
   {
      /*
      //Why doesn't this work???? Maybe in java 1.4?
      URL jarUrl;
      try
      {
      jarUrl = new URL("jar:" + url.toString() + "!/");
      }
      catch (MalformedURLException mfue)
      {
      throw new DeploymentException("Oops! Couldn't convert URL to a jar URL", mfue);
      }
      
      JarURLConnection jarConnection =
      (JarURLConnection)jarUrl.openConnection();
      JarFile jarFile = jarConnection.getJarFile();
      */

      
      String filename = url.getFile();
      JarFile jarFile = new JarFile(filename);
      try
      {
         for (Enumeration e = jarFile.entries(); e.hasMoreElements(); )
         {
            JarEntry entry = (JarEntry)e.nextElement();
            String name = entry.getName();
            if (path == null || name.startsWith(path))
            {
               File outFile = new File(destDir, name);
               if (!outFile.exists())
               {
                  
                  if (entry.isDirectory())
                  {
                     outFile.mkdirs();
                  }
                  else
                  {
                     InputStream in = jarFile.getInputStream(entry);
                     OutputStream out = new FileOutputStream(outFile);
                     
                     try
                     {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) > 0)
                        {
                           out.write(buffer, 0, read);
                        }
                     }                
                     finally
                     {
                        in.close();out.close();
                     }
                  }
               } // end of if (outFile.exists())
            } // end of if (matches path)
         }
      }
      finally
      {
         jarFile.close();
      }
   }
}
