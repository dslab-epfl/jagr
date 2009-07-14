/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.util.test;

import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import junit.framework.*;

import org.jboss.test.JBossTestCase;

/**
* Test case for the Scheduler Utility. The test
* checks if multiple scheduler can be created,
* that the notifications goes to the right target
* and that the reuse of the Scheduler works.
*
* @see org.jboss.util.Scheduler
* @see org.jboss.util.SchedulerMBean
* @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
*
* <p><b>Revisions:</b></p>
* <p><b>20011213: Andy Schaefer</b>
* <ul>
* <li>Creation</li>
* </ul>
* </p>
*
* @version   $Revision: 1.1.1.1 $
**/
public class SchedulerUnitTestCase
       extends JBossTestCase
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   /**
    * Constructor for the SchedulerUnitTestCase object
    *
    * @param pName Test case name
    */
   public SchedulerUnitTestCase( String pName )
   {
      super( pName );
   }

   // Public --------------------------------------------------------
   
   /**
    * Checks if the Scheduler is deployed and if not then
    * deployed the default one now.
    **/
   public void testDefaultScheduler()
      throws
         Exception
   {
      boolean lDeployed = false;
      try {
         lDeployed = getServer().isRegistered(
            new ObjectName( getServer().getDefaultDomain() + ":service=Scheduler" )
         );
      }
      catch( Exception e ) {
         getLog().error( "Failed to check if default Scheduler is already deployed", e );
      }
      if( !lDeployed ) {
         // The class loader used to locate the configuration file
         ClassLoader lLoader = Thread.currentThread().getContextClassLoader();
         assertTrue( "ContextClassloader missing", lLoader != null );
         //Get URL for deployable *service.xml file in resources
         URL lUrl = lLoader.getResource( "util/test-default-scheduler-service.xml" );
         if( lUrl == null )
         {
            //if we're running from the jmxtest.jar, it should be here instead
            lUrl = lLoader.getResource( "test-default-scheduler-service.xml" );
         }
         assertTrue( "resource test-default-scheduler-service.xml not found", lUrl != null );
         try 
         {
            deploy( lUrl + "" );
         }
         finally
         {
            undeploy( lUrl + "" );
         } // end of try-finally
         
      }
   }
   
}
