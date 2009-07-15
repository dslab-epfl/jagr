package org.jboss.test.net.jmx;



/**
 * @version     1.0
 * @author     Peter Braswell
 */

public interface JMXTestMBean
{

   /**
    * Method getTestString
    *
    *
    * @return
    *
    */

   public String getTestString ();

   /**
    * Method setTestString
    *
    *
    * @param str
    *
    */

   public void setTestString (String str);

   /**
    * Method noopOperation
    *
    *
    */

   public void noopOperation ();
}



