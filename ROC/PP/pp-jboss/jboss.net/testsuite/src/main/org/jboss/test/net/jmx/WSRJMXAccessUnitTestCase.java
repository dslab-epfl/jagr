
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: WSRJMXAccessUnitTestCase.java,v 1.1.1.1 2003/03/07 08:26:04 emrek Exp $
package org.jboss.test.net.jmx;



import org.jboss.net.axis.AxisInvocationHandler;
import org.jboss.net.jmx.MBeanInvocationHandler;
import org.jboss.net.jmx.adaptor.RemoteAdaptor;
import org.jboss.net.jmx.adaptor.RemoteAdaptorInvocationHandler;
import org.jboss.net.jmx.MBeanInvocationHandler;
import org.jboss.test.net.AxisTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import javax.management.ObjectName;
import java.net.URL;


/**
 * Tests remote accessibility of JMX MBean through the web service
 * @created 29 March
 * @author <a href="mailto:pbraswell@utopiansoft.com">Peter Braswell</a>
 * @version $Revision: 1.1.1.1 $
 */

public class WSRJMXAccessUnitTestCase
   extends AxisTestCase
{

   /**
    * Method setUp
    *
    *
    * @throws Exception
    *
    */

   public void setUp ()
      throws Exception
   {
      super.setUp();

   }

   public void testGetter( )
   {
       System.out.println("/* begin test: testGetter() */");
       System.out.println("Invoking MBean at ENDPOINT: " + JMX_END_POINT );
       try
       {
           MBeanInvocationHandler handler =
              createMBeanInvocationHandler(new URL(JMX_END_POINT));
           String str     =
              ( String ) handler.invoke("jboss.net:service=JMXTestMBean",   // serviceName
                                        "getTestString",   // methodName
                                    null,              // arguments
                                    null);             // classes           
           assertEquals( str,"JMX_TEST_STRING");
       }
       catch(Exception ex)
       {
           ex.printStackTrace();
       }
       System.out.println("/* end test: testGetter() */");
   }

   public void testSetter( )
   {
       System.out.println("/* begin test: testSetter() */");
       System.out.println("Invoking MBean at ENDPOINT: " + JMX_END_POINT );
       try
       {
           MBeanInvocationHandler handler =
              createMBeanInvocationHandler(new URL(JMX_END_POINT));
           handler.invoke("jboss.net:service=JMXTestMBean",         // serviceName
                                        "setTestString",            // methodName
                                    new String[] {"foo-dog"},  // arguments
                                    new Class[] {String.class});                     // classes           
           // invoke the getter and compare the answer with the 
           // set string value
           String str = 
                (String) handler.invoke("jboss.net:service=JMXTestMBean",         // serviceName
                                        "getTestString",            // methodName
                                    null,                      // arguments
                                    null);                     // classes           
           System.out.println("Checking: " + str + "==" + "'foo-dog'?");
           assertEquals( str, "foo-dog");   
       }
       catch(Exception ex)
       {
           ex.printStackTrace();
       }
       System.out.println("/* end test: testGetter() */");
   }

   public void testMethodInvoke( )
   {
       System.out.println("/* begin test: testMethodInvoke() */");
       System.out.println("Invoking MBean at ENDPOINT: " + JMX_END_POINT );
       try
       {
           MBeanInvocationHandler handler =
              createMBeanInvocationHandler(new URL(JMX_END_POINT));
           handler.invoke("jboss.net:service=JMXTestMBean",   // serviceName
                          "noopOperation",   // methodName
                        null,              // arguments
                        null);             // classes           
       }
       catch(Exception ex)
       {
           ex.printStackTrace();
       }
       System.out.println("/* end test: testMethodInvoke() */");
   }

   /**
    * Constructor WSRJMXAccessUnitTestCase
    *
    *
    * @param name
    *
    */

   public WSRJMXAccessUnitTestCase (String name)
   {
      super(name);
   }

   /**
    * Method suite
    *
    *
    * @return
    *
    * @throws Exception
    *
    */

   public static Test suite ()
      throws Exception
   {
      System.out.println("Deploying 'jmx-test.sar");
     return getDeploySetup(WSRJMXAccessUnitTestCase.class, "jmx-test.sar");
   }

   /* Member variables */
   protected String        JMX_END_POINT = END_POINT + "/JMXTest";
   protected static String AXIS_JMX_NAME = "jboss.net:service=Axis";
   private JMXTestMBean    mbean;
}



