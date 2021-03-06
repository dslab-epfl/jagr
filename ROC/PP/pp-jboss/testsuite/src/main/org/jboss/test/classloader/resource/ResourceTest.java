package org.jboss.test.classloader.resource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.jboss.system.ServiceMBeanSupport;

/**
A simple service to test resource loading.

@author Adrian.Brock@HappeningTimes.com
@version $Revision: 1.1.1.1 $
 */
public class ResourceTest
   extends ServiceMBeanSupport
   implements ResourceTestMBean, Runnable
{
   private Exception threadEx;
   private boolean running;

   protected void startService()
      throws Exception
   {
      // Run a thread in the background looking for rsrc using a different loader
      Thread t = new Thread(this, "RsrcLoader");
      synchronized( ResourceTest.class )
      {
         t.start();
         ResourceTest.class.wait();
      }

      loadLocalResource();
      loadGlobalResource();
      running = false;
      t.join();
      if( threadEx != null )
         throw threadEx;
   }

   protected void stopService()
      throws Exception
   {
      running = false;
   }

   /**
    * Checks we can find a local resource in our deployment unit
    */
   public void loadLocalResource()
      throws Exception
   {
      log.info("Looking for resource: META-INF/jboss-service.xml");
      ClassLoader cl = getClass().getClassLoader();
      URL serviceXML = cl.getResource("META-INF/jboss-service.xml");
      if (serviceXML == null)
         throw new Exception("Cannot find META-INF/jboss-service.xml");
      log.info("Found META-INF/jboss-service.xml: "+serviceXML);
      InputStream is = serviceXML.openStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      String line = reader.readLine();
      boolean foundService = false;
      while (line != null && foundService == false )
      {
         if (line.indexOf("org.jboss.test.classloader.resource.ResourceTest") != -1)
            foundService = true;
         line = reader.readLine();
      }
      is.close();
      if( foundService == false )
         throw new Exception("Wrong META-INF/jboss-service.xml");

      // Look for the dtds/sample.dtd
      log.info("Looking for resource: dtds/sample.dtd");
      URL dtd = cl.getResource("dtds/sample.dtd");
      if( dtd == null )
      {
         log.info("Looking for resource: /dtds/sample.dtd");
         dtd = cl.getResource("/dtds/sample.dtd");
      }
      if( dtd == null )
         throw new Exception("Failed to find dtds/sample.dtd or /dtds/sample.dtd");
      log.info("Found sample.dtd: "+dtd);
   }

   /**
    * Checks we can find a global resource located in the conf dir
    */
   public void loadGlobalResource()
      throws Exception
   {
      ClassLoader loader = getClass().getClassLoader();
      log.info("loadGlobalResource, loader="+loader);
      URL resURL = loader.getResource("standardjboss.xml");
      if (resURL == null)
         throw new Exception("Cannot find standardjboss.xml");
      resURL = loader.getResource("log4j.xml");
      if (resURL == null)
         throw new Exception("Cannot find log4j.xml");
      resURL = loader.getResource("jndi.properties");
      if (resURL == null)
         throw new Exception("Cannot find jndi.properties");
   }

   /** Load resources in the background to test MT access to the repository
    * during resource lookup
    */
   public void run()
   {
      ClassLoader loader = getClass().getClassLoader();
      do
      {
         synchronized( ResourceTest.class )
         {
            ResourceTest.class.notify();
            log.info("Notified start thread");
         }
         // Load some resouces located from the JavaMail mail.jar
         try
         {
            javax.mail.Session.getInstance(System.getProperties());

            Class sessionClass = loader.loadClass("javax.mail.Session");
            log.info("Loading JavaMail resources using: "+sessionClass.getClassLoader());
            URL resURL = sessionClass.getResource("/META-INF/javamail.default.address.map");
            if( resURL == null )
               throw new Exception("Failed to find javamail.default.address.map");
            resURL = sessionClass.getResource("/META-INF/javamail.default.providers");
            if( resURL == null )
               throw new Exception("Failed to find javamail.default.providers");
            resURL = sessionClass.getResource("/META-INF/javamail.charset.map");
            if( resURL == null )
               throw new Exception("Failed to find javamail.charset.map");
            resURL = sessionClass.getResource("/META-INF/mailcap");
            if( resURL == null )
               throw new Exception("Failed to find mailcap");
            log.info("Found all JavaMail resources");
            // Look for a resource that does not exist
            resURL = sessionClass.getResource("nowhere-to-be-found.xml");
         }
         catch(Exception e)
         {
            threadEx = e;
            log.error("Failed to load resource", e);
            break;
         }
      } while( running );
   }
}
