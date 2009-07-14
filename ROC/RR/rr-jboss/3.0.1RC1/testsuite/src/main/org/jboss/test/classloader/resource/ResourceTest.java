package org.jboss.test.classloader.resource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jboss.system.ServiceMBeanSupport;

/** 
A simple service to test resource loading.

@author Adrian.Brock@HappeningTimes.com
@version $Revision: 1.1.1.1 $
 */
public class ResourceTest 
   extends ServiceMBeanSupport
   implements ResourceTestMBean
{
   protected void startService()
      throws Exception
   {
      loadLocalResource();
      loadGlobalResource();
   }

   /**
    * Checks we can find a local resource in our deployment unit
    */
   public void loadLocalResource() 
      throws Exception
   {
      InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/jboss-service.xml");
      if (is == null)
         throw new Exception("Cannot find META-INF/jboss-service.xml");
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      String line = reader.readLine();
      while (line != null)
      {
         if (line.indexOf("org.jboss.test.classloader.resource.ResourceTest") != -1)
            return;
         line = reader.readLine();
      }
      throw new Exception("Wrong META-INF/jboss-service.xml");
   }

   /**
    * Checks we can find a global resource
    */
   public void loadGlobalResource() 
      throws Exception
   {
      InputStream is = getClass().getClassLoader().getResourceAsStream("standardjboss.xml");
      if (is == null)
         throw new Exception("Cannot find standardjboss.xml");
   }
}
