/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.deployment.scanner;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Collections;
import java.util.Comparator;

import javax.management.ObjectName;
import javax.management.MBeanServer;

import org.jboss.deployment.DeploymentSorter;
import org.jboss.deployment.IncompleteDeploymentException;
import org.jboss.system.server.ServerConfig;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.util.NullArgumentException;

/**
 * A URL-based deployment scanner.  Supports local directory
 * scanning for file-based urls.
 *
 * @jmx:mbean extends="org.jboss.deployment.scanner.DeploymentScannerMBean"
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class URLDeploymentScanner
   extends AbstractDeploymentScanner
   implements DeploymentScanner, URLDeploymentScannerMBean
{
   /** The list of URLs to scan. */
   protected List urlList = Collections.synchronizedList(new ArrayList());
   
   /** A set of scanned urls which have been deployed. */
   protected Set deployedSet = Collections.synchronizedSet(new HashSet());
   
   /** The server's home directory, for relative paths. */
   protected File serverHome;
   
   /** HACK to sort urls from a scaned directory. */
   protected Comparator sorter;
   
   /** Allow a filter for scanned directories */
   protected FileFilter filter;
   private IncompleteDeploymentException lastIncompleteDeploymentException;

   /**
    * @jmx:managed-attribute
    */
   public void setURLList(final List list)
   {
      if (list == null)
         throw new NullArgumentException("list");
      
      boolean debug = log.isDebugEnabled();
      
      // start out with a fresh list
      urlList.clear();
      
      Iterator iter = list.iterator();
      while (iter.hasNext())
      {
         URL url = (URL)iter.next();
         if (url == null)
            throw new NullArgumentException("list element");
         
         addURL(url);
      }
      
      if (debug)
      {
         log.debug("URL list: " + urlList);
      }
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void setURLComparator(String comparatorClassName)
   {
      try
      {
         sorter = (Comparator)Class.forName(comparatorClassName).newInstance();
      }
      catch (Exception e)
      {
         log.warn("Unable to create URLComparator.", e);
      }
   }
   
   /**
    * @jmx:managed-attribute
    */
   public String getURLComparator()
   {
      if (sorter == null)
         return null;
      return sorter.getClass().getName();
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void setFilter(String filterClassName)
   {
      try
      {
         filter = (FileFilter)Class.forName(filterClassName).newInstance();
      }
      catch (Exception e)
      {
         log.warn("Unable to create URLComparator.", e);
      }
   }
   
   /**
    * @jmx:managed-attribute
    */
   public String getFilter()
   {
      if (filter == null)
         return null;
      return filter.getClass().getName();
   }
   
   /**
    * @jmx:managed-attribute
    */
   public List getURLList()
   {
      // too bad, List isn't a cloneable
      return new ArrayList(urlList);
   }
   
   /**
    * @jmx:managed-operation
    */
   public void addURL(final URL url)
   {
      if (url == null)
         throw new NullArgumentException("url");
      
      urlList.add(url);
      if (log.isDebugEnabled())
      {
         log.debug("Added url: " + url);
      }
   }
   
   /**
    * @jmx:managed-operation
    */
   public void removeURL(final URL url)
   {
      if (url == null)
         throw new NullArgumentException("url");
      
      boolean success = urlList.remove(url);
      if (success && log.isDebugEnabled())
      {
         log.debug("Removed url: " + url);
      }
   }
   
   /**
    * @jmx:managed-operation
    */
   public boolean hasURL(final URL url)
   {
      if (url == null)
         throw new NullArgumentException("url");
      
      return urlList.contains(url);
   }
   
   
   /////////////////////////////////////////////////////////////////////////
   //                  Management/Configuration Helpers                   //
   /////////////////////////////////////////////////////////////////////////
   
   /**
    * @jmx:managed-attribute
    */
   public void setURLs(final String listspec) throws MalformedURLException
   {
      if (listspec == null)
         throw new NullArgumentException("listspec");
      
      boolean debug = log.isDebugEnabled();
      
      List list = new LinkedList();
      
      StringTokenizer stok = new StringTokenizer(listspec, ",");
      while (stok.hasMoreTokens())
      {
         String urlspec = stok.nextToken().trim();
         
         if (debug)
         {
            log.debug("Adding URL from spec: " + urlspec);
         }
         
         URL url = makeURL(urlspec);
         if (debug)
         {
            log.debug("URL: " + url);
         }
         list.add(url);
      }
      
      setURLList(list);
   }
   
   /**
    * A helper to make a URL from a full url, or a filespec.
    */
   protected URL makeURL(final String urlspec) throws MalformedURLException
   {
      boolean trace = log.isTraceEnabled();
      
      URL url;
      
      try
      {
         url = new URL(urlspec);
         if (trace) log.trace("using raw url: " + url);
      }
      catch (MalformedURLException e)
      {
         File file = new File(urlspec);
         if (trace) log.trace("converted to file: " + file);
         
         if (!file.isAbsolute())
         {
            file = new File(serverHome, urlspec);
            if (trace) log.trace("made absolute: " + file);
         }

         try
         {
            file = file.getCanonicalFile();
            if (trace) log.trace("made canonical: " + file);
         }
         catch (IOException x)
         {
            throw new MalformedURLException("Invalid urlspec: " + urlspec);
         }
         
         url = file.toURL();
      }

      return url;
   }

   /**
    * @jmx:managed-operation
    */
   public void addURL(final String urlspec) throws MalformedURLException
   {
      addURL(makeURL(urlspec));
   }
   
   /**
    * @jmx:managed-operation
    */
   public void removeURL(final String urlspec) throws MalformedURLException
   {
      removeURL(makeURL(urlspec));
   }
   
   /**
    * @jmx:managed-operation
    */
   public boolean hasURL(final String urlspec) throws MalformedURLException
   {
      return hasURL(makeURL(urlspec));
   }
   
   
   /////////////////////////////////////////////////////////////////////////
   //                           DeploymentScanner                         //
   /////////////////////////////////////////////////////////////////////////
   
   /**
    * A container and help class for a deployed URL.
    * should be static at this point, with the explicit scanner ref, but I'm (David) lazy.
    */
   protected class DeployedURL
   {
      public URL url;
      public URL watchUrl;
      public long deployedLastModified;
      
      public DeployedURL(final URL url)
      {
         this.url = url;
      }
      
      public void deployed()
      {
         deployedLastModified = getLastModified();
      }
      public boolean isFile()
      {
         return url.getProtocol().equals("file");
      }
      
      public File getFile()
      {
         return new File(url.getFile());
      }
      
      public boolean isRemoved()
      {
         if (isFile())
         {
            File file = getFile();
            return !file.exists();
         }
         return false;
      }
      
      public long getLastModified()
      {
         
         if (watchUrl == null)
         {
            try
            {
               Object o = getServer().invoke(getDeployer(), "getWatchUrl",
               new Object[] { url },
               new String[] { URL.class.getName() });
               watchUrl = o == null ? url : (URL)o;
               getLog().debug("Watch URL for: " + url + " -> " + watchUrl);
            }
            catch (Exception e)
            {
               watchUrl = url;
               getLog().debug("Unable to obtain watchUrl from deployer.  " +
                  "Use url: " + url, e);
            }
         }
         try
         {
            URLConnection connection;
            if (watchUrl != null)
            {
               connection = watchUrl.openConnection();
            }
            else
            {
               connection = url.openConnection();
            }
            // no need to do special checks for files...
            // org.jboss.net.protocol.file.FileURLConnection correctly
            // implements the getLastModified method.
            long lastModified = connection.getLastModified();
            return lastModified;
         }
         catch (java.io.IOException e)
         {
            getLog().warn("Failed to check modfication of deployed url: " + url, e);
         }
         
         return -1;
      }
      
      public boolean isModified()
      {
         return deployedLastModified != getLastModified();
      }
      
      public int hashCode()
      {
         return url.hashCode();
      }
      
      public boolean equals(final Object other)
      {
         if (other instanceof DeployedURL)
         {
            return ((DeployedURL)other).url.equals(this.url);
         }
         return false;
      }
      
      public String toString()
      {
         return super.toString() +
            "{ url=" + url +
            ", deployedLastModified=" + deployedLastModified +
            " }";
      }
   }

   /**
    * A helper to deploy the given URL with the deployer.
    */
   protected void deploy(final DeployedURL du)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Deploying: " + du);
      }
      try
      {
         deployer.deploy(du.url);
      }
      catch (IncompleteDeploymentException e)
      {
         lastIncompleteDeploymentException = e;
      }
      catch (Exception e)
      {
         log.error("Failed to deploy: " + du, e);
      } // end of try-catch

      du.deployed();

      if (!deployedSet.contains(du))
      {
         deployedSet.add(du);
      }
   }

   /**
    * A helper to undeploy the given URL from the deployer.
    */
   protected void undeploy(final DeployedURL du)
   {
      try
      {
         if (log.isTraceEnabled())
         {
            log.trace("Undeploying: " + du);
         }
         deployer.undeploy(du.url);
         deployedSet.remove(du);
      }
      catch (Exception e)
      {
         log.error("Failed to undeploy: " + du, e);
      }
   }
   
   /**
    * Checks if the url is in the deployed set.
    */
   protected boolean isDeployed(final URL url)
   {
      DeployedURL du = new DeployedURL(url);
      return deployedSet.contains(du);
   }
   
   public synchronized void scan() throws Exception
   {
      lastIncompleteDeploymentException = null;
      if (urlList == null)
         throw new IllegalStateException("not initialized");
  
      boolean trace = log.isTraceEnabled();

      // Scan for new deployements      
      if (trace)
      {
         log.trace("Scanning for new deployments");
      }

      synchronized (urlList)
      {
         URL[] urls = (URL[]) urlList.toArray( new URL[]{} );
         for( int i = 0; i < urls.length; i++ )
         {
            if (!isDeployed(urls[i]))
            {
               if (urls[i].getProtocol().equals("file"))
               {
                  // it is a file url which gets special handling
                  scanDirectory(urls[i]);
               }
               else
               {
                  // just deploy, not a file url and not yet deployed
                  deploy(new DeployedURL(urls[i]));
               }
            }
         }
      }

      // Scan for removed or changed deployments
      if (trace)
      {
         log.trace("Scanning existing deployments for removal or modification");
      }
      
      // Avoid ConcurrentModificationExceptions
      
      List removed = new LinkedList();
      List modified = new LinkedList();
      
      for (Iterator iter=deployedSet.iterator(); iter.hasNext();)
      {
         DeployedURL du = (DeployedURL)iter.next();
         
         if (trace)
         {
            log.trace("Checking deployment: " + du);
         }
         
         // check if it was removed
         if (du.isRemoved())
         {
            removed.add(du);
         }
         
         // check if was modified
         else if (du.isModified())
         {
            modified.add(du);
         }
      }
      
      for (Iterator iter=removed.iterator(); iter.hasNext();)
      {
         DeployedURL du = (DeployedURL)iter.next();
         undeploy(du);
      }
      
      for (Iterator iter=modified.iterator(); iter.hasNext();)
      {
         DeployedURL du = (DeployedURL)iter.next();
         undeploy(du);
         deploy(du);
      }

      // Validate that there are still incomplete deployments
      if( lastIncompleteDeploymentException != null )
      {
         try
         {
            Object[] args = {};
            String[] sig = {};
            Object o = getServer().invoke(getDeployer(),
               "checkIncompleteDeployments", args, sig);
         }
         catch (Exception e)
         {
            log.error(e);
         }
      }
   }

   /**
    * @todo Add configurable filter support.
    * @todo Add configurable nesting which recurses back to scanDirectory.
    */
   protected void scanDirectory(URL url) throws Exception
   {
      boolean trace = log.isTraceEnabled();
      
      if (trace)
      {
         log.trace("Scanning directory: " + url);
      }
      
      if (isDeployed(url))
      {
         // short-circuit on deployed urls, for nesting support
         return;
      }
      
      File file = new File(url.getFile());
      
      // make sure it exists
      if (!file.exists())
      {
         if (trace)
         {
            log.trace("Skipping non-existant file: " + file);
         }
         return;
      }
      
      // if it is not a directory, and it is not deployed then add it
      if (!file.isDirectory())
      {
         // just a plain file which isn't deployed
         deploy(new DeployedURL(url));
         return;
      }
      // else it is a directory, scan it
      
      File[] files = filter == null ? file.listFiles() : file.listFiles(filter);
      if (files == null)
      {
         throw new Exception("Null files returned from directory listing");
      }
      
      // list of urls to deploy
      List list = new LinkedList();
      
      for (int i = 0; i < files.length; i++)
      {
         if (trace)
         {
            log.trace("Checking deployment file: " + files[i]);
         }
         
         // It is a new file
         url = files[i].toURL();
         if (!isDeployed(url))
         {
            list.add(url);
         }
      }
      
      //
      // HACK, sort the elements so dependencies have a better chance of working
      //
      if (sorter != null)
         Collections.sort(list, sorter);
      
      // deploy each url
      Iterator iter = list.iterator();
      while (iter.hasNext())
      {
         url = (URL)iter.next();
         deploy(new DeployedURL(url));
      }
   }
   
   
   /////////////////////////////////////////////////////////////////////////
   //                     Service/ServiceMBeanSupport                     //
   /////////////////////////////////////////////////////////////////////////
   
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      // get server's home for relative paths, need this for setting
      // attribute final values, so we need todo it here
      serverHome = ServerConfigLocator.locate().getServerHomeDir();
      
      return super.preRegister(server, name);
   }
}
