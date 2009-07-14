/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.ha.framework.interfaces.GenericClusteringException;
import EDU.oswego.cs.dl.util.concurrent.FIFOReadWriteLock;

/**
 * Track the incoming invocations and when shuting down a container (stop or
 * destroy), waits for current invocations to finish before returning the
 * stop or destroy call. This interceptor can be important in clustered environment
 * where shuting down a node doesn't necessarly mean that an application cannot
 * be reached: other nodes may still be servicing. Consequently, it is important
 * to have a clean shutdown to keep a coherent behaviour cluster-wide.
 *
 * To avoid strange or inefficient behaviour, the facade session bean (if any)
 * should be stopped first thus not blocking invocations in a middle-step (i.e.
 * facade making multiple invocations to "sub-beans": if a "sub-bean" is
 * shut down, the facade will get an exception in the middle of its activity)
 *
 * @see <related>
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>14 avril 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class CleanShutdownInterceptor extends AbstractInterceptor
{

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   protected Container container = null;

   protected boolean allowInvocations = false;

   public long runningInvocations = 0;
   public long runningHomeInvocations = 0;

   protected FIFOReadWriteLock rwLock = new FIFOReadWriteLock();

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public CleanShutdownInterceptor ()
   {
   }

   // Public --------------------------------------------------------

   // Z implementation ----------------------------------------------

   // AbstractInterceptor overrides ---------------------------------------------------

   public void create() throws Exception {
      super.create ();
      this.allowInvocations = false;
   }

   public void start() throws Exception {
      super.start();
      this.allowInvocations = true;
   }

   public void stop() {
      super.stop ();
      this.log.debug ("Stopping container " + container.getJmxName () + ". " +
                        this.runningHomeInvocations + " current home invocations and " +
                        this.runningInvocations + " current remote invocations.");

      forbidInvocations ();
   }

   public void destroy() {
      super.destroy ();

      this.log.debug ("Destroying container " + container.getJmxName ().toString () + ". " +
                        this.runningHomeInvocations + " current home invocations and " +
                        this.runningInvocations + " current remote invocations.");

      forbidInvocations() ;
   }

   public Object invokeHome (Invocation mi)
   throws Exception
   {
      if (this.allowInvocations)
      {
         // we need to acquire the read lock. If we cannot directly, it means
         // that the stop/destroy call has gotten the write lock in the meantime
         //
         try
         {
            rwLock.readLock ().attempt (0);
         }
         catch (java.lang.InterruptedException ie)
         {
            throw new GenericClusteringException (GenericClusteringException.COMPLETED_NO,
                                                  "Container is shuting down on this node");
         }

         runningHomeInvocations++;
         try
         {
            return this.getNext ().invokeHome (mi);
         }
         catch (GenericClusteringException gce)
         {
            // a gce exception has be thrown somewhere else: we need to modify its flag
            // and forward it. We could add optimisations at this level by having some
            // "idempotent" flag at the container level
            //
            gce.setCompletionStatus (gce.COMPLETED_MAYBE);
            throw gce;
         }
         finally
         {
            runningHomeInvocations--;
            rwLock.readLock ().release ();
         }
      }
      else
         throw new GenericClusteringException (GenericClusteringException.COMPLETED_NO,
                                               "Container is shuting down on this node");
   }

   public Object invoke (Invocation mi)
   throws Exception
   {
      if (this.allowInvocations)
      {
         // we need to acquire the read lock. If we cannot directly, it means
         // that the stop/destroy call has gotten the write lock in the meantime
         //
         try
         {
            rwLock.readLock ().attempt (0);
         }
         catch (java.lang.InterruptedException ie)
         {
            throw new GenericClusteringException (GenericClusteringException.COMPLETED_NO,
                                                  "Container is shuting down on this node");
         }

         runningInvocations++;
         try
         {
            return this.getNext ().invoke (mi);
         }
         catch (GenericClusteringException gce)
         {
            // a gce exception has be thrown somewhere else: we need to modify its flag
            // and forward it. We could add optimisations at this level by having some
            // "idempotent" flag at the container level
            //
            gce.setCompletionStatus (gce.COMPLETED_MAYBE);
            throw gce;
         }
         finally
         {
            runningInvocations--;
            rwLock.readLock ().release ();
         }
      }
      else
         throw new GenericClusteringException (GenericClusteringException.COMPLETED_NO,
                                               "Container is shuting down on this node");
   }

   public Container getContainer ()
   {
      return this.container;
   }

   /** This callback is set by the container so that the plugin may access it
    *
    * @param con    The container using this plugin.
    */
   public void setContainer (Container con)
   {
      this.container = con;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   protected void forbidInvocations ()
   {
      this.allowInvocations = false;

      try
      {
         this.rwLock.writeLock ().acquire ();
      }
      catch (Exception e) {}
      finally
      {
         this.rwLock.writeLock ().release ();
      }
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
