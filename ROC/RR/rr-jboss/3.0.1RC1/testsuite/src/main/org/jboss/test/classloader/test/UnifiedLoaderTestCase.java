package org.jboss.test.classloader.test;

import java.net.URL;
import java.lang.reflect.Constructor;
import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;

/** Unit tests for the org.jboss.mx.loading.UnifiedLoaderRepository
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class UnifiedLoaderTestCase extends JBossTestCase
{
   private Object lock = new Object();
   private boolean m_circularity;

   public UnifiedLoaderTestCase(String name)
   {
      super(name);
   }

   /** Test the UnifiedLoaderRepository for ClassCircularityError
    */
   public void testClassCircularityErrorMBean() throws Exception
   {
      try 
      {
         deploy("circularity.sar");
         ObjectName testObjectName = new ObjectName("jboss.test:name=CircularityError");
         boolean isRegistered = getServer().isRegistered(testObjectName);
         assertTrue("jboss.test:name=CircularityError isRegistered", isRegistered);
      }
      finally
      {
         undeploy("circularity.sar");
      } // end of try-finally
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
         deploy("loadingresource.sar");
         ObjectName testObjectName = new ObjectName("jboss.test:name=LoadingResource");
         boolean isRegistered = getServer().isRegistered(testObjectName);
         assertTrue("jboss.test:name=LoadingResource isRegistered", isRegistered);
      }
      finally
      {
         undeploy("loadingresource.sar");
      } // end of try-finally
   }

   protected void debug(String message)
   {
      getLog().debug(message);
   }
}
