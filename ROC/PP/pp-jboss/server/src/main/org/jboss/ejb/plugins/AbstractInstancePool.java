/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.ejb.EJBException;

import org.jboss.ejb.Container;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.EnterpriseContext;

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;
import org.jboss.logging.Logger;
import org.jboss.management.j2ee.CountStatistic;

import org.w3c.dom.Element;
import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;

/**
 *  Abstract Instance Pool class containing the basic logic to create
 *  an EJB Instance Pool.
 *
 *  @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 *  @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 *  @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 *  @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 *  @author <a href="mailto:scott.stark@jboss.org">Scott Stark/a>
 *  @version $Revision: 1.1.1.1 $
 */
public abstract class AbstractInstancePool
   implements InstancePool, XmlLoadable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   /** A FIFO semaphore that is set when the strict max size behavior is in effect.
    When set, only maxSize instances may be active and any attempt to get an
    instance will block until an instance is freed.
    */
   private FIFOSemaphore strictMaxSize;
   /** The time in milliseconds to wait for the strictMaxSize semaphore.
    */
   private long strictTimeout = Long.MAX_VALUE;
   protected Logger log = Logger.getLogger(this.getClass());
   /** The Container the instance pool is associated with */
   protected Container container;
   /** The pool data structure */
   protected LinkedList pool = new LinkedList();
   /** The maximum number of instances allowed in the pool */
   protected int maxSize = 30;
   /** determine if we reuse EnterpriseContext objects i.e. if we actually do pooling */
   protected boolean reclaim = false;

   /** Counter of all the Bean instantiated within the Pool **/
   protected CountStatistic mInstantiate = new CountStatistic("Instantiation",
      "", "Beans instantiated in Pool");
   /** Counter of all the Bean destroyed within the Pool **/
   protected CountStatistic mDestroy = new CountStatistic("Destroy", "",
      "Beans destroyed in Pool");
   /** Counter of all the ready Beans within the Pool (which are not used now) **/
   protected CountStatistic mReadyBean = new CountStatistic("ReadyBean", "",
      "Numbers of ready Bean Pool");


   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   /**
    *   Set the callback to the container. This is for initialization.
    *   The IM may extract the configuration from the container.
    *
    * @param   c
    */
   public void setContainer(Container c)
   {
      this.container = c;
   }

   /**
    * @return Callback to the container which can be null if not set proviously
    */
   public Container getContainer()
   {
      return container;
   }

   public void create()
      throws Exception
   {
   }

   public void start()
      throws Exception
   {
   }

   public void stop()
   {
   }

   public void destroy()
   {
     freeAll();
     this.container = null;
   }

   /**
    * A pool is reclaim if it push back its dirty instances in its stack.
    */
   public boolean getReclaim()
   {
      return reclaim;
   }

   public void setReclaim(boolean reclaim)
   {
      this.reclaim = reclaim;
   }

   /**
    * Add a instance in the pool
    */
   public void add()
      throws Exception
   {
      EnterpriseContext ctx = create(container.createBeanClassInstance());
      if( log.isTraceEnabled() )
         log.trace("Add instance "+this+"#"+ctx);
      synchronized (pool)
      {
         pool.addFirst(ctx);
      }
   }

   /**
    *   Get an instance without identity.
    *   Can be used by finders,create-methods, and activation
    *
    * @return     Context /w instance
    * @exception   RemoteException
    */
   public EnterpriseContext get()
      throws Exception
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("Get instance "+this+"#"+pool.size()+"#"+getContainer().getBeanClass());

      if( strictMaxSize != null )
      {
         // Block until an instance is available
         boolean acquired = strictMaxSize.attempt(strictTimeout);
         if( trace )
            log.trace("Acquired("+acquired+") strictMaxSize semaphore, remaining="+strictMaxSize.permits());
         if( acquired == false )
            throw new EJBException("Failed to acquire the pool semaphore, strictTimeout="+strictTimeout);
      }

      synchronized (pool)
      {
         if (!pool.isEmpty())
         {
            mReadyBean.remove();
            return (EnterpriseContext) pool.removeFirst();
         }
      }

      // Pool is empty, create an instance
      try
      {
         return create(container.createBeanClassInstance());
      }
      catch (InstantiationException e)
      {
         throw new EJBException("Could not instantiate bean", e);
      }
      catch (IllegalAccessException e)
      {
         throw new EJBException("Could not instantiate bean", e);
      }
   }

   /**
    *   Return an instance after invocation.
    *
    *   Called in 2 cases:
    *   a) Done with finder method
    *   b) Just removed
    *
    * @param   ctx
    */
   public void free(EnterpriseContext ctx)
   {
      if( log.isTraceEnabled() )
      {
         String msg = pool.size() + "/" + maxSize+" Free instance:"+this
            +"#"+ctx.getId()
            +"#"+ctx.getTransaction()
            +"#"+reclaim
            +"#"+getContainer().getBeanClass();
         log.trace(msg);
      }

      ctx.clear();

      try
      {
         mReadyBean.add();
         if (this.reclaim)
         {
            // Add the unused context back into the pool
            synchronized (pool)
            {
               if (pool.size() < maxSize)
               {
                  pool.addFirst(ctx);
               } // end of if ()
            }
            // If we block when maxSize instances are in use, invoke release on strictMaxSize
            if( strictMaxSize != null )
               strictMaxSize.release();
         }
         else
         {
            // Discard the context
            discard (ctx);
         }
      }
      catch (Exception ignored)
      {
      }
   }

   public int getMaxSize()
   {
      return this.maxSize;
   }

   public void discard(EnterpriseContext ctx)
   {
      if( log.isTraceEnabled() )
      {
         String msg = "Discard instance:"+this+"#"+ctx
            +"#"+ctx.getTransaction()
            +"#"+reclaim
            +"#"+getContainer().getBeanClass();
         log.trace(msg);
      }

      // If we block when maxSize instances are in use, invoke release on strictMaxSize
      if( strictMaxSize != null )
         strictMaxSize.release();

      // Throw away, unsetContext()
      try
      {
         mDestroy.add();
         ctx.discard();
      }
      catch (RemoteException e)
      {
         if( log.isTraceEnabled() )
            log.trace("Ctx.discard error", e);
      }
   }

   public int getCurrentSize()
   {
      synchronized (pool)
      {
         return this.pool.size();
      }
   }


   /**
    * XmlLoadable implementation
    */
   public void importXml(Element element) throws DeploymentException
   {
      String maximumSize = MetaData.getElementContent(MetaData.getUniqueChild(element, "MaximumSize"));
      try
      {
         this.maxSize = Integer.parseInt(maximumSize);
      }
      catch (NumberFormatException e)
      {
         throw new DeploymentException("Invalid MaximumSize value for instance pool configuration");
      }

      // Get whether the pool will block when MaximumSize instances are active
      String strictValue = MetaData.getElementContent(MetaData.getOptionalChild(element, "strictMaximumSize"));
      Boolean strictFlag = Boolean.valueOf(strictValue);
      if( strictFlag == Boolean.TRUE )
         this.strictMaxSize = new FIFOSemaphore(this.maxSize);
      String delay = MetaData.getElementContent(MetaData.getOptionalChild(element, "strictTimeout"));
      try
      {
         if( delay != null )
            this.strictTimeout = Integer.parseInt(delay);
      }
      catch (NumberFormatException e)
      {
         throw new DeploymentException("Invalid strictTimeout value for instance pool configuration");
      }
   }

   public Map retrieveStatistic()
   {
      Map lStatistics = new HashMap();
      lStatistics.put( "InstantiationCount", mInstantiate );
      lStatistics.put( "DestroyCount", mDestroy );
      lStatistics.put( "ReadyBeanCount", mReadyBean );
      return lStatistics;
   }

   public void resetStatistic()
   {
      mInstantiate.reset();
      mDestroy.reset();
      mReadyBean.reset();
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------
   protected abstract EnterpriseContext create(Object instance)
   throws Exception;

   // Private -------------------------------------------------------

   /**
    * At undeployment we want to free completely the pool.
    */
   private void freeAll()
   {
      LinkedList clone = (LinkedList)pool.clone();
      for (Iterator i = clone.iterator(); i.hasNext(); )
      {
         EnterpriseContext ec = (EnterpriseContext)i.next();
         // Clear TX so that still TX entity pools get killed as well
         ec.clear();
         discard(ec);
      }
      pool.clear();
   }

   // Inner classes -------------------------------------------------

}
