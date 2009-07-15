/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.varia.deployment.convertor;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.deployment.DeploymentInfo;

import javax.management.ObjectName;
import javax.management.JMException;
import java.util.Properties;
import java.util.jar.JarFile;
import java.io.File;
import java.net.URL;

/**
 * Defines the methods of a Converter
 *
 * @author <a href="mailto:aloubyansky@hotmail.com">Alex Loubyansky</a>
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 1.1.1.1 $
 *
 * @jmx.mbean
 *    name="jboss.system:service=Convertor,type=WebLogic"
 *    extends="org.jboss.system.ServiceMBean"
 */
public class WebLogicConvertor
   extends ServiceMBeanSupport
   implements Convertor, WebLogicConvertorMBean
{
   // Attributes ----------------------------------------------------
   /** the deployer name this converter is registered with */
   private String deployerName;

   /** the version of xsl resources to apply */
   private String wlVersion;

   /** remove-table value */
   private String removeTable;

   /** datasource name that will be set up for converted bean */
   private String datasource;

   /** the datasource mapping for the datasource */
   private String datasourceMapping;

   /** xsl parameters used in transformations */
   private Properties xslParams;

   // Public --------------------------------------------------------
   /**
    * @jmx.managed-attribute
    */
   public String getDeployer()
   {
      return deployerName;
   }
   /**
    * @jmx.managed-attribute
    */
   public void setDeployer( String name )
   {
      if( deployerName != null && name!= null && deployerName != name )
      {
         // Remove deployer
         try
         {
            server.invoke(
               new ObjectName( deployerName ),
               "removeConvertor",
               new Object[] { this },
               new String[] { this.getClass().getName() }
            );
         }
         catch( JMException jme ) { }
      }
      if( name != null ) deployerName = name;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getWlVersion()
   {
      return wlVersion;
   }
   /**
    * @jmx.managed-attribute
    */
   public void setWlVersion( String wlVersion )
   {
      this.wlVersion = wlVersion;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getRemoveTable()
   {
      return removeTable;
   }
   /**
    * @jmx.managed-attribute
    */
   public void setRemoveTable( String removeTable )
   {
      this.removeTable = removeTable;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getDatasource()
   {
      return datasource;
   }
   /**
    * @jmx.managed-attribute
    */
   public void setDatasource( String datasource )
   {
      this.datasource = datasource;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getDatasourceMapping()
   {
      return datasourceMapping;
   }
   /**
    * @jmx.managed-attribute
    */
   public void setDatasourceMapping( String datasourceMapping )
   {
      this.datasourceMapping = datasourceMapping;
   }

   public Properties getXslParams()
   {
      if( xslParams == null )
      {
         log.warn( "xmlParams should have been initialized!" );
         xslParams = initXslParams();
      }

      // xsl resources path
      xslParams.setProperty( "resources_path", "resources/" + wlVersion + "/" );

      // set remove-table
      xslParams.setProperty( "remove-table", removeTable );

      // datasource
      xslParams.setProperty( "datasource", datasource );

      // datasource-mapping
      xslParams.setProperty( "datasource-mapping", datasourceMapping );

      return xslParams;
   }

   // ServiceMBeanSupport overridding ------------------------------
   public void startService()
   {
      try
      {
         // init xsl params first
         initXslParams();

         server.invoke(
            new ObjectName( deployerName ),
            "addConvertor",
            new Object[] { this },
            new String[] { Convertor.class.getName() }
         );
      }
      catch( JMException jme )
      {
         log.error( "Caught exception during startService()", jme );
      }
   }

   public void stopService()
   {
      if( deployerName != null )
      {
         // Remove deployer
         try {
            server.invoke(
               new ObjectName( deployerName ),
               "removeConvertor",
               new Object[] { this },
               new String[] { this.getClass().getName() }
            );
         }
         catch( JMException jme )
         {
            // Ingore
         }
      }
   }

   // Converter implementation ----------------------------------------
   /**
    * Checks if the deployment can be converted to a JBoss deployment
    * by this converter.
    *
    * @param di The deployment to be converted
    * @param path Path of the extracted deployment
    *
    * @return true if this converter is able to convert
    */
   public boolean accepts( DeploymentInfo di, File path )
   {
      log.debug( "accepts(), path: " + path );

      String url = di.url.toString();

      // File path could be removed from the parameter list
      // String url = di.url.toString();
      // File webLogicDD = new File( path, "META-INF/weblogic-ejb-jar.xml" );

      JarFile jarFile = null;
      try
      {
         jarFile = new JarFile( di.url.getPath() );
      }
      catch(Exception e)
      {
         log.debug( "Couldn't create JarFile for " + di.url.getPath(), e );
         return false;
      }

      return ( jarFile.getEntry("META-INF/weblogic-ejb-jar.xml" ) != null )
         && url.endsWith( ".wlar" );
   }

   /**
    * Converts the necessary files to make the given deployment deployable
    * on JBoss
    *
    * @param di The deployment to be converted
    * @param path Path of the extracted deployment
    **/
   public void convert( DeploymentInfo di, File path )
      throws Exception
   {
      log.debug( "convert(), path: " + path );

      Properties xslParams = getXslParams();
      log.debug( "convert(), xslParams: " + xslParams );
      JarTransformer.transform( path, xslParams );
   }


   // Private -------------------------------------------------------
   private Properties initXslParams()
   {
      xslParams = new Properties();

      ClassLoader cl = Thread.currentThread().getContextClassLoader();

      // path to standardjboss.xml
      URL url = cl.getResource( "standardjboss.xml" );
      if( url != null )
         xslParams.setProperty( "standardjboss",
            new File( url.getFile()).getAbsolutePath() );
      else log.debug( "standardjboss.xml not found." );

      // path to standardjbosscmp-jdbc.xml
      url = cl.getResource( "standardjbosscmp-jdbc.xml" );
      if( url != null )
         xslParams.setProperty( "standardjbosscmp-jdbc",
            new File( url.getFile()).getAbsolutePath() );
      else log.debug( "standardjbosscmp-jdbc.xml not found." );

      log.debug( "initialized xsl parameters: " + xslParams );

      return xslParams;
   }
}
