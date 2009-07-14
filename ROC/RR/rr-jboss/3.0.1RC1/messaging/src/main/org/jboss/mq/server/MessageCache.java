/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mq.server;

import java.io.File;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.mq.SpyMessage;
import javax.jms.JMSException;
import org.jboss.mq.pm.CacheStore;

/**
 * This class implements a Message cache so that larger amounts of messages
 * can be processed without running out of memory.  When memory starts getting tight
 * it starts moving messages out of memory and into a file so that they can be recovered
 * later.
 *
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:David.Maplesden@orion.co.nz">David Maplesden</a>
 * @author <a href="mailto:pra@tim.se">Peter Antman</a>
 * @version    $Revision: 1.1.1.1 $
 */
public class MessageCache extends ServiceMBeanSupport implements MessageCacheMBean, MBeanRegistration, Runnable
{
   // The cached messages are orded in a LRU linked list
   private LRUCache lruCache = new LRUCache();
  
   // Provides a Unique ID to MessageHanles
   private long messageCounter = 0;
   int cacheHits = 0;
   int cacheMisses = 0;

   CacheStore cacheStore;
   ObjectName cacheStoreObjectName;
        
   private Thread referenceSoftner;

   private long highMemoryMark = 1024L * 1000 * 16;
   private long maxMemoryMark = 1024L * 1000 * 32;
   public static final long ONE_MEGABYTE = 1024L * 1000;

   int softRefCacheSize = 0;
   int totalCacheSize = 0;

   // Used to get notified when message are being deleted by GC
   ReferenceQueue referenceQueue = new ReferenceQueue();


   public MessageCache getInstance()
   {
      return this;
   }

   /**
    * Adds a message to the cache
    */
   public MessageReference add(SpyMessage message) throws javax.jms.JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("add lock aquire");
      
      MessageReference mh = null;
      synchronized (this)
      {
         mh = new MessageReference();
         mh.init(this, messageCounter++, message);
         lruCache.addMostRecent(mh);
         totalCacheSize++;
         validateSoftReferenceDepth();
  
         if(trace)
            log.trace("add lock release");

      }

      return mh;
   }

   /**
    * removes a message from the cache
    */
   public void remove(MessageReference mr) throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("remove lock aquire");

      synchronized (this)
      {
         mr.clear();//Will remove it from storage if soft
         if (mr.hardReference != null)//If message is not hard, dont do lru stuff
            lruCache.remove(mr);
         totalCacheSize--;
  
         if (trace)
            log.trace("remove lock release");
      }
   }

   /**
    * The strategy is that we keep the most recently used messages as
    * Hard references.  Then we make the older ones soft references.  Making
    * something a soft reference stores it to disk so we need to avoid making
    * soft references if we can avoid it.  But once it is made a soft reference does
    * not mean that it is removed from memory.  Depending on how agressive the JVM's
    * GC is, it may stay around long enough for it to be used by a client doing a read,
    * saving us read from the file system.  If memory gets tight the GC will remove
    * the soft references.  What we want to do is make sure there are at least some
    * soft references available so that the GC can reclaim memory.
    * @see Runnable#run()
    */
   public void run()
   {
      try
      {
         while (true)
         {
            // 
            //log.trace("Waiting for a reference to get GCed.");
            // Get the next soft reference that was canned by the GC
            Reference r = referenceQueue.remove(1000);
            if (r != null)
            {
               softRefCacheSize--;
               // the GC will free a set of messages together, so we poll them
               // all before we validate the soft reference depth.
               while ((r = referenceQueue.poll()) != null)
               {
                  softRefCacheSize--;
               }
               if( log.isTraceEnabled() )
                  log.trace("soft reference cache size is now: "+softRefCacheSize);
                  
               //log.trace("Validating soft reference count.");
               validateSoftReferenceDepth();
            }
         }
      } catch (JMSException e)
      {
         log.error("Message Cache Thread Stopped: ", e);
      } catch (InterruptedException e)
      {
         // Signal to exit the thread.
      }
      log.debug("Thread exiting.");
   }

   /**
    * This method is in charge of determining if it time to convert some
    * hard references over to soft references.
    *
    * It must NOT be called by a thread holding a lock on a reference wich
    * in its turn holds a lock on cache. It WILL lead to deadlocks!!!
    */
   public void validateSoftReferenceDepth() throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("run lock aquire");
      
      synchronized (this)
      {

         // howmany to change over to soft refs
         int chnageCount = 0;

         long currentMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
         if (currentMem > highMemoryMark)
         {
            // we need to get more aggresive... how much?? lets get
            // a mesurment from 0 to 1
            float severity = ((float) (currentMem - highMemoryMark)) / (maxMemoryMark - highMemoryMark);
            severity = Math.min(severity, 1.0F);
            if( log.isTraceEnabled() )
               log.trace("Memory usage serverity=" + severity);
            int totoalMessageInMem = getHardRefCacheSize() + getSoftRefCacheSize();
            int howManyShouldBeSoft = (int) ((totoalMessageInMem) * severity);
            chnageCount = howManyShouldBeSoft - getSoftRefCacheSize();
         }

         // Ignore change counts of 1 since this will happen too often even
         // if the serverity is low since it will round up.
         if (chnageCount > 1)
         {
            if (log.isTraceEnabled())
               log.trace("Converting " + chnageCount + " hard ref to to soft refs");
            Node leastRecent = lruCache.getLeastRecent();
            for (int i = 0; i < chnageCount && leastRecent != null; i++)
            {
               // This is tricky, make soft should really be done outside
               // sync on cache
               MessageReference mr = (MessageReference) leastRecent.data;
               mr.makeSoft();
               lruCache.remove(mr);
               leastRecent = lruCache.getLeastRecent();
            }
         }
         if (trace)
            log.trace("run lock release");
      }
   }

   /**
    * This gets called when a MessageReference is de-referenced.
    * We will pop it to the top of the RLU
    */
   void messageReferenceUsedEvent(MessageReference mh, boolean wasHard)
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("messageReferenceUsedEvent lock aquire");
      
      synchronized (this)
      {
         if (wasHard)
            lruCache.makeMostRecent(mh);
         else
            lruCache.addMostRecent(mh);

         if (trace)
            log.trace("messageReferenceUsedEvent lock released");
      }
   }

   //////////////////////////////////////////////////////////////////////////////////
   // Perisitence methods used by the MessageReference.
   //////////////////////////////////////////////////////////////////////////////////
   SpyMessage loadFromStorage(MessageReference mh) throws JMSException
   {
      return (SpyMessage)cacheStore.loadFromStorage(mh);
   }

   void saveToStorage(MessageReference mh, SpyMessage message) throws JMSException
   {
      cacheStore.saveToStorage(mh, message);
   }

   void removeFromStorage(MessageReference mh) throws JMSException
   {
      cacheStore.removeFromStorage(mh);
   }

   //////////////////////////////////////////////////////////////////////////////////
   //
   // The following section deals the the JMX interface to manage the Cache
   //
   //////////////////////////////////////////////////////////////////////////////////


   /**
    * This gets called to start the cache service. Synch. by start
    */
   protected void startService() throws Exception
   {

      cacheStore = (CacheStore)getServer().getAttribute(cacheStoreObjectName, "Instance");
      
      if (getState() == ServiceMBeanSupport.STARTED)
         throw new Exception("Cannot be initialized from the current state");

      referenceSoftner = new Thread(this, "JBossMQ Cache Reference Softner");
      referenceSoftner.setDaemon(true);
      referenceSoftner.start();
   }

   /**
    * This gets called to stop the cache service.
    */
   synchronized public void stopService()
   {
      if (!(getState() == ServiceMBeanSupport.STARTED))
         return;

      referenceSoftner.interrupt();
      referenceSoftner = null;
   }


   /**
    * Gets the hardRefCacheSize
    * @return Returns a int
    */
   public int getHardRefCacheSize()
   {
      synchronized(this)
      {
         return lruCache.size();
      }
   }

   /**
    * Gets the softRefCacheSize
    * @return Returns a int
    */
   public int getSoftRefCacheSize()
   {
      return softRefCacheSize;
   }

   /**
    * Gets the totalCacheSize
    * @return Returns a int
    */
   public int getTotalCacheSize()
   {
      return totalCacheSize;
   }

   /**
    * Gets the cacheMisses
    * @return Returns a int
    */
   public int getCacheMisses()
   {
      return cacheMisses;
   }

   /**
    * Gets the cacheHits
    * @return Returns a int
    */
   public int getCacheHits()
   {
      return cacheHits;
   }

   /**
    * Gets the highMemoryMark
    * @return Returns a long
    */
   public long getHighMemoryMark()
   {
      return highMemoryMark / ONE_MEGABYTE;
   }
   /**
    * Sets the highMemoryMark
    * @param highMemoryMark The highMemoryMark to set
    */
   public void setHighMemoryMark(long highMemoryMark)
   {
      this.highMemoryMark = highMemoryMark * ONE_MEGABYTE;
   }

   /**
    * Gets the maxMemoryMark
    * @return Returns a long
    */
   public long getMaxMemoryMark()
   {
      return maxMemoryMark / ONE_MEGABYTE;
   }

   /**
    * Gets the CurrentMemoryUsage
    * @return Returns a long
    */
   public long getCurrentMemoryUsage()
   {
      return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / ONE_MEGABYTE;
   }

   /**
    * Sets the maxMemoryMark
    * @param maxMemoryMark The maxMemoryMark to set
    */
   public void setMaxMemoryMark(long maxMemoryMark)
   {
      this.maxMemoryMark = maxMemoryMark * ONE_MEGABYTE;
   }

   /**
    * @see ServiceMBeanSupport#getName()
    */
   public String getName()
   {
      return "MessageCache";
   }

   /**
    * TODO: Update so that it sets a CacheStore
    * 
    * This test creates 5000 x 100K messages and places them
    * in the MessageCache.  With out a cache this would be
    * 500 Megs of memory needed.  The cache will allow us to
    * stay withing 64 Megs of RAM.
    */
   public void testBigLoad() throws Exception
   {

      MessageCache cache = new MessageCache();
      File tempDir = new File("Temp-" + System.currentTimeMillis());
      tempDir.mkdirs();

      //cache.setDataDirectory(tempDir.getCanonicalPath());
      cache.setHighMemoryMark(40);
      cache.setMaxMemoryMark(60);
      cache.start();

      LinkedList ll = new LinkedList();

      int TEST_SIZE = 5000;
      // Create a whole bunch of messages.
      java.util.Random rand = new java.util.Random(System.currentTimeMillis());
      log.info("Adding the messages");
      for (int i = 0; i < TEST_SIZE; i++)
      {
         //log.info("Used Mem="+(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()));
         org.jboss.mq.SpyBytesMessage bm = new org.jboss.mq.SpyBytesMessage();
         bm.writeBytes(new byte[1024 * 100]); // 100K messages
         MessageReference mr = cache.add(bm);
         ll.add(mr);

         // Randomly pickout messages out of the cache..
         int pick = rand.nextInt(i + 1);
         mr = (MessageReference) ll.get(pick);
         mr.getMessage();
      }

      log.info("Used Mem=" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
      //assertTrue("getTotalCacheSize check", cache.getTotalCacheSize() == TEST_SIZE);
      log.info("Messages with Hard Refs=" + cache.getHardRefCacheSize());
      log.info("Messages with Soft Refs=" + cache.getSoftRefCacheSize());

      log.info("Removing the messages");
      Iterator iter = ll.iterator();
      for (int i = 0; i < TEST_SIZE; i++)
      {
         MessageReference mr = (MessageReference) iter.next();
         iter.remove();
         cache.remove(mr);
      }

      log.info("Stopping");
      //assertTrue("getTotalCacheSize check", cache.getTotalCacheSize() == 0);
      cache.stop();
      //assertTrue("Data directory clean up check", tempDir.listFiles().length == 0);
      tempDir.delete();

      log.info("Cache Hits=" + cache.getCacheHits());
      log.info("Cache Misses=" + cache.getCacheMisses());
   }

   /**
    * @see MessageCacheMBean#setCacheStore(ObjectName)
    */
   public void setCacheStore(ObjectName cacheStore) 
   {
      cacheStoreObjectName = cacheStore;
   }
   
   /**
    * This class implements a simple, efficient LRUCache.  It is pretty much a 
    * cut down version of the code in org.jboss.pool.cache.LeastRecentlyUsedCache
    * 
    * 
    */
   class LRUCache
   {
      int currentSize = 0;
      //maps objects to their nodes
      HashMap map = new HashMap();
      Node mostRecent = null;
      Node leastRecent = null;
      public void addMostRecent(Object o)
      {
         Node newNode = new Node();
         newNode.data = o;
         //insert into map
         Object oldNode = map.put(o,newNode);
         if(oldNode != null)
         {
            map.put(o,oldNode);
            throw new RuntimeException("Can't add object '"+o+"' to LRUCache that is already in cache.");
         }
         //insert into linked list
         if(mostRecent == null)
         {
            //first element
            mostRecent = newNode;
            leastRecent = newNode;
         }
         else
         {
            newNode.lessRecent = mostRecent;
            mostRecent.moreRecent = newNode;
            mostRecent = newNode;
         }
         ++currentSize;
      }
      // Not used anywhere!!
      public void addLeastRecent(Object o)
      {
         Node newNode = new Node();
         newNode.data = o;
         //insert into map
         Object oldNode = map.put(o,newNode);
         if(oldNode != null)
         {
            map.put(o,oldNode);
            throw new RuntimeException("Can't add object '"+o+"' to LRUCache that is already in cache.");
         }
         //insert into linked list
         if(leastRecent == null)
         {
            //first element
            mostRecent = newNode;
            leastRecent = newNode;
         }
         else
         {
            newNode.moreRecent = leastRecent;
            leastRecent.lessRecent = newNode;
            leastRecent = newNode;
         }
         ++currentSize;
      }
      public void remove(Object o)
      {
         //remove from map
         Node node = (Node) map.remove(o);
         if(node == null)
            throw new RuntimeException("Can't remove object '"+o+"' that is not in cache.");
         //remove from linked list
         Node more = node.moreRecent;
         Node less = node.lessRecent;
         if(more == null) {//means node is mostRecent
            mostRecent = less;
            if (mostRecent != null) {
               mostRecent.moreRecent = null;//Mark it as beeing at the top of tree
            }
         } else {
            more.lessRecent = less;
         }
         if(less == null) {//means node is leastRecent
            leastRecent = more;
            if (leastRecent != null) {
               leastRecent.lessRecent = null;//Mark it last in tree
            }
         } else {
            less.moreRecent = more;
         }
         --currentSize;
      }
      public void makeMostRecent(Object o)
      {
         //get node from map
         Node node = (Node) map.get(o);
         if(node == null)
            throw new RuntimeException("Can't make most recent object '"+o+"' that is not in cache.");
         //reposition in linked list, first remove
         Node more = node.moreRecent;
         Node less = node.lessRecent;
         if(more == null) //means node is mostRecent
            return;
         else
            more.lessRecent = less;
         if(less == null) //means node is leastRecent
            leastRecent = more;
         else
            less.moreRecent = more;
         //now add back in at most recent position
         node.lessRecent = mostRecent;
         node.moreRecent = null;//We are at the top
         mostRecent.moreRecent = node;
         mostRecent = node;
      }
      public int size()
      {
         return currentSize;
      }
      public Node getMostRecent()
      {
         return mostRecent;
      }   
      public Node getLeastRecent()
      {
         return leastRecent;
      }
   }

   class Node
   {
      Node moreRecent = null;
      Node lessRecent = null;
      Object data = null;
   }
}
/*
vim:tabstop=3:expandtab:ai
*/
