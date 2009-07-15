/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mq.server;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;

import org.jboss.logging.Logger;

import org.jboss.mq.AcknowledgementRequest;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.SpyQueue;
import org.jboss.mq.Subscription;
import org.jboss.mq.pm.PersistenceManager;
import org.jboss.mq.selectors.Selector;

/**
 *  This class represents a queue which provides it's messages exclusivly to one
 *  consumer at a time.
 *
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @author     Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author     David Maplesden (David.Maplesden@orion.co.nz)
 * @created    August 16, 2001
 * @version    $Revision: 1.1.1.1 $
 */
//abstract public class BasicQueue implements Runnable {
public class BasicQueue {
   //List of messages waiting to be dispatched
   SortedSet messages = new TreeSet();
   //The JMSServer object
   JMSDestinationManager server;
   // The subscribers waiting for messages
   HashSet receivers = new HashSet();
   // The subscription that all messages will goto eventualy, set for a topic's
   // queue to a subscription.
   Subscription     exclusiveSubscription;
   
   //List of messages that should be acked or else returned to thier
   //owning exclusive queues.
   HashMap unacknowledgedMessages = new HashMap();

   HashSet removedSubscribers = new HashSet();

   Logger cat;

   /**
    * Construct a new basic queue
    */
   public BasicQueue(JMSDestinationManager server, String description) throws JMSException
   {
      this.server = server;
      cat = Logger.getLogger( BasicQueue.class.getName()+"."+description);
   }
   
   public int getQueueDepth() {
      return messages.size();
   }

   public void clientConsumerStopped(ClientConsumer clientConsumer) {
      //remove all waiting subs for this clientConsumer and send to its blocked list.
      synchronized (receivers) {
         for (Iterator it = receivers.iterator(); it.hasNext();) {
            Subscription sub = (Subscription) it.next();
            if (sub.clientConsumer.equals(clientConsumer)) {
               clientConsumer.addBlockedSubscription(sub);
               it.remove();
            }
         }
      }
   }

   //Used to put a message that was added previously to the queue, back in the queue
   public void restoreMessage(MessageReference mes) {
      internalAddMessage(mes);
   }

   public SpyMessage[] browse(String selector) throws JMSException {

      if (selector == null) {
         SpyMessage list[];
         synchronized (messages) {
            list = new SpyMessage[messages.size()];
            Iterator iter = messages.iterator();
            for( int i=0; iter.hasNext(); i++ )
            	list[i] = ((MessageReference)iter.next()).getMessage();
         }
         return list;
      } else {
         Selector s = new Selector(selector);
         LinkedList selection = new LinkedList();

         synchronized (messages) {
            Iterator i = messages.iterator();
            while (i.hasNext()) {
               MessageReference m = (MessageReference) i.next();
               if (s.test(m.getHeaders())) {
                  selection.add(m.getMessage());
               }
            }
         }

         SpyMessage list[];
         list = new SpyMessage[selection.size()];
         list = (SpyMessage[]) selection.toArray(list);
         return list;
      }
   }

   public void addReceiver(Subscription sub) {
      synchronized (messages) {
         if (messages.size() != 0) {
            for (Iterator it = messages.iterator(); it.hasNext();) {
               MessageReference message = (MessageReference) it.next();
               try {
                  if (sub.accepts(message.getHeaders())) {
                     //queue message for sending to this sub
                     queueMessageForSending(sub, message);
                     it.remove();
                     return;
                  }
               } catch (JMSException ignore) {
                  cat.info("Caught unusual exception in addToReceivers.", ignore);
               }
            }
         }
      }
      addToReceivers(sub);
   }

   public void nackMessages(Subscription sub)
   {
      // Send nacks for unacknowledged messages
      synchronized (receivers)
      {
         synchronized (messages)
         {
            synchronized (unacknowledgedMessages)
            {
               int count = 0;
               Iterator i = ((HashMap) unacknowledgedMessages.clone()).entrySet().iterator();
               while (i.hasNext())
               {
                  Map.Entry entry = (Map.Entry) i.next();
                  AcknowledgementRequest item = (AcknowledgementRequest) entry.getKey();
                  UnackedMessageInfo unacked = (UnackedMessageInfo) entry.getValue();
                  try
                  {
                     if (unacked.sub == sub)
                     {
                        acknowledge(item, null);
                        count++;
                     }
                  }
                  catch (JMSException ignore)
                  {
                     cat.debug("Unable to nack message: " + item, ignore);
                  }
               }
               if (cat.isDebugEnabled())
                  cat.debug("Nacked " + count + " messages for removed subscription " + sub);
            }
         }
      }
   }

   public void removeReceiver(Subscription sub) {
      synchronized (receivers) {
         receivers.remove(sub);
      }
   }

   public void removeSubscriber(Subscription sub) {
      removeReceiver(sub);
      if (hasUnackedMessages(sub))
      {
         synchronized (removedSubscribers)
         {
            removedSubscribers.add(sub);
         }
      }
      else
      {
         ((ClientConsumer) sub.clientConsumer).removeRemovedSubscription(sub.subscriptionId);
      }
   }
   
   public boolean isInUse() {
      synchronized (receivers) {
         return receivers.size()>0;
      }      
   }

   public SpyMessage receive(Subscription sub, boolean wait) throws JMSException {
      MessageReference messageRef = null;
      synchronized (receivers) {
      	 // If the subscription is not picky, the first message will be it
         if (sub.getSelector() == null && sub.noLocal==false ) {
            synchronized (messages) {
               if (messages.size() != 0) {
               	  messageRef = (MessageReference)messages.first();
                  messages.remove(messageRef);
               }
            }
         } else {
         	// The subscription is picky, so we have to iterate.
            synchronized (messages) {
               Iterator i = messages.iterator();
               while (i.hasNext()) {
                  MessageReference mr = (MessageReference) i.next();
                  if (sub.accepts(mr.getHeaders())) {
                     messageRef = mr;
                     i.remove();
                     break;
                  }
               }
            }
         }

         if (messageRef == null) {
            if (wait) {
               addToReceivers(sub);
            }
         } else {
            setupMessageAcknowledgement(sub, messageRef);
         }
      }
      if(messageRef== null)
      	return null;
      return messageRef.getMessage();
   }

   public void removeAllMessages() throws JMSException {
      synchronized (unacknowledgedMessages) {
         Iterator i = ((HashMap) unacknowledgedMessages.clone()).keySet().iterator();
         while (i.hasNext()) {
            AcknowledgementRequest item = (AcknowledgementRequest) i.next();
            try {
               acknowledge(item, null);
            } catch (JMSException ignore) {
            }
         }
      }
      synchronized (messages) {
         Iterator i = messages.iterator();
         while (i.hasNext()) {
            MessageReference message = (MessageReference) i.next();
            i.remove();
            dropMessage(message);
         }
      }
   }

   public void acknowledge(AcknowledgementRequest item, org.jboss.mq.pm.Tx txId) throws javax.jms.JMSException {

      // This task gets run to place the neg ack a messge (place it back on the queue)
      class RestoreMessageTask implements Runnable {

         MessageReference message;

         RestoreMessageTask(MessageReference m) {
            message = m;
         }

         public void run() {
            if (cat.isTraceEnabled())
               cat.trace("Restoring message: " + message);
            restoreMessage(message);
         }
      }

      class RemoveMessageTask implements Runnable {
         MessageReference message;
         RemoveMessageTask(MessageReference m) {
            message = m;
         }
         public void run() {
         	try {
            	server.getMessageCache().remove(message);
               //we are finally done with message so we can release it back to pool
               org.jboss.mq.MessagePool.releaseMessage(message.hardReference);
         	} catch ( JMSException e ) {
         		cat.error("Could not remove an acknowleged message from the message cache: ", e);
         	}
         }
      }

      UnackedMessageInfo unacked = null;
      synchronized (unacknowledgedMessages) {
         unacked = (UnackedMessageInfo) unacknowledgedMessages.remove(item);
      }

      if (unacked == null)
      {
         return;
      }

      MessageReference m = unacked.messageRef;

      // Was it a negative acknowledge??
      if (!item.isAck) {

         SpyMessage spyMessage = m.getMessage();
         //set redelivered flag
         spyMessage.setJMSRedelivered(true);
         // Lock the message reference since we are updating.
         m.invalidate();

         Runnable task = new RestoreMessageTask(m);
         server.getPersistenceManager().getTxManager().addPostCommitTask(txId, task);

      } else {

         if (this instanceof PersistentQueue && m.getHeaders().jmsDeliveryMode == DeliveryMode.PERSISTENT) {
            server.getPersistenceManager().remove(m, txId);
         }

         Runnable task = new RestoreMessageTask(m);
         server.getPersistenceManager().getTxManager().addPostRollbackTask(txId, task);

         task = new RemoveMessageTask(m);
         server.getPersistenceManager().getTxManager().addPostCommitTask(txId, task);
      }

      // Adrian Brock
      // We have to synchronize on everything.
      // For RMI and topics one thread can be acknowledging
      // when the pumping thread spots the connection has
      // dropped. This call tidies up the queue when
      // all messages have been nacked after a failure.
      // This needs revisiting...
      synchronized (receivers)
      {
         synchronized (messages)
         {
            synchronized (unacknowledgedMessages)
            {
               checkRemovedSubscribers(unacked.sub);
            }
         }
      }
   }

   public void addMessage(MessageReference mes, org.jboss.mq.pm.Tx txId) throws JMSException {
      // This task gets run to make the message visible in the queue.
      class AddMessagePostCommitTask implements Runnable {

         MessageReference message;

         AddMessagePostCommitTask(MessageReference m) {
            message = m;
         }

         public void run() {
            //restore a message to the message list...
            internalAddMessage(message);
         }
      }

      // The message gets added to the queue after the transaction commits
      Runnable task = new AddMessagePostCommitTask(mes);
      server.getPersistenceManager().getTxManager().addPostCommitTask(txId, task);

   }

   protected void setupMessageAcknowledgement(Subscription sub, MessageReference messageRef)  throws JMSException {
   	  SpyMessage message = messageRef.getMessage();
      AcknowledgementRequest ack = new AcknowledgementRequest();
      ack.destination = message.getJMSDestination();
      ack.messageID = message.getJMSMessageID();
      ack.subscriberId = sub.subscriptionId;
      ack.isAck = false;

      synchronized (unacknowledgedMessages) {
         unacknowledgedMessages.put(ack, new UnackedMessageInfo(messageRef, sub));
      }
   }

   protected void queueMessageForSending(Subscription sub, MessageReference message) throws JMSException {
      setupMessageAcknowledgement(sub, message);
      RoutedMessage r = new RoutedMessage();
      r.message = message;
      r.subscriptionId = new Integer(sub.subscriptionId);
      ((ClientConsumer) sub.clientConsumer).queueMessageForSending(r);
   }

   protected void addToReceivers(Subscription sub) {
      synchronized (receivers) {
         receivers.add(sub);
      }
   }

   protected boolean hasUnackedMessages(Subscription sub) {
      synchronized (unacknowledgedMessages) {
         for (Iterator it = unacknowledgedMessages.values().iterator(); it.hasNext();) {
            if (((UnackedMessageInfo) it.next()).sub == sub) {
               return true;
            }
         }
         return false;
      }
   }

   protected void checkRemovedSubscribers(Subscription sub) {
      synchronized (removedSubscribers) {
         if (removedSubscribers.contains(sub) && !hasUnackedMessages(sub)) {
            removedSubscribers.remove(sub);
            ((ClientConsumer) sub.clientConsumer).removeRemovedSubscription(sub.subscriptionId);
         }
      }
   }

   protected void dropMessage(MessageReference message)
   {
      try
      {
         if (this instanceof PersistentQueue && message.getHeaders().jmsDeliveryMode == DeliveryMode.PERSISTENT)
         {
            try
            {
               server.getPersistenceManager().remove(message, null);
            }
            catch (JMSException e )
            {
               try
               {
                  cat.warn("Message removed from queue, but not from the persistent store: "+message.getMessage(), e);
               }
               catch (JMSException x)
               {
                  cat.warn("Message removed from queue, but not from the persistent store: "+message.messageId, e);
               }
            }
         }
        	     
         server.getMessageCache().remove(message);
         // we are finally done with message so we can release it back to pool
         org.jboss.mq.MessagePool.releaseMessage(message.hardReference);
      }
      catch (JMSException e)
      {
         cat.warn("Unexcepted error dropping message", e);
      }
   }

   private void internalAddMessage(MessageReference message) {
      try {
         //try waiting receivers
         synchronized (receivers) {
            if (!receivers.isEmpty()) {
               for (Iterator it = receivers.iterator(); it.hasNext();) {
                  Subscription sub = (Subscription) it.next();
                  if (sub.accepts(message.getHeaders())) {
                     //queue message for sending to this sub
                     queueMessageForSending(sub, message);
                     it.remove();
                     return;
                  }
               }
            }
         }

         //else add to message list
         synchronized (messages) {
            messages.add(message);
         }
         
      } catch (JMSException e) {
         // Could happen at the accepts() calls
         cat.error("Caught unusual exception in internalAddMessage.", e);
         // And drop the message, otherwise we have a leak in the cache
         dropMessage(message);
      }
   }

   private class UnackedMessageInfo
   {
      public MessageReference messageRef;
      public Subscription sub;
      public UnackedMessageInfo(MessageReference messageRef, Subscription sub)
      {
         this.messageRef = messageRef;
         this.sub = sub;
      }
   }
}
