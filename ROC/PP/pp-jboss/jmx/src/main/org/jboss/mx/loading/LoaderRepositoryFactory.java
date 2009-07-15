package org.jboss.mx.loading;

import javax.management.ObjectName;
import javax.management.ObjectInstance;
import javax.management.MBeanServer;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import org.jboss.mx.server.ServerConstants;


/** A factory for LoaderRepository instances. This is used to obtain repository
 * instances for scoped class loading.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.1.1.1 $
 */
public class LoaderRepositoryFactory
{
   
   public static ObjectName DEFAULT_LOADER_REPOSITORY;
   
   private LoaderRepositoryFactory()
   {
      
   }

   public static synchronized void ensureLoaderRepository(
      MBeanServer server,
      String repositoryClassName,
      ObjectName repositoryName) throws JMException
   {
      if (DEFAULT_LOADER_REPOSITORY == null)
      {
         DEFAULT_LOADER_REPOSITORY = new ObjectName(ServerConstants.DEFAULT_LOADER_NAME);
      }
      try
      {
         ObjectInstance oi = server.getObjectInstance(repositoryName);
         if ((repositoryClassName != null) && !oi.getClassName().equals(repositoryClassName))
         {
            throw new JMException("Inconsistent LoaderRepository class specification in repository: " + repositoryName);
         } // end of if ()
      }
      catch (InstanceNotFoundException e)
      {
         //we are the first, make the repository.
         if( repositoryClassName == null )
            repositoryClassName = ServerConstants.DEFAULT_SCOPED_REPOSITORY_CLASS;
         try
         {
            // Create the repository loader
            Object[] args =
            {server, DEFAULT_LOADER_REPOSITORY};
            String[] sig =
            {"javax.management.MBeanServer", "javax.management.ObjectName"};
            //log.debug("Creating ear loader repository:"+repositoryName);
            server.createMBean(repositoryClassName, repositoryName,
            args, sig);
         }
         catch(Exception e2)
         {
            throw new JMException("Failed to create deployment loader repository:" + e2);
         }
      } // end of try-catch
   }
}// LoaderRepositoryFactory
