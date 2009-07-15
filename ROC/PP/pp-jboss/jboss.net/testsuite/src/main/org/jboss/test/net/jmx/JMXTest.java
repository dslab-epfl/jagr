package org.jboss.test.net.jmx;



/**
 * @version     1.0
 * @author
 */

public class JMXTest
   implements JMXTestMBean
{

   /**
    * Method getTestString
    *
    *
    * @return
    *
    */

   public String getTestString ()
   {
      return testString;
   }

   /**
    * Method setTestString
    *
    *
    * @param str
    *
    */

   public void setTestString (String str)
   {
      testString = str;
   }

   /**
    * Method noopOperation
    *
    *
    */

   public void noopOperation ()
   {

      /* doing nothing */
   }

   /* Member variables */
   private String testString = "JMX_TEST_STRING";
}



