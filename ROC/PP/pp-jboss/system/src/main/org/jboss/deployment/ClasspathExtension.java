
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.deployment;

import org.jboss.system.ServiceMBeanSupport;
import javax.management.ObjectName;
import java.net.URL;
import org.jboss.mx.loading.UnifiedClassLoader;


/**
 * ClasspathExtension.java
 *
 *
 * Created: Sun Jun 30 13:17:22 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 *
 * @jmx:mbean name="jboss:type=Service,service=JDOMetadata,flavor=LiDO"
 *            extends="org.jboss.system.ServiceMBean"
 */

public class ClasspathExtension 
   extends ServiceMBeanSupport
   implements ClasspathExtensionMBean
{

   private String metadataURL;

   private ObjectName loaderRepository = DeploymentInfo.DEFAULT_LOADER_REPOSITORY;

   private UnifiedClassLoader ucl;

   public ClasspathExtension() 
   {
      
   }

   
   
   /**
    * mbean get-set pair for field metadataURL
    * Get the value of metadataURL
    * @return value of metadataURL
    *
    * @jmx:managed-attribute
    */
   public String getMetadataURL()
   {
      return metadataURL;
   }
   
   
   /**
    * Set the value of metadataURL
    * @param metadataURL  Value to assign to metadataURL
    *
    * @jmx:managed-attribute
    */
   public void setMetadataURL(String metadataURL)
   {
      this.metadataURL = metadataURL;
   }
   
   
   
   /**
    * mbean get-set pair for field loaderRepository
    * Get the value of loaderRepository
    * @return value of loaderRepository
    *
    * @jmx:managed-attribute
    */
   public ObjectName getLoaderRepository()
   {
      return loaderRepository;
   }
   
   
   /**
    * Set the value of loaderRepository
    * @param loaderRepository  Value to assign to loaderRepository
    *
    * @jmx:managed-attribute
    */
   public void setLoaderRepository(ObjectName loaderRepository)
   {
      this.loaderRepository = loaderRepository;
   }
   
   

   protected void createService() throws Exception
   {
      if (metadataURL != null) 
      {
         URL url = new URL(metadataURL);
         ucl = new UnifiedClassLoader(url, url, server, loaderRepository);

      } // end of if ()
      
   }

   protected void destroyService() throws Exception
   {
      if (ucl != null) 
      {
         ucl.unregister();
      } // end of if ()
      
   }


}// ClasspathExtension
