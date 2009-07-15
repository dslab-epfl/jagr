/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management.loading;

import java.util.Vector;
import java.io.Serializable;
import java.lang.reflect.Method;

import org.jboss.mx.loading.LoaderRepository;
import org.jboss.mx.loading.BasicLoaderRepository;
import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.server.MBeanServerImpl;

/**
 *
 * @see org.jboss.mx.loading.LoaderRepository
 * @see org.jboss.mx.loading.BasicLoaderRepository
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $  
 */
public class DefaultLoaderRepository
     implements Serializable
{

   // Attributes ----------------------------------------------------
   protected static Vector loaders = null;
   private static LoaderRepository repository = null;

   // Constructors --------------------------------------------------
   public DefaultLoaderRepository()
   {
   }

   // Static --------------------------------------------------------
   static 
   {
      repository = LoaderRepository.getDefaultLoaderRepository();
      loaders = repository.getLoaders();
   }
   
   public static Class loadClass(String className) throws ClassNotFoundException
   {
      return repository.loadClass(className);
   }

   public static Class loadClassWithout(ClassLoader loader, String className) throws ClassNotFoundException
   {
      return repository.loadClassWithout(loader, className);
   }

}


