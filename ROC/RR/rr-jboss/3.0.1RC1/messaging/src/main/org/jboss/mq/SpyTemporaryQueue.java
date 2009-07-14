/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mq;
import javax.jms.JMSException;

import javax.jms.TemporaryQueue;

import org.jboss.logging.Logger;

/**
 *  This class implements javax.jms.TemporaryQueue
 *
 * @author     Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author     Hiram Chirino (Cojonudo14@hotmail.com)
 * @created    August 16, 2001
 * @version    $Revision: 1.1.1.1 $
 */
public class SpyTemporaryQueue
       extends SpyQueue
       implements TemporaryQueue {

   //The DistributedConnection of its creator
   ConnectionToken  dc;

   static Logger cat = Logger.getLogger( SpyTemporaryQueue.class );

   // Constructor ---------------------------------------------------

   public SpyTemporaryQueue( String queueName, ConnectionToken dc_ ) {
      super( queueName );
      dc = dc_;
   }


   // Public --------------------------------------------------------

   public void delete()
      throws JMSException {
      try {
         dc.clientIL.deleteTemporaryDestination( this );
      } catch ( Exception e ) {
         throw new SpyJMSException( "Cannot delete the TemporaryQueue", e );
      }
   }
}
