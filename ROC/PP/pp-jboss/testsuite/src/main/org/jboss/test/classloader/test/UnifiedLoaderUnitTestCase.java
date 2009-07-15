package org.jboss.test.classloader.test;

import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;

/** Unit tests for the org.jboss.mx.loading.UnifiedLoaderRepository
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class UnifiedLoaderUnitTestCase extends JBossTestCase
{
   public UnifiedLoaderUnitTestCase(String name)
   {
      super(name);
   }

   /** Test the UnifiedLoaderRepository for multi-threaded class loading
    */
   public void testClassLoadingMBean() throws Exception
   {
      try 
      {
         deploy("concurrentloader.sar");
         ObjectName testObjectName = new ObjectName("jboss.test:name=ConcurrentLoader");
         boolean isRegistered = getServer().isRegistered(testObjectName);
         assertTrue("jboss.test:name=ConcurrentLoader isRegistered", isRegistered);
      }
      finally
      {
         undeploy("concurrentloader.sar");
      } // end of try-finally
   }
   /** Test the UnifiedLoaderRepository being accessed by thread with an
    interrupted status
    */
   public void testInterruptedThreads() throws Exception
   {
      try 
      {
         deploy("interrupt.sar");
         ObjectName testObjectName = new ObjectName("jboss.test:name=InterruptTest");
         boolean isRegistered = getServer().isRegistered(testObjectName);
         assertTrue("jboss.test:name=InterruptTest isRegistered", isRegistered);
      }
      finally
      {
         undeploy("interrupt.sar");
      } // end of try-finally
   }
   /**
    * Test the UnifiedLoaderRepository finding local and global resources
    */
   public void testResource()
      throws Exception
   {
      try 
      {
         deploy("loadingresource.ear");
         ObjectName testObjectName = new ObjectName("jboss.test:name=LoadingResource");
         boolean isRegistered = getServer().isRegistered(testObjectName);
         assertTrue("jboss.test:name=LoadingResource isRegistered", isRegistered);
      }
      finally
      {
         undeploy("loadingresource.ear");
      } // end of try-finally
   }

   protected void debug(String message)
   {
      getLog().debug(message);
   }
}
