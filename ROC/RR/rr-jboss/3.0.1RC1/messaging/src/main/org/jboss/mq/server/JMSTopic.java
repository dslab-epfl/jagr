/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mq.server;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;

import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.SpyTopic;
import org.jboss.mq.Subscription;
import org.jboss.mq.pm.Tx;
import org.jboss.mq.selectors.Selector;

/**
 *  This class is a message queue which is stored (hashed by Destination) on the
 *  JMS provider
 *
 * @author     Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @author     David Maplesden (David.Maplesden@orion.co.nz)
 * @created    August 16, 2001
 * @version    $Revision: 1.1.1.1 $
 */
public class JMSTopic extends JMSDestination {

   //Hashmap of ExclusiveQueues
   HashMap durQueues = new HashMap();
   HashMap tempQueues = new HashMap();

   // Constructor ---------------------------------------------------
   public JMSTopic(SpyDestination dest, ClientConsumer temporary, JMSDestinationManager server) throws JMSException {
      super(dest, temporary, server);
   }

   public void clientConsumerStopped(ClientConsumer clientConsumer) {
      synchronized (durQueues) {
         Iterator iter = durQueues.values().iterator();
         while (iter.hasNext()) {
            ((BasicQueue) iter.next()).clientConsumerStopped(clientConsumer);
         }
      }
      synchronized (tempQueues) {
         Iterator iter = tempQueues.values().iterator();
         while (iter.hasNext()) {
            ((BasicQueue) iter.next()).clientConsumerStopped(clientConsumer);
         }
      }
   }

   public void addSubscriber(Subscription sub) throws JMSException {
      SpyTopic topic = (SpyTopic) sub.destination;
      DurableSubscriptionID id = topic.getDurableSubscriptionID();
      if (id == null) {
         BasicQueue q = new BasicQueue(server, destination.toString()+"."+sub.connectionToken.getClientID(), sub );
         synchronized (tempQueues) {
            tempQueues.put(sub, q);
         }
      } else {
         PersistentQueue q = null;
         synchronized (durQueues) {
            q = (PersistentQueue) durQueues.get(id);
         }
         if (q == null || //Brand new durable subscriber
      !q.destination.equals(topic)) {
            //subscription changed to new topic
            server.getStateManager().setDurableSubscription(server, id, topic);
         }
      }
   }

   public void removeSubscriber(Subscription sub) throws JMSException {
      BasicQueue queue = null;
      SpyTopic topic = (SpyTopic) sub.destination;
      DurableSubscriptionID id = topic.getDurableSubscriptionID();
      if (id == null) {
         synchronized (tempQueues) {
            queue = (BasicQueue) tempQueues.get(sub);
         }
      } else {
         synchronized (durQueues) {
            queue = (BasicQueue) durQueues.get(id);
            //note DON'T remove
         }
      }
      // The queue may be null if the durable subscription
      // is destroyed before the consumer is unsubscribed!
      if (queue == null) {
         (( ClientConsumer )sub.clientConsumer ).removeRemovedSubscription( sub.subscriptionId );
      } else {
         queue.removeSubscriber(sub);
      }
   }

   void cleanupSubscription(Subscription sub) {
      //just try to remove from tempQueues, don't worry if its not there
      synchronized (tempQueues) {
         tempQueues.remove(sub);
      }
   }

   public void addReceiver(Subscription sub) {
      getQueue(sub).addReceiver(sub);
   }

   public void removeReceiver(Subscription sub) {
      getQueue(sub).removeReceiver(sub);
   }
   public void restoreMessage(MessageReference messageRef) {
      try {
         SpyMessage spyMessage = messageRef.getMessage();
         synchronized (this) {
            messageIdCounter = Math.max(messageIdCounter, spyMessage.header.messageId + 1);
         }
         if (spyMessage.header.durableSubscriberID == null) {
            cat.debug("Trying to restore message with null durableSubscriberID");
         } else {
            ((BasicQueue) durQueues.get(spyMessage.header.durableSubscriberID)).restoreMessage(messageRef);
         }
      } catch (JMSException e) {
         cat.error("Could not restore message:", e);
      }
   }
   
   //called by state manager when a durable sub is created
   public void createDurableSubscription(DurableSubscriptionID id) throws JMSException {
      if (temporaryDestination != null) {
         throw new JMSException("Not a valid operation on a temporary topic");
      }

      SpyTopic dstopic = new SpyTopic((SpyTopic) destination, id);

      // Create a 
      BasicQueue queue;
      if( id.getSelector() == null ) {
      	queue = new PersistentQueue(server, dstopic);
      } else {
         // This guy drops messages if his selector does not match.
         class SelectorPersistentQueue extends PersistentQueue {
            Selector selector;
         	  SelectorPersistentQueue(JMSDestinationManager server, SpyTopic dstopic, String selector) throws JMSException {
         	     super( server, dstopic);
         	     this.selector = new Selector(selector);
         	  }
      	  
            public void addMessage( MessageReference mesRef, Tx txId ) throws JMSException {
                  if( selector.test(mesRef.getHeaders()) ) {
                     super.addMessage( mesRef, txId );
                  }
             }
         }
         queue = new SelectorPersistentQueue(server, dstopic, id.getSelector());
      }
      
      synchronized (durQueues) {
         durQueues.put(id, queue);
      }
      server.getPersistenceManager().restoreQueue(this, dstopic);
   }
   
   //called by JMSServer when a destination is being closed.
   public void close() throws JMSException {
      if (temporaryDestination != null) {
         throw new JMSException("Not a valid operation on a temporary topic");
      }

      synchronized (durQueues) {
         Iterator i = durQueues.values().iterator();
         while( i.hasNext() ) {
            PersistentQueue queue = (PersistentQueue)i.next();
            server.getPersistenceManager().closeQueue(this, queue.getSpyDestination());
         }
      }
   }
   

   //called by state manager when a durable sub is deleted
   public void destroyDurableSubscription(DurableSubscriptionID id) throws JMSException {
      BasicQueue queue;
      synchronized (durQueues) {
         queue = (BasicQueue) durQueues.remove(id);
      }
      queue.removeAllMessages();
   }

   public SpyMessage receive(Subscription sub, boolean wait) throws javax.jms.JMSException {
      return getQueue(sub).receive(sub, wait);
   }

   public void acknowledge(org.jboss.mq.AcknowledgementRequest req, Subscription sub, org.jboss.mq.pm.Tx txId) throws JMSException {
      getQueue(sub).acknowledge(req, txId);
   }

   public void addMessage(SpyMessage message, org.jboss.mq.pm.Tx txId) throws JMSException {

      //		if( message.getJMSDeliveryMode() == DeliveryMode.PERSISTENT &&
      //			temporaryDestination!=null ) {
      //			throw new JMSException("Cannot write a persistent message to a temporary destination!");
      //		}

      //Number the message so that we can preserve order of delivery.
      long messageId = 0;
      synchronized (this) {
         messageId = messageIdCounter++;
         synchronized (durQueues) {
            Iterator iter = durQueues.keySet().iterator();
            while (iter.hasNext()) {
               DurableSubscriptionID id = (DurableSubscriptionID) iter.next();
               PersistentQueue q = (PersistentQueue) durQueues.get(id);
               SpyMessage clone = message.myClone();
               clone.header.durableSubscriberID = id;
               clone.header.messageId = messageId;
               MessageReference ref = server.getMessageCache().add(clone);
               q.addMessage(ref, txId);
            }
         }
         synchronized (tempQueues) {
            Iterator iter = tempQueues.values().iterator();
            while (iter.hasNext()) {
               BasicQueue q = (BasicQueue) iter.next();
               SpyMessage clone = message.myClone();
               clone.header.messageId = messageId;
               MessageReference ref = server.getMessageCache().add(clone);
               q.addMessage(ref, txId);
            }
         }
      }
   }

   public ArrayList getPersistentQueues()
   {
      return new ArrayList(durQueues.values());
   }

   // Package protected ---------------------------------------------
   PersistentQueue getDurableSubscription(DurableSubscriptionID id) {
      synchronized (durQueues) {
         return (PersistentQueue) durQueues.get(id);
      }
   }

   private BasicQueue getQueue(Subscription sub) {
      SpyTopic topic = (SpyTopic) sub.destination;
      DurableSubscriptionID id = topic.getDurableSubscriptionID();
      if (id != null) {
         return getDurableSubscription(id);
      } else {
         synchronized (tempQueues) {
            return (BasicQueue) tempQueues.get(sub);
         }
      }
   }
   
   /*
    * @see JMSDestination#isInUse()
    */
   public boolean isInUse()
   {
      if (tempQueues.size() > 0)
         return true;
      Iterator iter = durQueues.values().iterator();
      while (iter.hasNext())
      {
         PersistentQueue q = (PersistentQueue) iter.next();
         if (q.isInUse())
            return true;
      }
      return false;
   }
   /**
    * @see JMSDestination#destroy()
    */
   public void removeAllMessages() throws JMSException
   {
      synchronized (durQueues) {
         Iterator i = durQueues.values().iterator();
         while( i.hasNext() ) {
            PersistentQueue queue = (PersistentQueue)i.next();
            queue.removeAllMessages();
         }
      }
   }

}
