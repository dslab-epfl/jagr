
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.resource.connectionmanager; 

import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import org.jboss.logging.Logger;

/**
 * JBossManagedConnectionPool.java
 *
 *
 * Created: Sun Dec 30 21:17:36 2001
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class InternalManagedConnectionPool 
{

   private final ManagedConnectionFactory mcf;
   private final Subject defaultSubject;
   private final ConnectionRequestInfo defaultCri;
   private final PoolParams poolParams;

   private final LinkedList mcs = new LinkedList();

   private final FIFOSemaphore permits;

   private final Logger log;

   private final Counter connectionCounter = new Counter();

   //used to fill pool after first connection returned.
   private boolean started = false;

   public InternalManagedConnectionPool (ManagedConnectionFactory mcf, Subject subject, ConnectionRequestInfo cri, PoolParams poolParams, Logger log)
   {
      this.mcf = mcf;
      defaultSubject = subject;
      defaultCri = cri;
      this.poolParams = poolParams;
      this.log = log;
      permits = new FIFOSemaphore(this.poolParams.maxSize);
      IdleRemover.registerPool(this, poolParams.idleTimeout);
   }

   public long getAvailableConnections()
   {
      return permits.permits();
   }

   public ManagedConnection getConnection(Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      subject = (subject == null)? defaultSubject: subject;
      cri = (cri == null)? defaultCri: cri;
      try 
      {
         if (permits.attempt(poolParams.blockingTimeout)) 
         {
            //We have a permit to get a connection. Is there one in the pool already?
            ManagedConnection mc = null;
            synchronized (mcs)
            {
               if (mcs.size() > 0) 
               {
                  mc = ((MCHolder)mcs.removeFirst()).getMC();
               } // end of if ()
            }
            try 
            {
               if (mc != null) 
               {
                  //Yes, we retrieved a ManagedConnection from the pool. Does it match?
                  mc = mcf.matchManagedConnections(new SetOfOne(mc), subject, cri);
                  if (mc == null) 
                  {
                     //match didn't work!
                     throw new ResourceException("Error in use of ManagedConnectionPool: matchManagedConnection failed with subject: "+ subject + " and ConnectionRequestInfo: " + cri);
                  } // end of if ()
                  log.trace("returning ManagedConnection from pool"); 
                  return mc;
               } // end of if ()
               else 
               {
                  //No, the pool was empty, so we have to make a new one.
                  mc = createConnection(subject, cri);
                  //lack of synch on "started" probably ok, if 2 reads occur we will just
                  //run fillPool twice, no harm done.
                  if (!started) 
                  {
                     started = true;
                     PoolFiller.fillPool(this);
                  } // end of if ()
                  log.trace("returning new ManagedConnection"); 
                  return mc;
               } // end of else
            } catch (ResourceException re) 
            {
               //return permit and rethrow
               permits.release();
               throw re;
            } // end of try-catch
         } // end of if ()
         else 
         {
            //we timed out
            throw new ResourceException("No ManagedConnections Available!");        
         } // end of else
      
      } catch (InterruptedException ie) 
      {
         throw new ResourceException("Interrupted while requesting permit!");
      } // end of try-catch
      
   }

   public void returnConnection(ManagedConnection mc, boolean kill)
   {
      log.trace("putting ManagedConnection back into pool");
      boolean wasInPool = false;
      try 
      {
         mc.cleanup();
         if (kill) 
         {
            synchronized (mcs)
            {
               for (Iterator i = mcs.iterator(); i.hasNext(); )
               {
                  MCHolder mch = (MCHolder)i.next();
                  if (mch.getMC() == mc) 
                  {
                     i.remove();
                     wasInPool = true;
                     break;
                  } // end of if ()
               } // end of for ()
            }

            doDestroy(mc);
         } // end of if ()
         else 
         {
            synchronized (mcs)
            {
               mcs.addLast(new MCHolder(mc));
            }
         } // end of else
      } catch (ResourceException re) 
      {
         log.info("ResourceException returning ManagedConnection to pool:", re);
      } finally 
      {
         if (!wasInPool) 
         {
            permits.release();
         } // end of if ()
      } // end of try-catch
   }

   public void removeTimedOut()
   {
      synchronized (mcs)
      {
         for (Iterator i = mcs.iterator(); i.hasNext(); ) 
         {
            MCHolder mch = (MCHolder)i.next();
            if (mch.isTimedOut()) 
            {
               i.remove();
               doDestroy(mch.getMC());
            } // end of if ()
            else
            {
               //They were put in chronologically, so if one isn't timed out, following ones won't be either.
               break;               
            } // end of else
         } // end of for ()
      }
      //refill if necessary, asynchronously.
      PoolFiller.fillPool(this);
   }

   public void shutdown()
   {
      synchronized (mcs)
      {
         for (Iterator i = mcs.iterator(); i.hasNext(); ) 
         {
            ManagedConnection mc = ((MCHolder)i.next()).getMC();
            i.remove();
            doDestroy(mc);
         } // end of for ()
      }
      IdleRemover.unregisterPool(this);
   }

   public void fillToMin()
   {
      ArrayList newMCs = new ArrayList();
      try 
      {
         while (connectionCounter.getCount() < poolParams.minSize)
         {
            newMCs.add(getConnection(defaultSubject, defaultCri));
         } // end of while ()
      }
      catch (ResourceException re)
      {
         //Whatever the reason, stop trying to add more!
      } // end of try-catch
      for (Iterator i = newMCs.iterator(); i.hasNext(); )
      {
         returnConnection((ManagedConnection)i.next(), false);
      } // end of for ()
      
   }

   public int getConnectionCount()
   {
      return connectionCounter.getCount();
   }

   public int getConnectionCreatedCount()
   {
      return connectionCounter.getCreatedCount();
   }

   public int getConnectionDestroyedCount()
   {
      return connectionCounter.getDestroyedCount();
   }

   private ManagedConnection createConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      try 
      {
         connectionCounter.inc();
         return mcf.createManagedConnection(subject, cri);         
      }
      catch (ResourceException re)
      {
         connectionCounter.dec();
         throw re;
      } // end of try-catch
   }  

   private void doDestroy(ManagedConnection mc)
   {   
      connectionCounter.dec();
      try 
      {
         mc.destroy();
      }
      catch (ResourceException re)
      {
         log.info("Exception destroying ManagedConnection", re);
      } // end of try-catch
   }

   public static class PoolParams
   {
      public int minSize = 0;
      public int maxSize = 10;
      public int blockingTimeout = 5000;//milliseconds
      public long idleTimeout = 1000*60*30;//milliseconds, 30 minutes.
   }


   private class MCHolder
   {
      private final ManagedConnection mc;
      private final long age;

      MCHolder(final ManagedConnection mc)
      {
         this.mc = mc;
         this.age = System.currentTimeMillis();
      }

      ManagedConnection getMC()
      {
         return mc;
      }

      boolean isTimedOut()
      {
         return System.currentTimeMillis() - age > poolParams.idleTimeout;
      }
   }

   private static class Counter
   {
      private int created = 0;
      private int destroyed = 0;

      synchronized int getCount() 
      {
         return created - destroyed;
      }

      synchronized int getCreatedCount() 
      {
         return created;
      }

      synchronized int getDestroyedCount() 
      {
         return created;
      }

      synchronized void inc()
      {
         created++;
      }

      synchronized void dec()
      {
         destroyed++;
      }
   }

   public static class SetOfOne  implements Set
   {
      private final Object object;

      public SetOfOne(Object object)
      {
         if (object == null) 
         {
            throw new IllegalArgumentException("SetOfOne must contain a non-null object!");
         } // end of if ()
         
         this.object = object;
      }
      // implementation of java.util.Set interface

      /**
       *
       * @return <description>
       */
      public int hashCode() {
         return object.hashCode();
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean equals(Object other) {
         if (other instanceof SetOfOne) 
         {
            return this.object == ((SetOfOne)other).object;
         } // end of if ()
         
         return false;
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean add(Object param1) {
         throw new UnsupportedOperationException("can't add to SetOfOne");
      }

      /**
       *
       * @return <description>
       */
      public int size() {
         return 1;
      }

      /**
       *
       * @return <description>
       */
      public Object[] toArray() {
         return new Object[] {object};
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public Object[] toArray(Object[] array) {
         if (array.length < 1) 
         {
            array = (Object[])java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), 1); 
         } // end of if ()
         array[0] = object;
         return array;
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean contains(Object object) {
         return this.object.equals(object);
      }

      /**
       *
       */
      public void clear() {
         throw new UnsupportedOperationException("can't clear SetOfOne");
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean remove(Object param1) {
         throw new UnsupportedOperationException("can't remove from SetOfOne");
      }

      /**
       *
       * @return <description>
       */
      public boolean isEmpty() {
         return false;
      }

      /**
       *
       * @return <description>
       */
      public Iterator iterator() {
         return new Iterator() {
               boolean done = false;

               public boolean hasNext()
               {
                  return !done;
               }

               public Object next()
               {
                  if (done) 
                  {
                     throw new NoSuchElementException();
                  } // end of if ()
                  done = true;
                  return object;
               }

               public void remove()
               {
                  throw new UnsupportedOperationException();
               }
                  
            };
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean containsAll(Collection col)
      {
         if (col == null || col.size() != 1 )
         {
            return false;
         } // end of if ()
         
         return object.equals(col.iterator().next());
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean addAll(Collection param1) {
         throw new UnsupportedOperationException();
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean removeAll(Collection param1) {
         throw new UnsupportedOperationException();
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       */
      public boolean retainAll(Collection param1) {
         throw new UnsupportedOperationException();
      }

   }
   
}// ManagedConnectionPool
