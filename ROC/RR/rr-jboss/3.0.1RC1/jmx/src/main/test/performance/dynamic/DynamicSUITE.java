/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package test.performance.dynamic;

import junit.framework.Test;
import junit.framework.TestSuite;

public class DynamicSUITE extends TestSuite
{
   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("Performance tests for Dynamic MBeans");

      suite.addTest(new TestSuite(InvocationTEST.class));

      return suite;
   }

}
