/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.management.test;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.Set;
import javax.ejb.CreateException;
import javax.ejb.Handle;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.j2ee.ListenerRegistration;
import javax.management.j2ee.Management;
import javax.management.j2ee.ManagementHome;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;

import org.jboss.management.j2ee.J2EEDomain;
import org.jboss.management.j2ee.J2EEManagedObject;
import org.jboss.management.j2ee.J2EEServer;
import org.jboss.management.j2ee.JavaMailResource;
import org.jboss.management.j2ee.JCAConnectionFactory;

/**
 * Test of JSR-77 specification conformance using the ??ToDo
 * These test the basic JSR-77 handling and access.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.1.1.1 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011206 Andreas Schaefer:</b>
 * <ul>
 * <li> Creation
 * </ul>
 **/
public class JSR77SpecUnitTestCase
   extends JBossTestCase
{
   // Constants -----------------------------------------------------
   
   public static final String TEST_DATASOURCE = "hsqldbDS-LocalTxCM";
   public static final String TEST_MAIL = "Mail";
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   /**
    * Setup the test suite.
    */
   public static Test suite()
   {
      TestSuite lSuite = new TestSuite();
      lSuite.addTest( new TestSuite( JSR77SpecUnitTestCase.class ) );

      // Create an initializer for the test suite
      TestSetup lWrapper = new JBossTestSetup( lSuite )
      {
         protected void setUp() throws Exception
         {
            super.setUp();
//            deployJ2ee( "jsr77-spec.jar" );
//            flushAuthCache();
         }
         protected void tearDown() throws Exception
         {
//            undeployJ2ee( "jsr77-spec.jar" );
//            super.tearDown();
         
         }
      };
      return lWrapper;
   }
   
   // Constructors --------------------------------------------------
   
   public JSR77SpecUnitTestCase( String pName )
   {
      super( pName );
   }
   
   // Public --------------------------------------------------------
   
   /**
    * Test if a connection could be made to the Management MBean
    **/
   public void testConnect()
      throws
         Exception
   {
      getLog().debug("+++ testConnect");
      Management lManagement = getManagementEJB();
      String lDomain = lManagement.getDefaultDomain();
      getLog().debug( "+++ testConnect, domain: " + lDomain );
      lManagement.remove();
   }
   
   /**
    * Test if the management domain could be retrieved
    **/
   public void testGetManagementDomain()
      throws
         Exception
   {
      getLog().debug("+++ testGetManagementDomain");
      Management lManagement = getManagementEJB();
      Set lNames = lManagement.queryNames(
         new ObjectName(
            lManagement.getDefaultDomain() + ":" +
            J2EEManagedObject.TYPE + "=" + J2EEDomain.J2EE_TYPE + "," +
            "*"
         ),
         null
      );
      if( lNames.isEmpty() ) {
         fail( "Could not found JSR-77 root object of type '" + J2EEDomain.J2EE_TYPE + "'" );
      }
      if( lNames.size() > 1 ) {
         fail( "Found more than one JSR-77 root objects of type '" + J2EEDomain.J2EE_TYPE + "'" );
      }
      ObjectName lManagementDomain = (ObjectName) lNames.iterator().next();
      getLog().debug( "+++ testGetManagementDomain, root: " + lManagementDomain );
      lManagement.remove();
   }
   
   /**
    * Test if the deployed EJB could be found directly
    * and through navigation
    **/
   public void testGetDataSource()
      throws
         Exception
   {
      getLog().debug("+++ testGetDataSource");
      Management lManagement = getManagementEJB();
      Set lNames = lManagement.queryNames(
         getConnectionFactoryName(lManagement),
         null
      );
      if( lNames.isEmpty() ) {
         fail( "Could not found JSR-77 JDBC DataSource named '" + TEST_DATASOURCE + "'" );
      }
      if( lNames.size() > 1 ) {
         fail( "Found more than one JSR-77 JDBC DataSource named '" + TEST_DATASOURCE + "'" );
      }
      ObjectName lDataSource = (ObjectName) lNames.iterator().next();
      getLog().debug( "+++ testGetDataSource, " + TEST_DATASOURCE + ": " + lDataSource );
      getLog().debug(
         "+++ testGetDataSource, " + TEST_DATASOURCE + " status: " +
         lManagement.getAttribute( lDataSource, "State" )
      );
      lManagement.remove();
   }
   
   /**
    * Test if default Datasource could be stopped and restarted
    **/
   public void testRestartDatasource()
      throws
         Exception
   {
      getLog().debug("+++ testRestartDatasource");
      Management lManagement = getManagementEJB();
      Set lNames = lManagement.queryNames(
         getConnectionFactoryName(lManagement),
         null
      );
      if( lNames.isEmpty() ) {
         fail( "Could not found JSR-77 JDBC DataSource named '" + TEST_DATASOURCE + "'" );
      }
      if( lNames.size() > 1 ) {
         fail( "Found more than one JSR-77 JDBC DataSource named '" + TEST_DATASOURCE + "'" );
      }
      ObjectName lDataSource = (ObjectName) lNames.iterator().next();
      getLog().debug( "+++ testRestartDatasource, " + TEST_DATASOURCE + ": " + lDataSource );
      getLog().debug( "+++ testRestartDatasource, stop " + TEST_DATASOURCE + "" );
      lManagement.invoke( lDataSource, "stop", new Object[] {}, new String[] {} );
      getLog().debug( "+++ testRestartDatasource, start " + TEST_DATASOURCE + "" );
      lManagement.invoke( lDataSource, "start", new Object[] {}, new String[] {} );
      lManagement.remove();
   }
   
   /**
    * Test the notification delivery by restarting Default DataSource
    **/
    public void testNotificationDeliver()
      throws
         Exception
   {
      /* AS Taken out because there is a problem with JCA
      try {
      getLog().debug("+++ testNotificationDeliver");
      Management lManagement = getManagementEJB();
      Set lNames = lManagement.queryNames(
         getConnectionFactoryName(lManagement),
         null
      );
      if( lNames.isEmpty() ) {
         fail( "Could not found JSR-77 JDBC DataSource named '" + TEST_DATASOURCE + "'" );
      }
      if( lNames.size() > 1 ) {
         fail( "Found more than one JSR-77 JDBC DataSource named '" + TEST_DATASOURCE + "'" );
      }
      ObjectName lDataSource = (ObjectName) lNames.iterator().next();
      Listener lLocalListener = new Listener();
      ListenerRegistration lListenerFactory = lManagement.getListenerRegistry();
      getLog().debug( "+++ testNotificationDeliver, add Notification Listener to " + TEST_DATASOURCE + "" );
      lListenerFactory.addNotificationListener(
         lDataSource,
         lLocalListener,
         null,
         null
      );
      getLog().debug( "+++ testNotificationDeliver, stop " + TEST_DATASOURCE + "" );
      lManagement.invoke( lDataSource, "stop", new Object[] {}, new String[] {} );
      getLog().debug( "+++ testNotificationDeliver, start " + TEST_DATASOURCE + "" );
      lManagement.invoke( lDataSource, "start", new Object[] {}, new String[] {} );
      // Wait 5 seconds to ensure that the notifications are delivered
      Thread.sleep( 5000 );
      if( lLocalListener.getNumberOfNotifications() < 2 ) {
         fail( "Not enough notifications received: " + lLocalListener.getNumberOfNotifications() );
      }
      getLog().debug( "+++ testNotificationDeliver, remove Notification Listener from " + TEST_DATASOURCE + "" );
      lListenerFactory.removeNotificationListener(
         lDataSource,
         lLocalListener
      );
      lManagement.remove();
      }
      catch( Exception e ) {
         log.debug("failed", e);
         throw e;
      }
      */
      try {
      getLog().debug("+++ testNotificationDeliver");
      Management lManagement = getManagementEJB();
      Set lNames = lManagement.queryNames(
         getMailName(lManagement),
         null
      );
      if( lNames.isEmpty() ) {
         fail( "Could not found JSR-77 JDBC DataSource named '" + TEST_MAIL + "'" );
      }
      if( lNames.size() > 1 ) {
         fail( "Found more than one JSR-77 JDBC DataSource named '" + TEST_MAIL + "'" );
      }
      ObjectName lMail = (ObjectName) lNames.iterator().next();
      Listener lLocalListener = new Listener();
      ListenerRegistration lListenerFactory = lManagement.getListenerRegistry();
      getLog().debug( "+++ testNotificationDeliver, add Notification Listener to " + TEST_MAIL + "" );
      lListenerFactory.addNotificationListener(
         lMail,
         lLocalListener,
         null,
         null
      );
      getLog().debug( "+++ testNotificationDeliver, stop " + TEST_MAIL + "" );
      lManagement.invoke( lMail, "stop", new Object[] {}, new String[] {} );
      getLog().debug( "+++ testNotificationDeliver, start " + TEST_MAIL + "" );
      lManagement.invoke( lMail, "start", new Object[] {}, new String[] {} );
      // Wait 5 seconds to ensure that the notifications are delivered
      Thread.sleep( 5000 );
      if( lLocalListener.getNumberOfNotifications() < 2 ) {
         fail( "Not enough notifications received: " + lLocalListener.getNumberOfNotifications() );
      }
      getLog().debug( "+++ testNotificationDeliver, remove Notification Listener from " + TEST_MAIL + "" );
      lListenerFactory.removeNotificationListener(
         lMail,
         lLocalListener
      );
      lManagement.remove();
      }
      catch( Exception e ) {
         log.debug("failed", e);
         throw e;
      }
   }
// */
   
   // Z implementation ----------------------------------------------
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   private Management getManagementEJB()
      throws
         Exception
   {
      getLog().debug("+++ getManagementEJB()");
      Object lObject = getInitialContext().lookup( "ejb/mgmt/MEJB" );
      ManagementHome lHome = (ManagementHome) PortableRemoteObject.narrow(
         lObject,
         ManagementHome.class
      );
      getLog().debug( "Found JSR-77 Management EJB (MEJB)" );
      return lHome.create();
   }
   
   private ObjectName getConnectionFactoryName(Management lManagement) throws Exception
   {
      return new ObjectName(
            lManagement.getDefaultDomain() + ":" +
            J2EEManagedObject.TYPE + "=" + JCAConnectionFactory.J2EE_TYPE + "," +
            "name=" + TEST_DATASOURCE + "," +
            "*"
            );
   }
   
   private ObjectName getMailName(Management lManagement) throws Exception
   {
      return new ObjectName(
            lManagement.getDefaultDomain() + ":" +
            J2EEManagedObject.TYPE + "=" + JavaMailResource.J2EE_TYPE + "," +
            "name=" + TEST_MAIL + "," +
            "*"
            );
   }
   // Inner classes -------------------------------------------------
   
   private class Listener implements NotificationListener {
      
      private int mNrOfNotifications = 0;
      
      public int getNumberOfNotifications() {
         return mNrOfNotifications;
      }
      
      public void handleNotification( Notification pNotification, Object pHandbank ) {
         mNrOfNotifications++;
      }
   }
}
