/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.test.classloader.concurrentload;

import java.util.Vector;

import org.jboss.logging.Logger;
import org.jboss.system.Service;
import org.jboss.system.ServiceMBeanSupport;

/** A multi-threaded class loading test service.
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>16. mai 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class ConcurrentLoader
       extends ServiceMBeanSupport
       implements ConcurrentLoaderMBean
{

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   public Object lock = new Object ();

   public final static int MAX_CLASSES = 10;
   public final static int NUMBER_OF_LOADING = 10;
   public final static int NUMBER_OF_THREADS = 20;

   public Vector ungarbaged = null;


   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public ConcurrentLoader ()
   {
   }

   // Public --------------------------------------------------------

   // Z implementation ----------------------------------------------

   // ServiceMBeanSupport overrides ---------------------------------------------------

   protected void createService() throws Exception
   {
      ungarbaged = new Vector ();
      log.debug("Creating threads...");
      for (int t=0; t<NUMBER_OF_THREADS; t++)
      {
         ConcurrentLoader.Loader loader = new ConcurrentLoader.Loader (t);
         loader.start ();
         ungarbaged.add (loader);
      }
      log.debug("...threads created");
      synchronized (this)
      {
         this.wait (2000);
      }

	   log.debug("unlocked!");
      synchronized (lock)
      {
         lock.notifyAll ();
      }
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

   class Loader extends Thread
   {
      int classid = 0;
      Logger log;

      public Loader (int classid)
      {
         super("ConcurrentLoader - Thread #" + classid);
         this.classid = classid;
         this.log = getLog();
      }

      public void run ()
      {
         int modId = classid % MAX_CLASSES;
         String className = this.getClass ().getPackage ().getName () + ".Anyclass" + modId;
         ClassLoader cl = this.getContextClassLoader ();

         synchronized (lock)
         {
            try
            {
               log.debug("Thread ready: " + classid);
               lock.wait ();
            }
            catch (Exception e)
            {
               log.error("Error during wait", e);
            }
         }
         log.debug("loading class... " + className);
         for (int i=0; i<NUMBER_OF_LOADING; i++)
         {
            log.debug("loading class with id " + classid + " for the " + i + "th time");
            try
            {
               log.debug("before load...");
               cl.loadClass (className);
               log.debug("Class " + className + " loaded.");
            }
            catch (Exception e)
            {
               e.printStackTrace ();
            }
         }
         log.debug("...Done loading classes. " + classid);
      }
   }

}
