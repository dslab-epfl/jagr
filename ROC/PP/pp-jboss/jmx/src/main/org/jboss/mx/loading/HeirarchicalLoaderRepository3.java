/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.loading;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jboss.mx.logging.Logger;
import org.jboss.mx.loading.LoadMgr.PkgClassLoader;
import org.jboss.mx.server.ServerConstants;

/** A simple extension of UnifiedLoaderRepository3 that adds the notion of a
 * parent UnifiedLoaderRepository. Classes and resources are loaded from child
 * first and then the parent.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class HeirarchicalLoaderRepository3 extends UnifiedLoaderRepository3
{
   // Attributes ----------------------------------------------------
   private static final Logger log = Logger.getLogger(HeirarchicalLoaderRepository3.class);
   private static ObjectName DEFAULT_LOADER_NAME;
   static
   {
      try
      {
         DEFAULT_LOADER_NAME = new ObjectName(ServerConstants.DEFAULT_LOADER_NAME);
      }
      catch(Exception e)
      {
         log.error("Failed to initialize default loader name", e);
      }
   }

   /** The repository to which we delegate if requested classes or resources
    are not available from this repository.
    */
   private UnifiedLoaderRepository3 parentRepository;

   public HeirarchicalLoaderRepository3(MBeanServer server)
      throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException
   {
      this(server, DEFAULT_LOADER_NAME);
   }
   public HeirarchicalLoaderRepository3(MBeanServer server, ObjectName parentName)
      throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException
   {
      this.parentRepository = (UnifiedLoaderRepository3) server.getAttribute(parentName,
                    "Instance");
   }

   // Public --------------------------------------------------------

   /** Load a class using the repository class loaders.
    *
    * @param name The name of the class
    * @param resolve If <code>true</code>, the class will be resolved
    * @param scl The asking class loader
    * @return The loaded class
    * @throws ClassNotFoundException If the class could not be found.
    */
   public Class loadClass(String name, boolean resolve, ClassLoader scl)
      throws ClassNotFoundException
   {
      Class foundClass = null;
      
      // Try this repository
      try
      {
         foundClass = super.loadClass(name, resolve, scl);
      }
      catch(ClassNotFoundException e)
      {
         // Next try our parent repository
         if( foundClass == null )
            foundClass = parentRepository.loadClass(name, resolve, scl);
      }
      if( foundClass != null )
         return foundClass;

      /* If we reach here, all of the classloaders currently in the VM don't
         know about the class
      */
      throw new ClassNotFoundException(name);
   }

   /** Find a resource from this repository. This first looks to this
    * repository and then the parent repository.
    * @param name The name of the resource
    * @param scl The asking class loader
    * @return An URL for reading the resource, or <code>null</code> if the
    *          resource could not be found.
    */
   public URL getResource(String name, ClassLoader scl)
   {  
      // Try this repository
      URL resource = super.getResource(name, scl);
      // Next try our parent repository
      if( resource == null )
         resource = parentRepository.getResource(name, scl);
      
      return resource;
   }

   /** Obtain a listing of the URLs for all UnifiedClassLoaders associated with
    *the repository
    */
   public URL[] getURLs()
   {
      URL[] ourURLs = super.getURLs();
      URL[] parentURLs = parentRepository.getURLs();
      int size = ourURLs.length + parentURLs.length;
      URL[] urls = new URL[size];
      System.arraycopy(ourURLs, 0, urls, 0, ourURLs.length);
      System.arraycopy(parentURLs, 0, urls, ourURLs.length, parentURLs.length);
      return urls;
   }

   /** Called by LoadMgr to locate a previously loaded class. This looks
    * first to this repository and then the parent repository.
    *@return the cached class if found, null otherwise
    */
   Class loadClassFromCache(String name)
   {
      // Try this repository
      Class foundClass = super.loadClassFromCache(name);
      // Next try our parent repository
      if( foundClass == null )
         foundClass = parentRepository.loadClassFromCache(name);
      return foundClass;
   }

   /** Called by LoadMgr to obtain all class loaders. This returns a set of
    * PkgClassLoader with the HeirarchicalLoaderRepository3 ordered ahead of
    * the parent repository pkg class loaders
    *@return HashSet<PkgClassLoader>
    */
   public HashSet getPackageClassLoaders(String name)
   {
      HashSet pkgSet = super.getPackageClassLoaders(name);
      HashSet parentPkgSet = parentRepository.getPackageClassLoaders(name);
      // Build a set of PkgClassLoader
      HashSet theSet = new HashSet();
      if( pkgSet != null )
      {
         Iterator iter = pkgSet.iterator();
         while( iter.hasNext() )
         {
            UnifiedClassLoader3 ucl = (UnifiedClassLoader3) iter.next();
            PkgClassLoader pkgUcl = new PkgClassLoader(ucl, 0);
            theSet.add(pkgUcl);
         }
      }

      if( parentPkgSet != null )
      {
         Iterator iter = parentPkgSet.iterator();
         while( iter.hasNext() )
         {
            UnifiedClassLoader3 ucl = (UnifiedClassLoader3) iter.next();
            PkgClassLoader pkgUcl = new PkgClassLoader(ucl, 1);
            theSet.add(pkgUcl);
         }
      }

      return theSet;
   }

}
