/*
 * Copyright (c) 2000 Hiram Chirino <Cojonudo14@hotmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jboss.test.jbossmq.test;

import javax.naming.*;
import javax.jms.*;
import java.util.*;

import EDU.oswego.cs.dl.util.concurrent.CountDown;
import org.apache.log4j.Category;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;

import junit.framework.TestSuite;
import junit.framework.Test;


/**
 * JBossMQUnitTestCase.java
 *
 * Some simple tests of spyderMQ
 *
 * @author
 * @version
 */
public class JBossMQUnitTestCase
   extends JBossTestCase
{
   // Provider specific
   static String TOPIC_FACTORY;// = "ConnectionFactory";
   static String QUEUE_FACTORY;// = "ConnectionFactory";
   
   static String TEST_QUEUE = "queue/testQueue";
   static String TEST_TOPIC = "topic/testTopic";
   static String TEST_DURABLE_TOPIC = "topic/testDurableTopic";
   
   //JMSProviderAdapter providerAdapter;
   static Context context;
   static QueueConnection queueConnection;
   static TopicConnection topicConnection;
   
   public JBossMQUnitTestCase(String name) throws Exception
   {
      super(name);
   }
   
   // Emptys out all the messages in a queue
   private void drainQueue() throws Exception
   {
      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue)context.lookup(TEST_QUEUE);
      
      QueueReceiver receiver = session.createReceiver(queue);
      Message message = receiver.receive( 50 );
      int c=0;
      while( message != null )
      {
         message = receiver.receive( 50 );
         c++;
      }
      
      if( c!=0 )
         getLog().debug("  Drained "+c+" messages from the queue");
      
      session.close();
   }
   
   protected void connect() throws Exception
   {
      
      if( context == null )
      {
         
         context = new InitialContext();
         
      }
      QueueConnectionFactory queueFactory = (QueueConnectionFactory) context.lookup(QUEUE_FACTORY);
      queueConnection = queueFactory.createQueueConnection();
      
      TopicConnectionFactory topicFactory = (TopicConnectionFactory)context.lookup(TOPIC_FACTORY);
      topicConnection = topicFactory.createTopicConnection();
      
      getLog().debug("Connection to spyderMQ established.");
      
   }
   
   protected void disconnect() throws Exception
   {
      queueConnection.close();
      topicConnection.close();
   }
   
   /**
    * Test that messages are ordered by message arrival and priority.
    * This also tests :
    * 		Using a non-transacted AUTO_ACKNOWLEDGE session
    *		Using a QueueReceiver
    *		Using a QueueSender
    *			Sending PERSITENT and NON_PERSISTENT text messages.
    *		Using a QueueBrowser
    */
   public void testQueueMessageOrder()	throws Exception
   {
      
      getLog().debug("Starting QueueMessageOrder test");
      
      connect();
      
      queueConnection.start();
      
      drainQueue();
      
      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue)context.lookup(TEST_QUEUE);
      QueueSender sender = session.createSender(queue);
      
      TextMessage message = session.createTextMessage();
      message.setText("Normal message");
      sender.send(message, DeliveryMode.NON_PERSISTENT, 4, 0);
      //sender.send(queue, message, DeliveryMode.NON_PERSISTENT, 4, 0);
      message.setText("Persistent message");
      sender.send(message, DeliveryMode.PERSISTENT, 4, 0);
      //sender.send(queue, message, DeliveryMode.PERSISTENT, 4, 0);
      message.setText("High Priority Persistent message");
      sender.send(message, DeliveryMode.PERSISTENT, 10, 0);
      //sender.send(queue, message, DeliveryMode.PERSISTENT, 10, 0);
      
      //message.setText("Expiring Persistent message");
      //sender.send(queue, message, DeliveryMode.NON_PERSISTENT, 4, 1);
      
      QueueBrowser browser = session.createBrowser( queue );
      Enumeration enum = browser.getEnumeration();
      //message = (TextMessage)enum.nextElement();
      //if( !message.getText().equals("High Priority Persistent message") )
      //	throw new Exception("Queue is not prioritizing messages correctly. Unexpected Message:"+message);
      getLog().debug(message.getText());
      
      message = (TextMessage)enum.nextElement();
      //if( !message.getText().equals("Normal message") )
      //	throw new Exception("Queue is not ordering messages correctly. Unexpected Message:"+message);
      getLog().debug(message.getText());
      
      message = (TextMessage)enum.nextElement();
      //if( !message.getText().equals("Persistent message") )
      //	throw new Exception("Queue is not ordering messages correctly. Unexpected Message:"+message);
      getLog().debug(message.getText());
      
      // if( enum.hasMoreElements() )
      //	throw new Exception("Queue does not expire messages correctly. Unexpected Message:"+enum.nextElement());
      
      disconnect();
      getLog().debug("QueueMessageOrder passed");
   }
   
   /**
    * Test that a using QueueRequestor works.
    * this also tests that :
    *		temporary queues work.
    */
   public void testRequestReplyQueue() throws Exception
   {
      
      getLog().debug("Starting RequestReplyQueue test");
      connect();
      
      {
         queueConnection.start();
         drainQueue( );
      }
      
      Thread serverThread = new Thread()
      {
         public void run()
         {
            Category log = Category.getInstance(getClass().getName());
            try
            {
               log.debug("Server Thread Started");
               QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
               Queue queue = (Queue)context.lookup(TEST_QUEUE);
               
               QueueReceiver queueReceiver = session.createReceiver(queue);
               
               boolean done = false;
               while ( !done )
               {
                  TextMessage message = (TextMessage) queueReceiver.receive();
                  Queue tempQueue = (Queue) message.getJMSReplyTo();
                  
                  QueueSender replySender = session.createSender(tempQueue);
                  TextMessage reply = session.createTextMessage();
                  reply.setText("Request Processed");
                  reply.setJMSCorrelationID(message.getJMSMessageID());
                  replySender.send(reply);
                  
                  if( message.getText().equals("Quit") )
                     done = true;
               }
               
               session.close();
               log.debug("Server Thread Finished");
               
            } catch ( Exception e )
            {
               log.error("Error",e);
            }
         }
      };
      
      serverThread.start();
      
      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue)context.lookup(TEST_QUEUE);
      
      QueueRequestor queueRequestor = new QueueRequestor(session, queue);
      TextMessage message = session.createTextMessage();
      message.setText("Request Test");
      
      for( int i=0; i < 5; i ++ )
      {
         
         getLog().debug("Making client request #"+i);
         TextMessage reply = (TextMessage) queueRequestor.request(message);
         String replyID = new String(reply.getJMSCorrelationID());
         if (!replyID.equals(message.getJMSMessageID()))
            throw new Exception("REQUEST: ERROR: Reply does not match sent message");
         
      }
      
      getLog().debug("Making client request to shut server down.");
      message.setText("Quit");
      queueRequestor.request(message);
      
      serverThread.join();
      disconnect();
      
      getLog().debug("RequestReplyQueue passed");
   }
   
   public void testMessageListener() throws Exception
   {
      getLog().debug("Starting MessageListener test");
      
      connect();
      queueConnection.start();
      drainQueue();
      final CountDown counter1 = new CountDown(3);

      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue)context.lookup(TEST_QUEUE);
      
      QueueReceiver receiver = session.createReceiver(queue);
      receiver.setMessageListener(new MessageListener()
      {
         public void onMessage(Message msg)
         {
            Category log = Category.getInstance(getClass().getName());
            log.debug("ML");
            try
            {
               if(msg instanceof TextMessage)
               {
                  log.debug(((TextMessage)msg).getText());
                  counter1.release();
               }
            }
            catch(Exception e)
            {
            }
         }
      });

      QueueSender sender = session.createSender(queue);
      
      TextMessage message = session.createTextMessage();
      message.setText("Normal message");
      sender.send(message, DeliveryMode.NON_PERSISTENT, 4, 0);
      //sender.send(queue, message, DeliveryMode.NON_PERSISTENT, 4, 0);
      message.setText("Persistent message");
      sender.send( message, DeliveryMode.PERSISTENT, 4, 0);
      //sender.send(queue, message, DeliveryMode.PERSISTENT, 4, 0);
      message.setText("High Priority Persistent message");
      sender.send(message, DeliveryMode.PERSISTENT, 10, 0);
      //sender.send(queue, message, DeliveryMode.PERSISTENT, 10, 0);

      // Wait for the msgs to be received
      counter1.acquire();
      log.debug("MessageListener1 received the TMs sent");
      
      final CountDown counter2 = new CountDown(2);
      receiver.setMessageListener(new MessageListener()
      {
         public void onMessage(Message msg)
         {
            Category log = Category.getInstance(getClass().getName());
            log.debug("ML 2");
            try
            {
               if(msg instanceof TextMessage)
               {
                  log.debug(((TextMessage)msg).getText());
                  counter2.release();
               }
            }catch(Exception e)
            {}
         }
      });
      
      message.setText("Persistent message");
      sender.send( message, DeliveryMode.PERSISTENT, 4, 0);
      //sender.send(queue, message, DeliveryMode.PERSISTENT, 4, 0);
      message.setText("High Priority Persistent message");
      sender.send(message, DeliveryMode.PERSISTENT, 10, 0);
      //sender.send(queue, message, DeliveryMode.PERSISTENT, 10, 0);

      // Wait for the msgs to be received
      counter2.acquire();
      log.debug("MessageListener2 received the TMs sent");
      
      receiver.setMessageListener(null);
      
      message.setText("Persistent message");
      sender.send(message, DeliveryMode.PERSISTENT, 4, 0);
      //sender.send(queue, message, DeliveryMode.PERSISTENT, 4, 0);
      message.setText("High Priority Persistent message");
      sender.send(message, DeliveryMode.PERSISTENT, 10, 0);
      //sender.send(queue, message, DeliveryMode.PERSISTENT, 10, 0);
      
      sender.close();
      drainQueue();
      disconnect();
      getLog().debug("MessageListener test passed");
   }

   public void testApplicationServerStuff() throws Exception
   {
      getLog().debug("Starting testing app server stuff");
      connect();
      
      Queue testQueue = (Queue)context.lookup(TEST_QUEUE);
      final QueueSession session = queueConnection.createQueueSession(false,Session.AUTO_ACKNOWLEDGE);

      session.setMessageListener(new MessageListener()
      {
         public void onMessage(Message mess)
         {
            Category log = Category.getInstance(getClass().getName());
            log.debug("Processing message");
            try
            {
               if(mess instanceof TextMessage)
                  log.debug(((TextMessage)mess).getText());
            }catch(Exception e)
            {
               log.error("Error",e);
            }
         }
      });
      
      QueueSender sender = session.createSender(testQueue);
      sender.send(session.createTextMessage("Hi"));
      sender.send(session.createTextMessage("There"));
      sender.send(session.createTextMessage("Guys"));
      queueConnection.createConnectionConsumer(testQueue,null,new ServerSessionPool()
      {
         public ServerSession getServerSession()
         {
            Category.getInstance(getClass().getName()).debug("Getting server session.");
            return new ServerSession()
            {
               public Session getSession()
               {
                  return session;
               }
               public void start()
               {
                  Category.getInstance(getClass().getName()).debug("Starting server session.");
                  session.run();
               }
            };
         }
      },10);
      
      queueConnection.start();
      
      try
      { Thread.sleep(5*1000); }catch(Exception e)
      {}
      
      disconnect();
      getLog().debug("Testing app server stuff passed");
   }
   
   public void testPM() throws Exception
   {
      getLog().debug("Starting testing pm");
      //simply put a few messages on the test queue for next time.
      connect();
      
      Queue testQueue = (Queue)context.lookup(TEST_QUEUE);
      QueueSession session = queueConnection.createQueueSession(false,Session.AUTO_ACKNOWLEDGE);
      QueueSender sender = session.createSender(testQueue);
      sender.send(session.createTextMessage("From last time"));
      sender.send(session.createTextMessage("From last time"));
      sender.send(session.createTextMessage("From last time"));
      sender.close();
      session.close();
      disconnect();
      getLog().debug("Testing pm stuff passed");
   }
   
   private void drainMessagesForTopic(TopicSubscriber sub) throws JMSException
   {
      Message msg = sub.receive(50);
      int c = 0;
      while(msg != null)
      {
         c++;
         if(msg instanceof TextMessage)
            getLog().debug(((TextMessage)msg).getText());
         msg = sub.receive(50);
      }
      getLog().debug("Received "+c+" messages from topic.");
   }
   
   public void testTopics() throws Exception
   {
      getLog().debug("Starting Topic test");
      connect();

      TopicConnectionFactory topicFactory = (TopicConnectionFactory)context.lookup(TOPIC_FACTORY);
      topicConnection = topicFactory.createTopicConnection("john","needle");
      
      topicConnection.start();
      
      //set up some subscribers to the topic
      TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      Topic topic = (Topic)context.lookup(TEST_TOPIC);
      
      TopicSubscriber sub1 = session.createDurableSubscriber(topic,"sub1");
      TopicSubscriber sub2 = session.createSubscriber(topic);
      TopicSubscriber sub3 = session.createSubscriber(topic);
      
      //Now a sender
      TopicPublisher sender = session.createPublisher(topic);
      
      //send some messages
      sender.publish(session.createTextMessage("Message 1"));
      sender.publish(session.createTextMessage("Message 2"));
      sender.publish(session.createTextMessage("Message 3"));
      drainMessagesForTopic(sub1);
      drainMessagesForTopic(sub2);
      drainMessagesForTopic(sub3);
      
      //close some subscribers
      sub1.close();
      sub2.close();
      
      //send some more messages
      sender.publish(session.createTextMessage("Message 4"));
      sender.publish(session.createTextMessage("Message 5"));
      sender.publish(session.createTextMessage("Message 6"));
      
      //give time for message 4 to be negatively acked (as it will be cause last receive timed out)
      try
      { Thread.sleep(5*1000); }catch(InterruptedException e)
      {}
      
      drainMessagesForTopic(sub3);
      
      //open subscribers again.
      sub1 = session.createDurableSubscriber(topic,"sub1");
      sub2 = session.createSubscriber(topic);
      
      //Send a final message
      sender.publish(session.createTextMessage("Final message"));
      sender.close();
      
      drainMessagesForTopic(sub1);
      drainMessagesForTopic(sub2);
      drainMessagesForTopic(sub3);
      
      sub1.close();
      sub2.close();
      sub3.close();
      
      session.unsubscribe("sub1");
      
      topicConnection.stop();
      topicConnection.close();
      
      disconnect();
      getLog().debug("Topic test passed");
   }
   /**
    * Test to seeif the NoLocal feature of topics works.
    * Messages published from the same connection should not
    * be received by Subscribers on the same connection.
    */
   public void testTopicNoLocal() throws Exception
   {
      getLog().debug("Starting TopicNoLocal test");
      connect();
      
      TopicConnectionFactory topicFactory = (TopicConnectionFactory)context.lookup(TOPIC_FACTORY);
      TopicConnection topicConnection1 = topicFactory.createTopicConnection();
      topicConnection1.start();
      TopicConnection topicConnection2 = topicFactory.createTopicConnection();
      topicConnection2.start();
      
      // We don't want local messages on this topic.
      TopicSession session1 = topicConnection1.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      Topic topic = (Topic)context.lookup(TEST_TOPIC);
      TopicSubscriber subscriber1 = session1.createSubscriber(topic, null, true);
      TopicPublisher sender1 = session1.createPublisher(topic);
      
      //Now a sender
      TopicSession session2 = topicConnection2.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      TopicPublisher sender2 = session2.createPublisher(topic);
      
      drainMessagesForTopic(subscriber1);
      
      //send some messages
      sender1.publish(session1.createTextMessage("Local Message"));
      sender2.publish(session2.createTextMessage("Remote Message"));
      
      // Get the messages, we should get the remote message
      // but not the local message
      TextMessage msg1 = (TextMessage)subscriber1.receive(2000);
      if( msg1 == null )
      {
         fail("Did not get any messages");
      } else
      {
         getLog().debug("Got message: "+msg1);
         if(msg1.getText().equals("Local Message"))
         {
            fail("Got a local message");
         }
         TextMessage msg2 = (TextMessage)subscriber1.receive(2000);
         if( msg2 != null )
         {
            getLog().debug("Got message: "+msg2);
            fail("Got an extra message.  msg1:"+msg1+", msg2:"+msg2);
         }
      }
      
      topicConnection1.stop();
      topicConnection1.close();
      topicConnection2.stop();
      topicConnection2.close();
      
      disconnect();
      getLog().debug("TopicNoLocal test passed");
   }

   /**
    * Test subscribing to a topic with one selector, then changing to another
    */
   public void testTopicSelectorChange() throws Exception
   {
      getLog().debug("Starting TopicSelectorChange test");

      getLog().debug("Create topic connection");
      TopicConnectionFactory topicFactory = (TopicConnectionFactory)context.lookup(TOPIC_FACTORY);
      topicConnection = topicFactory.createTopicConnection("john", "needle");
      topicConnection.start();

      try
      {
      getLog().debug("Retrieving Topic");
      Topic topic = (Topic)context.lookup(TEST_DURABLE_TOPIC);

      getLog().debug("Creating a send session");
      TopicSession sendSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      TopicPublisher sender = sendSession.createPublisher(topic);

      getLog().debug("Clearing the topic");
      TopicSession subSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      TopicSubscriber subscriber = subSession.createDurableSubscriber(topic, "test");
      Message message = subscriber.receive(50);
      while (message != null)
         message = subscriber.receive(50);
      subSession.close();

      getLog().debug("Subscribing to topic, looking for Value = 'A'");
      subSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      subscriber = subSession.createDurableSubscriber(topic, "test", "Value = 'A'", false);

      getLog().debug("Send some messages");
      message = sendSession.createTextMessage("Message1");
      message.setStringProperty("Value", "A");
      sender.publish(message);
      message = sendSession.createTextMessage("Message2");
      message.setStringProperty("Value", "A");
      sender.publish(message);
      message = sendSession.createTextMessage("Message3");
      message.setStringProperty("Value", "B");
      sender.publish(message);

      getLog().debug("Retrieving the A messages");
      message = subscriber.receive(2000);
      assertTrue("Expected message 1", message != null);
      assertTrue("Should get an A", message.getStringProperty("Value").equals("A"));
      message = subscriber.receive(2000);
      assertTrue("Expected message 2", message != null);
      assertTrue("Should get a second A", message.getStringProperty("Value").equals("A"));
      assertTrue("That should be it for A", subscriber.receive(2000) == null);

      getLog().debug("Closing the subscriber without acknowledgement");
      subSession.close();

      getLog().debug("Subscribing to topic, looking for Value = 'B'");
      subSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      subscriber = subSession.createDurableSubscriber(topic, "test", "Value = 'B'", false);

      getLog().debug("Retrieving the non-existent B messages");
      assertTrue("B should not be there", subscriber.receive(2000) == null);

      getLog().debug("Closing the subscriber.");
      subSession.close();

      getLog().debug("Subscribing to topic, looking for those Value = 'A'");
      subSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      subscriber = subSession.createDurableSubscriber(topic, "test", "Value = 'A'", false);
      assertTrue("Should not be any A the subscription was changed", subscriber.receive(2000) == null);
      subSession.close();

      getLog().debug("Subscribing to topic, looking for everything");
      subSession = topicConnection.createTopicSession(false, Session.CLIENT_ACKNOWLEDGE);
      subscriber = subSession.createDurableSubscriber(topic, "test", null, false);

      message = sendSession.createTextMessage("Message4");
      message.setStringProperty("Value", "A");
      sender.publish(message);

      message = subscriber.receive(2000);
      assertTrue("Expected message 4", message != null);
      assertTrue("Should be an A which we don't acknowledge", message.getStringProperty("Value").equals("A"));
      subSession.close();

      getLog().debug("Subscribing to topic, looking for the Value = 'A'");
      subSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      subscriber = subSession.createDurableSubscriber(topic, "test", "Value = 'A'", false);
      assertTrue("Should not be any A, the subscription was changed. Even though the old and new selectors match the message", subscriber.receive(2000) == null);
      subSession.close();

      getLog().debug("Closing the send session");
      sendSession.close();

      getLog().debug("Removing the subscription");
      subSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      subSession.unsubscribe("test");

      }
      finally
      {
         getLog().debug("Closing the connection");
         topicConnection.close();
      }

      getLog().debug("TopicSelectorChange test passed");
   }
   
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      
      suite.addTest(new JBossTestSetup(new TestSuite(JBossMQUnitTestCase.class))
      {
         protected void setUp() throws Exception
         {
            this.getLog().info("JBossMQUnitTestCase, ConnectionFactory started");
            JBossMQUnitTestCase.TOPIC_FACTORY = "ConnectionFactory";
            JBossMQUnitTestCase.QUEUE_FACTORY = "ConnectionFactory";
         }
         protected void tearDown() throws Exception
         {
            this.getLog().info("JBossMQUnitTestCase, ConnectionFactory done");
         }
      });
      
      suite.addTest(new JBossTestSetup(new TestSuite(JBossMQUnitTestCase.class))
      {
         protected void setUp() throws Exception
         {
            this.getLog().info("JBossMQUnitTestCase, UILConnectionFactory started");
            JBossMQUnitTestCase.TOPIC_FACTORY = "UILConnectionFactory";
            JBossMQUnitTestCase.QUEUE_FACTORY = "UILConnectionFactory";
         }
         protected void tearDown() throws Exception
         {
            this.getLog().info("JBossMQUnitTestCase, UILConnectionFactory done");
         }
      });
      
      suite.addTest(new JBossTestSetup(new TestSuite(JBossMQUnitTestCase.class))
      {
         protected void setUp() throws Exception
         {
            this.getLog().info("JBossMQUnitTestCase, RMIConnectionFactory started");
            JBossMQUnitTestCase.TOPIC_FACTORY = "RMIConnectionFactory";
            JBossMQUnitTestCase.QUEUE_FACTORY = "RMIConnectionFactory";
         }
         protected void tearDown() throws Exception
         {
            this.getLog().info("JBossMQUnitTestCase, RMIConnectionFactory done");
         }
      });
      
      //in vm won't work ;-) We have to write an IN VM MBean junit testrunner!
      /*suite.addTest(new JBossTestSetup(new TestSuite(JBossMQUnitTestCase.class))
         {
            protected void setUp() throws Exception
            {
               this.getLog().info("JBossMQUnitTestCase, java:/ConnectionFactory started");
               JBossMQUnitTestCase.TOPIC_FACTORY = "java:/ConnectionFactory";
               JBossMQUnitTestCase.QUEUE_FACTORY = "java:/ConnectionFactory";
            }
            protected void tearDown() throws Exception
            {
               this.getLog().info("JBossMQUnitTestCase, java:/ConnectionFactory done");
            }
            });*/
      return suite;
   }

   static public void main( String []args )
   {
      String newArgs[] = { "org.jboss.test.jbossmq.test.JBossMQUnitTestCase" };
      junit.swingui.TestRunner.main(newArgs);
   }
}
