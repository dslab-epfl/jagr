/*
 * JBossMQ, the OpenSource JMS implementation
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mq.pm.jdbc2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.mq.SpyDestination;
import org.jboss.mq.SpyJMSException;
import org.jboss.mq.SpyMessage;
import org.jboss.mq.pm.TxManager;
import org.jboss.mq.server.JMSDestination;
import org.jboss.mq.server.MessageCache;
import org.jboss.mq.server.MessageReference;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.tm.TransactionManagerService;

/**
 * This class manages all persistence related services for JDBC based
 * persistence.
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean, org.jboss.mq.pm.PersistenceManagerMBean, org.jboss.mq.pm.CacheStoreMBean"
 *
 * @author Jayesh Parayali (jayeshpk1@yahoo.com)
 * @author Hiram Chirino (cojonudo14@hotmail.com)
 *
 *  @version $Revision: 1.1.1.1 $
 */
public class PersistenceManager
   extends ServiceMBeanSupport
   implements PersistenceManagerMBean, org.jboss.mq.pm.PersistenceManager, org.jboss.mq.pm.CacheStore
{

   /////////////////////////////////////////////////////////////////////////////////
   //
   // TX state attibutes
   //
   /////////////////////////////////////////////////////////////////////////////////
   private long nextTransactionId = 0;
   private TxManager txManager;
   private DataSource datasource;
   private MessageCache messageCache;
   private TransactionManager tm;

   /////////////////////////////////////////////////////////////////////////////////
   //
   // JDBC Access Attributes
   //
   /////////////////////////////////////////////////////////////////////////////////

   String SELECT_ALL_UNCOMMITED_TXS = "SELECT TXID FROM JMS_TRANSACTIONS";
   String DELETE_ALL_MESSAGE_WITH_TX = "DELETE FROM JMS_MESSAGES WHERE TXID=?";
   String DELETE_TX = "DELETE FROM JMS_TRANSACTIONS WHERE TXID = ?";
   String DELETE_MARKED_MESSAGES = "DELETE FROM JMS_MESSAGES WHERE TXID=? AND TXOP=?";
   String INSERT_TX = "INSERT INTO JMS_TRANSACTIONS (TXID) values(?)";
   String SELECT_MAX_TX = "SELECT MAX(TXID) FROM JMS_MESSAGES";
   String SELECT_MESSAGES_IN_DEST = "SELECT MESSAGEID, MESSAGEBLOB FROM JMS_MESSAGES WHERE DESTINATION=?";
   String SELECT_MESSAGE = "SELECT MESSAGEID, MESSAGEBLOB FROM JMS_MESSAGES WHERE MESSAGEID=? AND DESTINATION=?";
   String INSERT_MESSAGE = "INSERT INTO JMS_MESSAGES (MESSAGEID, DESTINATION, MESSAGEBLOB, TXID, TXOP) VALUES(?,?,?,?,?)";
   String MARK_MESSAGE = "UPDATE JMS_MESSAGES SET (TXID, TXOP) VALUES(?,?) WHERE MESSAGEID=? AND DESTINATION=?";
   String DELETE_MESSAGE = "DELETE FROM JMS_MESSAGES WHERE MESSAGEID=? AND DESTINATION=?";
   String CREATE_MESSAGE_TABLE =
      "CREATE TABLE JMS_MESSAGES ( MESSAGEID INTEGER NOT NULL, "
         + "DESTINATION VARCHAR(32) NOT NULL, TXID INTEGER, TXOP CHAR(1),"
         + "MESSAGEBLOB OBJECT, PRIMARY KEY (MESSAGEID, DESTINATION) )";
   String CREATE_TX_TABLE = "CREATE TABLE JMS_TRANSACTIONS ( TXID INTEGER )";

   static final int OBJECT_BLOB = 0;
   static final int BYTES_BLOB = 1;
   static final int BINARYSTREAM_BLOB = 2;
   static final int BLOB_BLOB = 3;

   int blobType = OBJECT_BLOB;
   boolean createTables;

   boolean restoringQueue = false;

   /////////////////////////////////////////////////////////////////////////////////
   //
   // Constructor.
   //
   /////////////////////////////////////////////////////////////////////////////////
   public PersistenceManager() throws javax.jms.JMSException
   {
      txManager = new TxManager(this);
   }

   /**
    * This inner class helps handle the tx management of the jdbc connections.
    * 
    */
   class TransactionManagerStrategy
   {

      Transaction threadTx;
      Transaction newTransaction;

      void startTX() throws JMSException
      {
         //log.debug("starting a new TM transaction");
         try
         {
            // Thread arriving must be clean (jboss doesn't set the thread
            // previously). However optimized calls come with associated
            // thread for example. We suspend the thread association here, and
            // resume in the finally block of the following try.
            threadTx = tm.suspend();

            // Always begin a transaction
            tm.begin();

            // get it
            newTransaction = tm.getTransaction();
         }
         catch (Exception e)
         {
            try
            {
               if (threadTx != null)
                  tm.resume(threadTx);
            }
            catch (Exception ignore)
            {
            }
            throw new SpyJMSException("Could not start a transaction with the transaction manager.", e);
         }
      }

      void setRollbackOnly() throws JMSException
      {
         //log.debug("rolling back a TM transaction");
         try
         {
            newTransaction.setRollbackOnly();
         }
         catch (Exception e)
         {
            throw new SpyJMSException("Could not start a mark the transaction for rollback .", e);
         }
      }

      void endTX() throws JMSException
      {
         //log.debug("ending TM transaction.");
         try
         {
            if (newTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK)
            {
               newTransaction.rollback();
            }
            else
            {
               newTransaction.commit();
            }
         }
         catch (Exception e)
         {
            throw new SpyJMSException("Could not start a transaction with the transaction manager.", e);
         }
         finally
         {
            try
            {
               if (threadTx != null)
                  tm.resume(threadTx);
            }
            catch (Exception ignore)
            {
            }
         }
      }
   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // TX Resolution.
   //
   /////////////////////////////////////////////////////////////////////////////////
   synchronized public void resolveAllUncommitedTXs() throws JMSException
   {
      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      try
      {
         c = datasource.getConnection();

         if (createTables)
         {
            try
            {
               stmt = c.prepareStatement(CREATE_MESSAGE_TABLE);
               stmt.executeUpdate();
            }
            catch (SQLException e)
            {
               log.debug("Could not create table with SQL: " + CREATE_MESSAGE_TABLE + ", got : " + e);
            }
            finally
            {
               try
               {
                  stmt.close();
               }
               catch(Throwable ignore)
               {
               }
            }

            try
            {
               stmt = c.prepareStatement(CREATE_TX_TABLE);
               stmt.executeUpdate();
            }
            catch (SQLException e)
            {
               log.debug("Could not create table with SQL: " + CREATE_TX_TABLE + ", got : " + e);
            }
            finally
            {
               try
               {
                  stmt.close();
               }
               catch(Throwable ignore)
               {
               }
            }
         }

         // Delete all the messages that were added but thier tx's were not commited.
         stmt = c.prepareStatement(DELETE_ALL_MESSAGE_WITH_TX);
         Collection unresolvedTXs = findAllUncommitedTXs(c);

         Iterator i = unresolvedTXs.iterator();
         while (i.hasNext())
         {
            long txid = ((Long) i.next()).longValue();
            stmt.setLong(1, txid);
            stmt.executeUpdate();
         }
         stmt.close();

         // Delete all the non persistent messages that were added by the 
         // CacheStore interface of this PM
         removeMarkedMessages(c, null, "T");

         // Find out what the next TXID should be
         stmt = c.prepareStatement(SELECT_MAX_TX);
         rs = stmt.executeQuery();
         if (rs.next())
         {
            nextTransactionId = rs.getLong(1) + 1;
         }

      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not resolve uncommited transactions.  Message recovery may not be accurate", e);
      }
      finally
      {
         try
         {
            rs.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();
      }
   }

   public Collection findAllUncommitedTXs(Connection con) throws SQLException
   {
      LinkedList items = new LinkedList();
      PreparedStatement stmt = null;
      ResultSet rs = null;
      try
      {
         stmt = con.prepareStatement(SELECT_ALL_UNCOMMITED_TXS);
         rs = stmt.executeQuery();
         while (rs.next())
         {
            long id = rs.getLong(1);
            items.add(new Long(id));
         }
      }
      finally
      {
         try
         {
            rs.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            stmt.close();
         }
         catch (Throwable ignore)
         {
         }
      }
      return items;
   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // Message Recovery
   //
   /////////////////////////////////////////////////////////////////////////////////
   synchronized public void restoreQueue(JMSDestination jmsDest, SpyDestination dest) throws javax.jms.JMSException
   {
      if (jmsDest == null)
         throw new IllegalArgumentException("Must supply non null JMSDestination to restoreQueue");
      if (dest == null)
         throw new IllegalArgumentException("Must supply non null SpyDestination to restoreQueue");

      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      try
      {
         restoringQueue = true;
         c = datasource.getConnection();
         stmt = c.prepareStatement(SELECT_MESSAGES_IN_DEST);
         stmt.setString(1, dest.toString());

         rs = stmt.executeQuery();
         int counter=0;
         while (rs.next())
         {
            SpyMessage message = extractMessage(rs);
            if (dest instanceof org.jboss.mq.SpyTopic)
               message.header.durableSubscriberID = ((org.jboss.mq.SpyTopic)dest).getDurableSubscriptionID();
            MessageReference mr = messageCache.add(message);
            mr.setStored(((SpyDestination) message.getJMSDestination()).toString());
            jmsDest.restoreMessage(mr);
            counter++;
         }
         
         log.debug("Restored "+counter+" message(s) to: "+dest);
      }
      catch (IOException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not restore messages to destination : " + dest.toString(), e);
      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not restore messages to destination : " + dest.toString(), e);
      }
      finally
      {
         restoringQueue = false;
         try
         {
            rs.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();
      }

   }

   SpyMessage extractMessage(ResultSet rs) throws SQLException, IOException
   {
      try
      {
         long messageid = rs.getLong(1);

         SpyMessage message = null;

         if (blobType == OBJECT_BLOB)
         {

            message = (SpyMessage) rs.getObject(2);

         }
         else if (blobType == BYTES_BLOB)
         {

            byte[] st = rs.getBytes(2);
            ByteArrayInputStream baip = new ByteArrayInputStream(st);
            ObjectInputStream ois = new ObjectInputStream(baip);
            message = SpyMessage.readMessage(ois);

         }
         else if (blobType == BINARYSTREAM_BLOB)
         {

            ObjectInputStream ois = new ObjectInputStream(rs.getBinaryStream(2));
            message = SpyMessage.readMessage(ois);

         }
         else if (blobType == BLOB_BLOB)
         {

            ObjectInputStream ois = new ObjectInputStream(rs.getBlob(2).getBinaryStream());
            message = SpyMessage.readMessage(ois);
         }

         message.header.messageId = messageid;
         return message;
      }
      catch (StreamCorruptedException e)
      {
         throw new IOException("Could not load the message: " + e);
      }
   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // TX Commit
   //
   /////////////////////////////////////////////////////////////////////////////////
   public void commitPersistentTx(org.jboss.mq.pm.Tx txId) throws javax.jms.JMSException
   {

      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      try
      {

         c = datasource.getConnection();
         removeMarkedMessages(c, txId, "D");
         removeTXRecord(c, txId.longValue());

      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not commit tx: " + txId, e);
      }
      finally
      {
         try
         {
            c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();
      }
   }

   public void removeMarkedMessages(Connection c, org.jboss.mq.pm.Tx txid, String mark) throws SQLException
   {
      PreparedStatement stmt = null;
      try
      {
         stmt = c.prepareStatement(DELETE_MARKED_MESSAGES);
         if (txid != null)
            stmt.setLong(1, txid.longValue());
         else
            stmt.setNull(1, java.sql.Types.BIGINT);
         stmt.setString(2, mark);
         stmt.executeUpdate();
      }
      finally
      {
         try
         {
            stmt.close();
         }
         catch (Throwable e)
         {
         }
      }
   }

   public void removeTXRecord(Connection c, long txid) throws SQLException
   {
      PreparedStatement stmt = null;
      try
      {
         stmt = c.prepareStatement(DELETE_TX);
         stmt.setLong(1, txid);
         stmt.executeUpdate();
      }
      finally
      {
         try
         {
            stmt.close();
         }
         catch (Throwable e)
         {
         }
      }
   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // TX Rollback
   //
   /////////////////////////////////////////////////////////////////////////////////
   public void rollbackPersistentTx(org.jboss.mq.pm.Tx txId) throws JMSException
   {

      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      try
      {

         c = datasource.getConnection();
         removeMarkedMessages(c, txId, "A");
         removeTXRecord(c, txId.longValue());

      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not rollback tx: " + txId, e);
      }
      finally
      {
         try
         {
            c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();
      }

   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // TX Creation
   //
   /////////////////////////////////////////////////////////////////////////////////
   public org.jboss.mq.pm.Tx createPersistentTx() throws JMSException
   {
      org.jboss.mq.pm.Tx id = new org.jboss.mq.pm.Tx(nextTransactionId++);
      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      try
      {

         c = datasource.getConnection();
         stmt = c.prepareStatement(INSERT_TX);
         stmt.setLong(1, id.longValue());
         stmt.executeUpdate();

      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not crate tx: " + id, e);
      }
      finally
      {
         try
         {
            stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();
      }

      return id;
   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // Adding a message
   //
   /////////////////////////////////////////////////////////////////////////////////
   public void add(MessageReference messageRef, org.jboss.mq.pm.Tx txId) throws javax.jms.JMSException
   {

      // LogInfo logInfo;
      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      try
      {
         c = datasource.getConnection();
         // has it allready been stored by the message cache interface??
         if (messageRef.persistData!=null)
         {
            //update the stored record,
            markMessage(c, messageRef.messageId, (String) messageRef.persistData, txId, "A");
         }
         else
         {
            SpyMessage message = messageRef.getMessage();
            add(c, message, txId, "A");
            messageRef.setStored(((SpyDestination) message.getJMSDestination()).toString());
         }

      }
      catch (IOException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not store message: " + messageRef.messageId, e);
      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not store message: " + messageRef.messageId, e);
      }
      finally
      {
         try
         {
            stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();
      }
   }

   public void add(Connection c, SpyMessage message, org.jboss.mq.pm.Tx txId, String mark)
      throws SQLException, IOException
   {

      PreparedStatement stmt = null;
      try
      {

         stmt = c.prepareStatement(INSERT_MESSAGE);

         stmt.setLong(1, message.header.messageId);
         stmt.setString(2, ((SpyDestination) message.getJMSDestination()).toString());

         if (blobType == OBJECT_BLOB)
         {
            stmt.setObject(3, message);
         }
         else if (blobType == BYTES_BLOB)
         {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            SpyMessage.writeMessage(message,oos);
            oos.flush();
            byte[] messageAsBytes = baos.toByteArray();
            stmt.setBytes(3, messageAsBytes);
         }
         else if (blobType == BINARYSTREAM_BLOB)
         {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            SpyMessage.writeMessage(message,oos);
            oos.flush();
            byte[] messageAsBytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(messageAsBytes);
            stmt.setBinaryStream(3, bais, messageAsBytes.length);
         }
         else if (blobType == BLOB_BLOB)
         {
            
            throw new RuntimeException("BLOB_TYPE: BLOB_BLOB is not yet implemented.");
            /** TODO:
            ByteArrayOutputStream baos= new ByteArrayOutputStream();
            ObjectOutputStream oos= new ObjectOutputStream(baos);
            oos.writeObject(message);
            byte[] messageAsBytes= baos.toByteArray();
            ByteArrayInputStream bais= new ByteArrayInputStream(messageAsBytes);
            stmt.setBsetBinaryStream(3, bais, messageAsBytes.length);
            */
         }

         if (txId != null)
            stmt.setLong(4, txId.longValue());
         else
            stmt.setNull(4, java.sql.Types.BIGINT);
         stmt.setString(5, mark);

         stmt.executeUpdate();

      }
      finally
      {
         try
         {
            stmt.close();
         }
         catch (Throwable ignore)
         {
         }
      }
   }

   public void markMessage(Connection c, long messageid, String destination, org.jboss.mq.pm.Tx txId, String mark)
      throws SQLException
   {

      // LogInfo logInfo;
      PreparedStatement stmt = null;
      try
      {

         stmt = c.prepareStatement(MARK_MESSAGE);
         if (txId == null)
         {
            stmt.setNull(1, java.sql.Types.BIGINT);
         }
         else
         {
            stmt.setLong(1, txId.longValue());
         }
         stmt.setString(2, mark);
         stmt.setLong(3, messageid);
         stmt.setString(4, destination);
         stmt.executeUpdate();

      }
      finally
      {
         try
         {
            stmt.close();
         }
         catch (Throwable ignore)
         {
         }
      }

   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // Removing a message
   //
   /////////////////////////////////////////////////////////////////////////////////
   public void remove(MessageReference messageRef, org.jboss.mq.pm.Tx txId) throws javax.jms.JMSException
   {
      // LogInfo logInfo;
      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      try
      {

         c = datasource.getConnection();
         if (txId == null)
         {
            stmt = c.prepareStatement(DELETE_MESSAGE);
            stmt.setLong(1, messageRef.messageId);
            stmt.setString(2, (String) messageRef.persistData);
            int rc = stmt.executeUpdate();
            if(  rc != 1 ) 
               throw new SpyJMSException("Could not delete the message from the database: delete affected "+rc+" rows");

            // if the message is not in the DB, we need to update the message cache.
            messageRef.setStored(null);
             
         }
         else
         {
            stmt = c.prepareStatement(MARK_MESSAGE);
            stmt.setLong(1, txId.longValue());
            stmt.setString(2, "D");
            stmt.setLong(3, messageRef.messageId);
            stmt.setString(4, (String) messageRef.persistData);
            int rc = stmt.executeUpdate();
            if(  rc != 1 )
               throw new SpyJMSException("Could not mark the message as deleted in the database: update affected "+rc+" rows");
         }

      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not remove message: " + messageRef, e);
      }
      finally
      {
         try
         {
            stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();
      }

   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // Misc. PM functions
   //
   /////////////////////////////////////////////////////////////////////////////////

   public org.jboss.mq.pm.TxManager getTxManager()
   {
      return txManager;
   }

   /*
    * @see PersistenceManager#closeQueue(JMSDestination, SpyDestination)
    */
   public void closeQueue(JMSDestination jmsDest, SpyDestination dest) throws JMSException
   {
      // Nothing to clean up, all the state is in the db.
   }

   /*
    * @see CacheStore#loadFromStorage(MessageReference)
    */
   public SpyMessage loadFromStorage(MessageReference messageRef) throws JMSException
   {
      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      try
      {

         c = datasource.getConnection();
         stmt = c.prepareStatement(SELECT_MESSAGE);
         stmt.setLong(1, messageRef.messageId);
         stmt.setString(2, (String) messageRef.persistData);

         rs = stmt.executeQuery();
         if (rs.next())
            return extractMessage(rs);

         return null;

      }
      catch (IOException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not load message : " + messageRef, e);
      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not load message : " + messageRef, e);
      }
      finally
      {
         try
         {
            rs.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();
      }
   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // CacheStore Functions
   //
   /////////////////////////////////////////////////////////////////////////////////   
   public void removeFromStorage(MessageReference messageRef) throws JMSException
   {
      
      // removeFromStorage is called by the MessageCache when it need to 
      // invalidate the message from storage due change in the message headers.
      // If the message is persistent don't delete since it is the only copy we have!
      // TODO: look into renaming this method invalidate().  
      if( messageRef.getHeaders().jmsDeliveryMode == DeliveryMode.PERSISTENT )
      	return;   
      
      // LogInfo logInfo;
      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      try
      {
         c = datasource.getConnection();
         stmt = c.prepareStatement(DELETE_MESSAGE);
         stmt.setLong(1, messageRef.messageId);
         stmt.setString(2, (String) messageRef.persistData);
         stmt.executeUpdate();
         messageRef.setStored(null);
      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not remove message: " + messageRef.messageId, e);
      }
      finally
      {
         try
         {
            stmt.close();
         }
         catch (Throwable ignore)
         {
         }
         try
         {
            c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();
      }
   }

   /*
    * @see CacheStore#saveToStorage(MessageReference, SpyMessage)
    */
   public void saveToStorage(MessageReference messageRef, SpyMessage message) throws JMSException
   {
      // If the queue is being restored from the database, then the message
      // is definitely already stored, so there's no need to add it again.
      if( restoringQueue ) {
         messageRef.setStored(((SpyDestination) message.getJMSDestination()).toString());
         return;
      }

      // If the message cache invalidated the message, then it thinks it 
      // is no longer in the database.  But since persitent messages are 
      // allways in the database, we do not have to add the message again.
      if( messageRef.persistData != null ) {
         messageRef.setStored(messageRef.persistData);
         return;
      }
      
      // LogInfo logInfo;
      TransactionManagerStrategy tms = new TransactionManagerStrategy();
      tms.startTX();
      Connection c = null;
      PreparedStatement stmt = null;
      try
      {

         c = datasource.getConnection();
         add(c, message, null, "T");
         messageRef.setStored(((SpyDestination) message.getJMSDestination()).toString());

      }
      catch (IOException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not store message: " + messageRef.messageId, e);
      }
      catch (SQLException e)
      {
         tms.setRollbackOnly();
         throw new SpyJMSException("Could not store message: " + messageRef.messageId, e);
      }
      finally
      {
         try
         {
            c.close();
         }
         catch (Throwable ignore)
         {
         }
         tms.endTX();
      }
   }

   /////////////////////////////////////////////////////////////////////////////////
   //
   // JMX Interface 
   //
   /////////////////////////////////////////////////////////////////////////////////   
   private ObjectName messageCacheName;
   private ObjectName dataSourceName;
   private Properties sqlProperties = new Properties();

   public void startService() throws Exception
   {
      SELECT_ALL_UNCOMMITED_TXS = sqlProperties.getProperty("SELECT_ALL_UNCOMMITED_TXS", SELECT_ALL_UNCOMMITED_TXS);
      DELETE_ALL_MESSAGE_WITH_TX = sqlProperties.getProperty("DELETE_ALL_MESSAGE_WITH_TX", DELETE_ALL_MESSAGE_WITH_TX);
      DELETE_TX = sqlProperties.getProperty("DELETE_TX", DELETE_TX);
      DELETE_MARKED_MESSAGES = sqlProperties.getProperty("DELETE_MARKED_MESSAGES", DELETE_MARKED_MESSAGES);
      INSERT_TX = sqlProperties.getProperty("INSERT_TX", INSERT_TX);
      SELECT_MAX_TX = sqlProperties.getProperty("SELECT_MAX_TX", SELECT_MAX_TX);
      SELECT_MESSAGES_IN_DEST = sqlProperties.getProperty("SELECT_MESSAGES_IN_DEST", SELECT_MESSAGES_IN_DEST);
      SELECT_MESSAGE = sqlProperties.getProperty("SELECT_MESSAGE", SELECT_MESSAGE);
      INSERT_MESSAGE = sqlProperties.getProperty("INSERT_MESSAGE", INSERT_MESSAGE);
      MARK_MESSAGE = sqlProperties.getProperty("MARK_MESSAGE", MARK_MESSAGE);
      DELETE_MESSAGE = sqlProperties.getProperty("DELETE_MESSAGE", DELETE_MESSAGE);
      CREATE_MESSAGE_TABLE = sqlProperties.getProperty("CREATE_MESSAGE_TABLE", CREATE_MESSAGE_TABLE);
      CREATE_TX_TABLE = sqlProperties.getProperty("CREATE_TX_TABLE", CREATE_TX_TABLE);
      createTables = sqlProperties.getProperty("CREATE_TABLES_ON_STARTUP", "true").equals("true");
      String s = sqlProperties.getProperty("BLOB_TYPE", "OBJECT_BLOB");

      if (s.equals("OBJECT_BLOB"))
      {
         blobType = OBJECT_BLOB;
      }
      else if (s.equals("BYTES_BLOB"))
      {
         blobType = BYTES_BLOB;
      }
      else if (s.equals("BINARYSTREAM_BLOB"))
      {
         blobType = BINARYSTREAM_BLOB;
      }
      else if (s.equals("BLOB_BLOB"))
      {
         blobType = BLOB_BLOB;
      }

      //Find the ConnectionFactoryLoader MBean so we can find the datasource
      String dsName = (String) getServer().getAttribute(dataSourceName, "JndiName");
      //Get an InitialContext

      InitialContext ctx = new InitialContext();
      datasource = (DataSource) ctx.lookup("java:/" + dsName);

      //Get the Transaction Manager so we can control the jdbc tx
      tm = (TransactionManager) ctx.lookup(TransactionManagerService.JNDI_NAME);

      messageCache = (MessageCache) getServer().getAttribute(messageCacheName, "Instance");

      log.debug("Resolving uncommited TXS");
      resolveAllUncommitedTXs();
   }

   public Object getInstance()
   {
      return this;
   }

   public ObjectName getMessageCache()
   {
      return messageCacheName;
   }

   public void setMessageCache(ObjectName messageCache)
   {
      this.messageCacheName = messageCache;
   }

   /**
    * @jmx:managed-attribute
    */
   public ObjectName getDataSource()
   {
      return dataSourceName;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setDataSource(ObjectName dataSourceName)
   {
      this.dataSourceName = dataSourceName;
   }

   public MessageCache getMessageCacheInstance()
   {
      return messageCache;
   }

   /**
    * Gets the sqlProperties.
    *
    * @jmx:managed-attribute
    *
    * @return Returns a Properties
    */
   public String getSqlProperties()
   {
      try
      {
         ByteArrayOutputStream boa = new ByteArrayOutputStream();
         sqlProperties.store(boa, "");
         return new String(boa.toByteArray());
      }
      catch (IOException shouldnothappen)
      {
         return "";
      }
   }

   /**
    * Sets the sqlProperties.
    *
    * @jmx:managed-attribute
    *
    * @param sqlProperties The sqlProperties to set
    */
   public void setSqlProperties(String value)
   {
      try
      {

         ByteArrayInputStream is = new ByteArrayInputStream(value.getBytes());
         sqlProperties = new Properties();
         sqlProperties.load(is);

      }
      catch (IOException shouldnothappen)
      {
      }
   }

}
