/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.tm;

import java.io.File;
import java.net.URL;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

/**
 *  This is a JMX service which manages the TransactionManager.
 *  The service creates it and binds a Reference to it into JNDI.
 *      
 *  @see TxManager
 *  @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 *  @version $Revision: 1.1.1.1 $
 *
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */
public class TransactionManagerService
   extends ServiceMBeanSupport
   implements TransactionManagerServiceMBean, ObjectFactory
{
   // Constants -----------------------------------------------------
   public static String JNDI_NAME = "java:/TransactionManager";
   public static String JNDI_IMPORTER = "java:/TransactionPropagationContextImporter";
   public static String JNDI_EXPORTER = "java:/TransactionPropagationContextExporter";
    
   // Attributes ----------------------------------------------------

   //private MBeanServer server;

   private int timeout = 300; // default tx timeout, dupl. in TM when it exists.
   //private String xidClassName = null;
   private ObjectName xidFactory;

   // Static --------------------------------------------------------

   static TxManager tm;

   // ServiceMBeanSupport overrides ---------------------------------

    
   protected void startService()
      throws Exception
   {
      // Initialize the Xid constructor.
      /*
      if (xidClassName != null) {
         if (log.isInfoEnabled())
            log.info("Using Xid class '" + xidClassName + "'");
         Class cls = Class.forName(xidClassName);

         TxCapsule.xidConstructor = cls.getConstructor(
            new Class[] { Integer.TYPE, byte[].class, byte[].class });
      }
      */
      XidFactoryMBean xidFactoryObj = (XidFactoryMBean)getServer().getAttribute(xidFactory, "Instance");
      TxCapsule.xidFactory = xidFactoryObj;
      // Get a reference to the TxManager singleton.
      tm = TxManager.getInstance();
      // Set its default timeout.
      tm.setDefaultTransactionTimeout(timeout);

      // Bind reference to TM in JNDI
      // Our TM also implement the tx importer and exporter
      // interfaces, so we bind it under those names too.
      // Other transaction managers may have seperate
      // implementations of these objects, so they are
      // accessed under seperate names.
      bindRef(JNDI_NAME, "org.jboss.tm.TxManager");
      bindRef(JNDI_IMPORTER, "org.jboss.tm.TransactionPropagationContextImporter");
      bindRef(JNDI_EXPORTER, "org.jboss.tm.TransactionPropagationContextFactory");
   }
    
   protected void stopService()
   {
      try {
         // Remove TM, importer and exporter from JNDI
         Context ctx = new InitialContext();
         ctx.unbind(JNDI_NAME);
         ctx.unbind(JNDI_IMPORTER);
         ctx.unbind(JNDI_EXPORTER);
      } catch (Exception e) {
         log.error("Failed to clear JNDI bindings", e);
      }
   }
    
   /**
    * Describe <code>getTransactionTimeout</code> method here.
    *
    * @return an <code>int</code> value
    * @jmx:managed-attribute
    */
   public int getTransactionTimeout() {
      if (tm != null) // Get timeout value from TM (in case it was changed).
         timeout = tm.getDefaultTransactionTimeout();
      return timeout;
   }

   /**
    * Describe <code>setTransactionTimeout</code> method here.
    *
    * @param timeout an <code>int</code> value
    * @jmx:managed-attribute
    */
   public void setTransactionTimeout(int timeout) {
      this.timeout = timeout;
      if (tm != null) // Update TM default timeout
         tm.setDefaultTransactionTimeout(timeout);
   }


   
   
   /**
    * mbean get-set pair for field xidFactory
    * Get the value of xidFactory
    * @return value of xidFactory
    *
    * @jmx:managed-attribute
    */
   public ObjectName getXidFactory()
   {
      return xidFactory;
   }
   
   
   /**
    * Set the value of xidFactory
    * @param xidFactory  Value to assign to xidFactory
    *
    * @jmx:managed-attribute
    */
   public void setXidFactory(ObjectName xidFactory)
   {
      this.xidFactory = xidFactory;
   }
   
   

   //public String getXidClassName() { return xidClassName; }
   //public void setXidClassName(String name) { xidClassName = name; }


   // ObjectFactory implementation ----------------------------------

   public Object getObjectInstance(Object obj, Name name,
                                   Context nameCtx, Hashtable environment)
      throws Exception
   {
      // Return the transaction manager
      return tm;
   }


   // Private -------------------------------------------------------

   private void bindRef(String jndiName, String className)
      throws Exception
   {
      Reference ref = new Reference(className, getClass().getName(), null);
      new InitialContext().bind(jndiName, ref);
   }
}

