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
import java.util.TreeSet;
import javax.jms.DeliveryMode;

import javax.jms.Destination;
import javax.jms.JMSException;

import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.SpyQueue;
import org.jboss.mq.Subscription;

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
public class JMSQueue extends JMSDestination {

   public BasicQueue       queue;

   // Constructor ---------------------------------------------------
   public JMSQueue( SpyDestination dest, ClientConsumer temporary, JMSDestinationManager server )
      throws JMSException {
      super( dest, temporary, server );

      // If this is a non-temp queue, then we should persist data
      if ( temporaryDestination == null ) {
         queue = new PersistentQueue( server, dest );
         server.getPersistenceManager().restoreQueue(this, dest);
      } else {
         queue = new BasicQueue( server, destination.toString() );
      }
   }

   public void clientConsumerStopped( ClientConsumer clientConsumer ) {
      queue.clientConsumerStopped( clientConsumer );
   }

   public void addSubscriber( Subscription sub ) {
   }

   public void removeSubscriber( Subscription sub ) {
      queue.removeSubscriber( sub );
   }

   public void addReceiver( Subscription sub ) {
      queue.addReceiver( sub );
   }

   public void removeReceiver( Subscription sub ) {
      queue.removeReceiver( sub );
   }

   public void restoreMessage( MessageReference messageRef) {
   	  try {
   	  	  SpyMessage spyMessage = messageRef.getMessage();
	      synchronized ( this ) {
	         messageIdCounter = Math.max( messageIdCounter, spyMessage.header.messageId + 1 );
	      }
	      queue.restoreMessage( messageRef);
   	  } catch ( JMSException e ) {
   	  	cat.error("Could not restore message:", e);
   	  }
   }


   public SpyMessage[] browse( String selector )
      throws JMSException {
      return queue.browse( selector );
   }

   public String toString() {
      return "JMSDestination:" + destination;
   }


   public void acknowledge( org.jboss.mq.AcknowledgementRequest req, Subscription sub, org.jboss.mq.pm.Tx txId )
      throws JMSException {
      queue.acknowledge( req, txId );
   }

   public void addMessage( SpyMessage mes, org.jboss.mq.pm.Tx txId )
      throws JMSException {
//		if( mes.getJMSDeliveryMode() == DeliveryMode.PERSISTENT &&
//			temporaryDestination!=null ) {
//			throw new JMSException("Cannot write a persistent message to a temporary destination!");
//		}

      //Number the message so that we can preserve order of delivery.
      synchronized ( this ) {
       	 mes.header.messageId = messageIdCounter++;
	   	 MessageReference message = server.getMessageCache().add(mes);
         queue.addMessage( message, txId );
      }
   }

   public org.jboss.mq.SpyMessage receive( org.jboss.mq.Subscription sub, boolean wait )
      throws javax.jms.JMSException {
      return queue.receive( sub, wait );
   }
   /*
    * @see JMSDestination#isATopic()
    */

   /*
    * @see JMSDestination#isInUse()
    */
   public boolean isInUse()
   {
      return queue.isInUse();
   }

   /*
    * @see JMSDestination#close()
    */
   public void close() throws JMSException
   {
       server.getPersistenceManager().closeQueue(this, getSpyDestination());
   }

   /**
    * @see JMSDestination#destroy()
    */
   public void removeAllMessages() throws JMSException
   {
      queue.removeAllMessages();
   }

}

