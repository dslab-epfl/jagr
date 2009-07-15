/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mq.server;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;

import org.jboss.logging.Logger;
import org.jboss.mq.AcknowledgementRequest;
import org.jboss.mq.ConnectionToken;
import org.jboss.mq.ReceiveRequest;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.Subscription;
import org.jboss.mq.threadpool.ThreadPool;
import org.jboss.mq.threadpool.Work;
import org.jboss.mq.xml.XElement;

/**
 *  This represent the clients queue which consumes messages from the
 *  destinations on the provider.
 *
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @author <a href="mailto:pra@tim.se">Peter Antman</a>
 * @created    August 16, 2001
 * @version    $Revision: 1.1.1.1 $
 */
public class ClientConsumer implements Work
{
   private Logger log;
   //The JMSServer object
   JMSDestinationManager server;
   //The connection this queue will send messages over
   ConnectionToken connectionToken;
   //Is this connection enabled (Can we transmit to the receiver)
   boolean enabled;
   //Has this connection been closed?
   boolean closed = false;
   //Maps a subscription id to a Subscription
   HashMap subscriptions = new HashMap();
   //Maps a subscription id to a Subscription for subscriptions that have finished receiving
   HashMap removedSubscriptions = new HashMap();
   
   LinkedList blockedSubscriptions = new LinkedList();
   
   //List of messages waiting to be transmitted to the client
   private LinkedList messages = new LinkedList();
   //LinkedList of the the temporary destinations that this client created
   //	public LinkedList temporaryDestinations = new LinkedList();
   
   /**
    *  Flags that I am enqueued as work on my thread pool.
    */
   private boolean enqueued = false;
   
   // Static ---------------------------------------------------
   
   /**
    *  The {@link org.jboss.mq.threadpool.ThreadPool ThreadPool} that
    *  does the actual message pushing for us.
    */
   private static ThreadPool threadPool = null;
   
   // Constructor ---------------------------------------------------
   
   public ClientConsumer(JMSDestinationManager server, ConnectionToken connectionToken) throws JMSException
   {
      this.server = server;
      this.connectionToken = connectionToken;
      log = Logger.getLogger(ClientConsumer.class.getName() + ":" + connectionToken.getClientID());
      // Create thread pool
      synchronized (ClientConsumer.class)
      {
         if (threadPool == null)
            threadPool = new ThreadPool("Message Pushers", server.threadGroup, 10, true);
      }
   }
   
   public void setEnabled(boolean enabled) throws JMSException
   {
      if( log.isTraceEnabled() )
         log.trace("" + this +"->setEnabled(enabled=" + enabled + ")");
      this.enabled = enabled;
      if (enabled)
      {
         // queues might be waiting for messages.
         synchronized (blockedSubscriptions)
         {
            for (Iterator it = blockedSubscriptions.iterator(); it.hasNext();)
            {
               Subscription sub = (Subscription) it.next();
               JMSDestination dest = server.getJMSDestination(sub.destination);
               if (dest != null)
               {
                  dest.addReceiver(sub);
               }
            }
            blockedSubscriptions.clear();
         }
      }
   }
   
   public void queueMessageForSending(RoutedMessage r)
   {
     
      synchronized (messages)
      {
         if (closed)
            return; // Wouldn't be delivered anyway
         
         messages.add(r);
         if (!enqueued)
         {
            threadPool.enqueueWork(this);
            enqueued = true;
         }
      }
   }
   
   public void addSubscription(Subscription req) throws JMSException
   {
      if( log.isTraceEnabled() )
         log.trace("Adding subscription for: " + req);
      req.connectionToken = connectionToken;
      req.clientConsumer = this;
      
      JMSDestination jmsdest = (JMSDestination) server.getJMSDestination(req.destination);
      if (jmsdest == null)
      {
         throw new JMSException("The destination " + req.destination + " does not exist !");
      }
      
      jmsdest.addSubscriber(req);
      
      synchronized (subscriptions)
      {
         HashMap subscriptionsClone = (HashMap) subscriptions.clone();
         subscriptionsClone.put(new Integer(req.subscriptionId), req);
         subscriptions = subscriptionsClone;
      }
   }
   
   public void close()
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("" + this +"->close()");
      
      synchronized (messages)
      {
         closed = true;
         if (enqueued)
         {
            if( trace )
               log.trace("" + this +"->close(): Cancelling work in progress.");
            threadPool.cancelWork(this);
            enqueued = false;
         }
      }
      
      synchronized (subscriptions)
      {
         Iterator i = subscriptions.keySet().iterator();
         while (i.hasNext())
         {
            Integer subscriptionId = (Integer) i.next();
            try
            {
               removeSubscription(subscriptionId.intValue());
            }
            catch(JMSException ignore)
            {
            }
         }
      }
      
      // Nack the removed subscriptions, the connection is gone
      HashMap removedSubsClone = (HashMap) ((HashMap) removedSubscriptions).clone();
      Iterator i = removedSubsClone.values().iterator();
      while (i.hasNext())
      {
         Subscription removed = (Subscription) i.next();
         JMSDestination queue = (JMSDestination) server.getJMSDestination(removed.destination);
         if (queue == null)
            log.warn("The subscription was registered with a destination that does not exist: " + removed);
         try
         {
            queue.nackMessages(removed);
         }
         catch (JMSException e)
         {
            log.warn("Unable to nack removed subscription: " + removed, e);
         }
      }
   }

   public SpyMessage receive(int subscriberId, long wait) throws JMSException
   {
      
      Subscription req = (Subscription) subscriptions.get(new Integer(subscriberId));
      if (req == null)
      {
         throw new JMSException("The provided subscription does not exist");
      }
      
      JMSDestination queue = server.getJMSDestination(req.destination);
      if (queue == null)
      {
         throw new JMSException("The subscription's destination " + req.destination + " does not exist");
      }
      
      if (enabled)
      {
         return queue.receive(req, (wait != -1));
      }
      else if (wait != -1)
      {
         addBlockedSubscription(req);
      }
      
      return null;
   }
   
   public void removeSubscription(int subscriptionId) throws JMSException
   {
      if( log.isTraceEnabled() )
         log.trace("" + this +"->removeSubscription(subscriberId=" + subscriptionId + ")");
      
      Integer subId = new Integer(subscriptionId);
      Subscription req;
      synchronized (subscriptions)
      {
         HashMap subscriptionsClone = (HashMap) subscriptions.clone();
         req = (Subscription) subscriptionsClone.remove(subId);
         subscriptions = subscriptionsClone;
         if (req != null)
         {
            removedSubscriptions.put(subId, req);
         }
      }
      
      if (req == null)
      {
         throw new JMSException("The subscription had not been previously registered");
      }
      
      JMSDestination queue = (JMSDestination) server.getJMSDestination(req.destination);
      if (queue == null)
      {
         throw new JMSException("The subscription was registed with a destination that does not exist !");
      }
      
      queue.removeSubscriber(req);
      
   }
   
   /**
    *  Push some messages.
    */
   public void doWork()
   {
      try
      {
         
         ReceiveRequest[] job;
         
         synchronized (messages)
         {
            if (closed)
               return;
            
            job = new ReceiveRequest[messages.size()];
            Iterator iter = messages.iterator();
            for (int i = 0; iter.hasNext(); i++)
            {
               RoutedMessage rm = (RoutedMessage) iter.next();
               job[i] = rm.toReceiveRequest();
               iter.remove();
            }
            enqueued = false;
         }
         
         
         connectionToken.clientIL.receive(job);
         
      }
      catch(Exception e)
      {
         log.warn("Could not send messages to a receiver.", e);
         try
         {
            server.connectionFailure(connectionToken);
         }
         catch(Throwable ignore)
         {
            log.warn("Could not close the client connection..", ignore);
         }
      }
   }
   
   public String toString()
   {
      return "ClientConsumer:" + connectionToken.getClientID();
   }
   
   public void acknowledge(AcknowledgementRequest request, org.jboss.mq.pm.Tx txId) throws JMSException
   {
      Subscription sub = (Subscription) subscriptions.get(new Integer(request.subscriberId));
      
      if (sub == null)
      {
         //might be in removed subscriptions
         synchronized (subscriptions)
         {
            sub = (Subscription) removedSubscriptions.get(new Integer(request.subscriberId));
         }
      }
      
      if (sub == null)
      {
         throw new JMSException("The provided subscription does not exist");
      }
      
      JMSDestination queue = server.getJMSDestination(sub.destination);
      if (queue == null)
      {
         throw new JMSException("The subscription's destination " + sub.destination + " does not exist");
      }
      
      queue.acknowledge(request, sub, txId);
   }
   
   void addBlockedSubscription(Subscription sub)
   {
      synchronized (blockedSubscriptions)
      {
         blockedSubscriptions.add(sub);
      }
   }
   
   void removeRemovedSubscription(int subId)
   {
      Subscription sub = null;
      synchronized (subscriptions)
      {
         sub = (Subscription) removedSubscriptions.remove(new Integer(subId));
      }
      if (sub != null)
      {
         JMSDestination topic = server.getJMSDestination(sub.destination);
         if (topic instanceof JMSTopic)
            ((JMSTopic) topic).cleanupSubscription(sub);
      }
   }

   /**
    * Get a subscription for the subscriberid
    *
    * @exception JMSException if it can not find the subscription.
    */
   public Subscription getSubscription(int subscriberId) throws JMSException {
      Subscription req = (Subscription) subscriptions.get(new Integer(subscriberId));
      if (req == null)
         throw new JMSException("The provided subscription does not exist");
      
      return req;
   }
}
