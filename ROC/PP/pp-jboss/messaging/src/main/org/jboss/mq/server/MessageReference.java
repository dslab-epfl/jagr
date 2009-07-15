/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mq.server;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import javax.jms.JMSException;

import org.jboss.logging.Logger;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.SpyMessage.Header;

/**
 * This class holds a reference to an actual Message.  Where it is actually
 * at may vary.  The reference it holds may be a:
 * <ul>
 * <li>Hard Reference - The message is consider recently used and should not be paged out
 * <li>Soft Reference - The message is consider old and CAN be removed from memory by the GC
 * <li>No Reference - The message was removed from memory by the GC, but we can load it from a file.
 * </ul>
 *
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:pra@tim.se">Peter Antman</a>
 * @version    $Revision: 1.1.1.1 $
 */
public class MessageReference implements Comparable
{
   static Logger log = Logger.getLogger(MessageReference.class);

   static class MessageSoftReference extends SoftReference
   {
      MessageReference parent;
      public MessageSoftReference(MessageReference parent, Object message, ReferenceQueue rq)
      {
         super(message, rq);
         this.parent = parent;
      }
   }
   
   public long referenceId;
   public MessageCache messageCache;
   public SpyMessage hardReference;
   public MessageSoftReference softReference;
   public boolean isStored;
   
   // These fields are copied over from the messae itself..
   // they are used too often to not have them handy.
   public byte jmsPriority;
   public long messageId;
   // This object could be used by the PM to associate some info
   public transient Object persistData = null;
   
   MessageReference()
   {
   }
   
   //init and reset methods for use by object pool
   void init(MessageCache messageCache, long referenceId, SpyMessage message)
   {
      this.messageCache = messageCache;
      this.hardReference = message;
      this.referenceId = referenceId;
      this.jmsPriority = (byte) message.getJMSPriority();
      this.messageId = message.header.messageId;
      this.isStored = false;
   }
   
   void reset()
   {
      //clear refs so gc can collect unused objects
      this.messageCache = null;
      this.hardReference = null;
      
      this.softReference = null;
      this.persistData = null;
   }
   
   public SpyMessage getMessage() throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("getMessage lock aquire");

      synchronized (messageCache)
      {
         if (hardReference == null)
         {
            makeHard();
            messageCache.messageReferenceUsedEvent(this, false);
         }
         else
         {
            messageCache.cacheHits++;
            messageCache.messageReferenceUsedEvent(this, true);
         }
         if( trace )
            log.trace("getMessage lock released");
         return hardReference;
      }
   }

   /**
    * We could optimize caching by keeping the headers but not the body.
    * The server will uses the headers more often than the body and the
    * headers take up much message memory than the body
    *
    * For now just return the message.
    */
   public SpyMessage.Header getHeaders() throws javax.jms.JMSException
   {
      return getMessage().header;
   }
   
   void clear() throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("clear lock aquire");
      synchronized (messageCache)
      {
         if (isStored)
         {
            messageCache.removeFromStorage(this);
            isStored = false;
         }
         if( trace )
            log.trace("clear lock relased");
      }
   }
    
   public void invalidate() throws JMSException
   {
      clear(); // Clear out the disk copy
   }

   void makeSoft() throws JMSException
   {
      // Called from MessageCache, either throug a reference (resulting in
      // other references beeing called) or by the softner.
      // Basically means that is a locking issue place. We need to sync
      // on the message cache.
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("makeSoft lock aquire");
      // You are only allowed to make something soft if you own a monitor
      // on messageCache
      synchronized (messageCache)
      {
         if (softReference != null)
            return;
         
         if (!isStored)
         {
            messageCache.saveToStorage(this, hardReference);
            isStored = true;
         }
         
         softReference = new MessageSoftReference(this, hardReference, messageCache.referenceQueue);
         
         //I am undecided about whether to release this message back to the message pool or not
         //the whole point of this caching business is to release the memory this message is using up
         //if we are going to do it then this is the place...
         //MessagePool.releaseMessage(hardReference);
         
         // We don't need the hard ref anymore..
         hardReference = null;
         
         messageCache.softRefCacheSize++;
         if( trace )
            log.trace("makeSoft lock released");
      }
   }
   
   public void setStored(Object persistData) throws JMSException
   {
      // Called from A PeristenceManager/CacheStore, 
      // to let us know that this message is allread stored on disk.
      this.persistData = persistData;
      isStored = persistData != null;
      
   }
   
 
   void makeHard() throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("makeHard lock aquire");
      synchronized (messageCache)
      {
         // allready hard
         if (hardReference != null)
            return;
         
         // Get the object via the softref
         hardReference = (SpyMessage) softReference.get();
         
         // It might have been removed from the cache due to memory constraints
         if (hardReference == null)
         {
            // load it from disk.
            hardReference = messageCache.loadFromStorage(this);
            messageCache.cacheMisses++;
         } else
         {
            messageCache.cacheHits++;
            messageCache.softRefCacheSize--;
         }
         
         // Since we have hard ref, we do not need the soft one.
         softReference = null;
         if( trace )
            log.trace("makeHard lock released");
      }
   }
   
   public boolean equals(Object o)
   {
      try
      {
         return referenceId == ((MessageReference) o).referenceId;
      } catch (Throwable e)
      {
         return false;
      }
   }
   
   /**
    * This method allows message to be order on the server queues
    * by priority and the order that they came in on.
    *
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(Object o)
   {
      MessageReference sm = (MessageReference) o;
      if (jmsPriority > sm.jmsPriority)
      {
         return -1;
      }
      if (jmsPriority < sm.jmsPriority)
      {
         return 1;
      }
      return (int) (messageId - sm.messageId);
   }
   
}
