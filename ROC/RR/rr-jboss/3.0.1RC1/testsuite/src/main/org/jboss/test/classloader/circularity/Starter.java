package org.jboss.test.classloader.circularity;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.test.classloader.circularity.test.CircularityErrorTests;

/**
 *
 * @version $Revision: 1.1.1.1 $
 */
public class Starter extends ServiceMBeanSupport implements StarterMBean
{
   protected void createService() throws Exception
   {
      new CircularityErrorTests().testClassCircularityError();
   }
}
