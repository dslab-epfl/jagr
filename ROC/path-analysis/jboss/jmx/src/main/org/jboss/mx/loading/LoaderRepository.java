/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.loading;

import java.net.URL;
import java.util.Vector;
import java.util.HashMap;

import org.jboss.mx.server.ServerConstants;

/**
 *
 * @see javax.management.loading.DefaultLoaderRepository
 * @see org.jboss.mx.loading.BasicLoaderRepository
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $  
 */
public abstract class LoaderRepository
   implements ServerConstants
{

   // Attributes ----------------------------------------------------
   protected static Vector loaders = new Vector();
   protected static LoaderRepository instance = null;

   /**
    * Native signature to class map
    */
   protected static HashMap nativeClassBySignature;
   
   // Static --------------------------------------------------------
   /**
    * Construct the native class map
    */
   static
   {
     nativeClassBySignature = new HashMap();
     nativeClassBySignature.put(boolean.class.getName(),
                                Boolean.TYPE);
     nativeClassBySignature.put(byte.class.getName(), 
                                Byte.TYPE);
     nativeClassBySignature.put(char.class.getName(), 
                                Character.TYPE);
     nativeClassBySignature.put(double.class.getName(), 
                                Double.TYPE);
     nativeClassBySignature.put(float.class.getName(), 
                                Float.TYPE);
     nativeClassBySignature.put(int.class.getName(), 
                                Integer.TYPE);
     nativeClassBySignature.put(long.class.getName(), 
                                Long.TYPE);
     nativeClassBySignature.put(short.class.getName(), 
                                Short.TYPE);
     nativeClassBySignature.put(void.class.getName(), 
                                Void.TYPE);
   }

   public synchronized static LoaderRepository getDefaultLoaderRepository()
   {
      
      if (instance != null)
         return instance;
         
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      String className = System.getProperty(LOADER_REPOSITORY_CLASS_PROPERTY, DEFAULT_LOADER_REPOSITORY_CLASS);
      System.setProperty(LOADER_REPOSITORY_CLASS_PROPERTY, className);

      try 
      {
         Class repository = cl.loadClass(className);
         instance = (LoaderRepository)repository.newInstance();
         
         return instance;
      }
      catch (ClassNotFoundException e)
      {
         throw new Error("Cannot instantiate default loader repository class. Class " + className + " not found.");
      }
      catch (ClassCastException e) 
      {
         throw new Error("Cannot instantiate default loader repository class. The target class is not an instance of LoaderRepository interface.");
      }
      catch (Exception e) 
      {
         throw new Error("Error creating default loader repository: " + e.toString());      
      }
   }
   
   // Public --------------------------------------------------------   
   public Vector getLoaders()
   {
      return loaders;
   }

   public URL[] getURLs()
   {
      return null;
   }

   public abstract UnifiedClassLoader newClassLoader(final URL url, boolean addToRepository)
      throws Exception;
   public abstract UnifiedClassLoader newClassLoader(final URL url, final URL origURL, boolean addToRepository)
      throws Exception;

   public abstract Class loadClass(String className) throws ClassNotFoundException;
   public abstract Class loadClass(String name, boolean resolve, ClassLoader cl) throws ClassNotFoundException;
   public abstract URL getResource(String name, ClassLoader cl);
   public abstract Class loadClassWithout(ClassLoader loader, String className) throws ClassNotFoundException;
   public abstract void addClassLoader(ClassLoader cl);
   public abstract void removeClassLoader(ClassLoader cl);
}
