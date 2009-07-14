/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.deployment.scanner;

import java.io.File;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;

import java.net.URL;
import java.net.MalformedURLException;

import javax.management.ObjectName;
import javax.management.MBeanServer;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.MissingAttributeException;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.Deployer;

import org.jboss.logging.Logger;

import org.jboss.util.NullArgumentException;
import org.jboss.util.MuLong;
import org.jboss.util.MuBoolean;
import org.jboss.util.jmx.MBeanProxy;
import org.jboss.util.jmx.MBeanProxyInstance;

/**
 * An abstract support class for implementing a deployment scanner.
 *
 * <p>Provides the implementation of period-based scanning, as well
 *    as Deployer integration.
 *
 * <p>Sub-classes only need to implement {@link DeploymentScanner#scan}.
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public abstract class AbstractDeploymentScanner
   extends ServiceMBeanSupport
   implements DeploymentScanner, DeploymentScannerMBean
{
   /** The scan period in milliseconds */
   protected MuLong scanPeriod = new MuLong(5000);

   /** True if period based scanning is enabled. */
   protected MuBoolean scanEnabled = new MuBoolean(true);

   /** A proxy to the deployer we are using. */
   protected Deployer deployer;

   /** The scanner thread. */
   protected ScannerThread scannerThread;


   /////////////////////////////////////////////////////////////////////////
   //                           DeploymentScanner                         //
   /////////////////////////////////////////////////////////////////////////

   public void setDeployer(final ObjectName deployerName)
   {
      if (deployerName == null)
         throw new NullArgumentException("deployerName");

      deployer = (Deployer)
         MBeanProxy.create(Deployer.class, deployerName, server);
   }

   public ObjectName getDeployer()
   {
      return ((MBeanProxyInstance)deployer).getMBeanProxyObjectName();
   }

   /**
    * Period must be >= 0.
    */
   public void setScanPeriod(final long period)
   {
      if (period < 0)
         throw new IllegalArgumentException("ScanPeriod must be >= 0; have: " + period);

      this.scanPeriod.set(period);
   }

   public long getScanPeriod()
   {
      return scanPeriod.longValue();
   }

   public void setScanEnabled(final boolean flag)
   {
      this.scanEnabled.set(flag);
   }

   public boolean isScanEnabled()
   {
      return scanEnabled.get();
   }


   /////////////////////////////////////////////////////////////////////////
   //                           Scanner Thread                            //
   /////////////////////////////////////////////////////////////////////////

   /**
    * Should use Timer/TimerTask instead?  This has some issues with
    * interaction with ScanEnabled attribute.  ScanEnabled works only
    * when starting/stopping.
    */
   public class ScannerThread
      extends Thread
   {
      /** We get our own logger. */
      protected Logger log = Logger.getLogger(ScannerThread.class);

      /** True if the scan loop should run. */
      protected boolean enabled;

      /** True if we are shutting down. */
      protected boolean shuttingDown;

      /** Lock/notify object. */
      protected Object lock = new Object();

      public ScannerThread(boolean enabled)
      {
         super("ScannerThread");

         this.enabled = enabled;
      }

      public void setEnabled(boolean enabled)
      {
         this.enabled = enabled;

         synchronized (lock)
         {
            lock.notifyAll();
         }

         if (log.isDebugEnabled())
         {
            log.debug("Notified that enabled: " + enabled);
         }
      }

      public void shutdown()
      {
         enabled = false;
         shuttingDown = true;

         synchronized (lock)
         {
            lock.notifyAll();
         }

         if (log.isDebugEnabled())
         {
            log.debug("Notified to shutdown");
         }
      }
    
      public void run()
      {
         log.info("Running");

         while (!shuttingDown)
         {

            // If we are not enabled, then wait
            if (!enabled)
            {
               try
               {
                  log.debug("Disabled, waiting for notification");
                  synchronized (lock)
                  {
                     lock.wait();
                  }
               }
               catch (InterruptedException ignore)
               {

               }
            }

            loop();
         }

         log.info("Shutdown");
      }

      public void doScan()
      {
         try
         {
            scan();
         }
         catch (Exception e)
         {
            log.error("Scanning failed; continuing", e);
         }
      }

      protected void loop()
      {
         while (enabled)
         {
            // Scan for new/removed/changed/whatever
            doScan();

            // Sleep for scan period
            try
            {
               log.trace("Sleeping...");
               Thread.sleep(scanPeriod.longValue());
            }
            catch (InterruptedException ignore)
            {
            }
         }
      }

   }


   /////////////////////////////////////////////////////////////////////////
   //                     Service/ServiceMBeanSupport                     //
   /////////////////////////////////////////////////////////////////////////

   protected void createService() throws Exception
   {
      if (deployer == null)
         throw new MissingAttributeException("Deployer");

      // setup + start scanner thread
      scannerThread = new ScannerThread(false);
      scannerThread.setDaemon(true);
      scannerThread.start();
      log.debug("Scanner thread started");
   }
   
   protected void startService() throws Exception 
   {
      // scan before we enable the thread, so JBoss version shows up afterwards
      scan();

      // enable scanner thread if we are enabled
      scannerThread.setEnabled(scanEnabled.get());
   }
   
   protected void stopService() throws Exception 
   {
      // disable scanner thread
      scannerThread.setEnabled(false);
   }
   
   protected void destroyService() throws Exception 
   {
      // drop our ref to deployer, so scan will fail
      deployer = null;

      // shutdown scanner thread
      scannerThread.shutdown();

      // help gc
      scannerThread = null;
   }
}
