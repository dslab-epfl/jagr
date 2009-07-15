/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.varia.deployment;

import org.jboss.deployment.SubDeployerSupport;
import org.jboss.deployment.SubDeployer;
import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.server.ServerConfig;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.util.Counter;
import org.jboss.util.jmx.MBeanProxy;
import org.jboss.util.file.Files;
import org.jboss.util.file.JarUtils;
import org.jboss.varia.deployment.convertor.Convertor;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.ListIterator;
import java.net.URL;


/**
 * This is the deployer for other vendor's application
 * with dynamic migration of vendor-specific DDs to
 * JBoss specific DDs.
 *
 * @see org.jboss.varia.deployment.convertor.Convertor
 *
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 1.1.1.1 $
 *
 * @jmx.mbean
 *    name="jboss.system:service=ServiceDeployer"
 *    extends="org.jboss.deployment.SubDeployerMBean"
 */
public class FoeDeployer
	extends SubDeployerSupport
	implements SubDeployer, FoeDeployerMBean
{
   // Attributes ----------------------------------------------------
   /** A proxy to the MainDeployer. */
   protected MainDeployerMBean mainDeployer;

   /** A proxy to the ServiceControllerDeployer. */
   private ServiceControllerMBean serviceController;

   /** The deployers scratch directory. */
   private File scratchDirectory;

   /** Contains the list of available converters */
   private List converterList = new ArrayList();

   /** an increment for tmp files */
   private final Counter id = Counter.makeSynchronized(new Counter(0));

   // Public --------------------------------------------------------
   /**
    * @jmx.managed-operation
    */
   public boolean accepts( DeploymentInfo di )
   {
      // delegate accepts to convertors
      Iterator i = converterList.iterator();
      while( i.hasNext() )
      {
         Convertor converter = (Convertor)i.next();
         if(converter.accepts(di, null))
            return true;
      }
      return false;
   }

   /**
    * @jmx.managed-operation
    */
   public void init( DeploymentInfo di )
      throws DeploymentException
   {
      // invoke super-class initialization
      super.init(di);
   }

   /**
    * @jmx.managed-operation
    */
   public void create(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         log.debug( "create(), got info: " + di );

         // Inflate JAR file
         File destination = null;
         // Loop until a destination is found which does not exists yet
         while( destination == null || destination.exists() )
            destination = new File( scratchDirectory, id.increment() + "." + di.shortName );

         inflateJar( di.localUrl, destination );

         log.debug( "Foe has converters: " + converterList.size() );

         // Check for vendor specific deployment descriptors
         Iterator i = converterList.iterator();
         while(i.hasNext())
         {
            Convertor converter = (Convertor)i.next();

            if(converter.accepts( di, destination))
            {
               // Convert them to JBoss specific DDs
               converter.convert( di, destination );
               // Now conversion is done and we can leave
               break;
            }
         }

         // deflate the JAR file again
         File file = new File(
            scratchDirectory,
            di.shortName.substring( 0, di.shortName.length() - 4 ) + "jar"
         );
         deflateJar( file, destination );
         // re-deploy JAR file to deployment directory
         copyFile( file, new File( di.url.getFile() ).getParentFile() );
      }
      catch (Exception e)
      {
         log.error( "Problem loading meta data ", e );
      }
   }
   /**
    * This method stops this deployment because it is not of any
    * use anymore (conversion is done)
    * @jmx.managed-operation
    */
   public void start( DeploymentInfo di )
      throws DeploymentException
   {
      stop( di );
      destroy( di );
   }

   /**
    * @jmx.managed-operation
    */
   public void stop( DeploymentInfo di )
   {
      log.debug( "undeploying application: " + di.url );
   }

   /**
    * @jmx.managed-operation
    */
   public void destroy( DeploymentInfo di )
      //throws DeploymentException
   {
      List services = di.mbeans;
      int lastService = services.size();

      for( ListIterator i = services.listIterator(lastService); i.hasPrevious(); )
      {
         ObjectName name = (ObjectName)i.previous();
         log.debug( "destroying mbean " + name );
         try
         {
            serviceController.destroy( name );
         }
         catch( Exception e )
         {
            log.error( "Could not destroy mbean: " + name, e );
         }
      }

      for( ListIterator i = services.listIterator(lastService); i.hasPrevious(); )
      {
         ObjectName name = (ObjectName)i.previous();
         log.debug("removing mbean " + name);
         try
         {
            serviceController.remove( name );
         }
         catch( Exception e )
         {
            log.error( "Could not remove mbean: " + name, e );
         }
      }
   }

   /**
    * The startService method gets the mbeanProxies for MainDeployer
    * and ServiceController, used elsewhere.
    *
    * @exception Exception if an error occurs
    */
   protected void startService()
      throws Exception
   {
      mainDeployer = (MainDeployerMBean) MBeanProxy.create(
         MainDeployerMBean.class,
         MainDeployerMBean.OBJECT_NAME,
         server
      );

      // get the controller proxy
      serviceController = (ServiceControllerMBean) MBeanProxy.create(
         ServiceControllerMBean.class,
         ServiceControllerMBean.OBJECT_NAME,
         server
      );

      ServerConfig config = ServerConfigLocator.locate();

      // build the scratch directory
      File tempDirectory = config.getServerTempDir();
      scratchDirectory = new File( tempDirectory, "foe" );
      if( !scratchDirectory.exists() )
         scratchDirectory.mkdirs();

      // Note: this should go the last.
      // scratch directory must be created before this call
      super.startService();
   }

   public void destroyService()
      throws Exception
   {
      mainDeployer = null;
   }

   protected ObjectName getObjectName( MBeanServer server, ObjectName name )
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }

   /**
    * Add a new conveter to the list. If the same converter is
    * added this one is replaced, meaning everything stays the same.
    * This method is normally called by a Converter to be
    * called by this deployer to convert.
    *
    * @param converter New Converter to be added
    *
    * @jmx.managed-operation
    */
   public void addConvertor( Convertor converter )
   {
      converterList.add( converter );

      // try to deploy waiting deployment units
      // note: there is no need to synchronize, because MainDeployer
      // returns a copy of waiting deployments
      Collection waitingDeployments = mainDeployer.listWaitingForDeployer();
      if((waitingDeployments != null) && (waitingDeployments.size() > 0))
      {
         for( Iterator iter = waitingDeployments.iterator(); iter.hasNext(); )
         {
            DeploymentInfo di = (DeploymentInfo)iter.next();

            // check whether the converter accepts the deployment
            if( !converter.accepts(di, null) ) continue;

            log.debug( "trying to deploy with new converter: " + di.shortName );
            try
            {
               mainDeployer.undeploy( di );
               mainDeployer.deploy( di );
            }
            catch (DeploymentException e)
            {
               log.error( "DeploymentException while trying to deploy a package with new converter", e );
            }
         }
      }
   }

   /**
    * Removes a conveter from the list of converters. If the
    * converter does not exist nothing happens.
    * This method is normally called by a Converter to be removed
    * from the list if not serving anymore.
    *
    * @param converter Conveter to be removed from the list
    *
    * @jmx.managed-operation
    */
   public void removeConvertor( Convertor converter )
   {
      converterList.remove( converter );
   }

   // Private --------------------------------------------------------
   /**
    * The <code>inflateJar</code> copies the jar entries
    * from the jar url jarUrl to the directory destDir.
    *
    * @param fileURL URL pointing to the file to be inflated
    * @param destinationDirectory Directory to which the content shall be inflated to
    *
    * @exception DeploymentException if an error occurs
    * @exception IOException if an error occurs
    */
   protected void inflateJar( URL fileURL, File destinationDirectory )
      throws DeploymentException, IOException
   {
      InputStream input = new FileInputStream( fileURL.getFile() );
      JarUtils.unjar( input, destinationDirectory );
   }

   /**
    * Deflate a given directory into a JAR file
    *
    * @param jarFile The JAR file to be created
    * @param root Root directory of the files to be included (this directory
    *             will not be included in the path of the JAR content)
    **/
   private void deflateJar( File jarFile, File root )
      throws Exception
   {
      OutputStream output = new FileOutputStream( jarFile );
      JarUtils.jar( output, root.listFiles(), null, null, null );
      output.close();
   }

   /**
    * Copies the given File to a new destination with the same name
    *
    * @param source The source file to be copied
    * @param destinationDirectory File pointing to the destination directory
    **/
   private void copyFile( File source, File destinationDirectory )
      throws Exception
   {
      File target = new File( destinationDirectory, source.getName() );
      // Move may fail if target is used (because it is deployed)
      // Use Files.copy instead
      Files.copy( source, target );
   }
}
