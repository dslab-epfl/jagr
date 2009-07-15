
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.resource.connectionmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import org.jboss.system.Registry;

/**
 *
 * The XATxConnectionManager connection manager has to perform the following operations:
 *
 * 1. When an application component requests a new ConnectionHandle,
 *    it must find a ManagedConnection, and make sure a
 *    ConnectionEventListener is registered. It must inform the
 *    CachedConnectionManager that a connection handle has been given
 *    out. It needs to count the number of handles for each
 *    ManagedConnection.  If there is a current transaction, it must
 *    enlist the ManagedConnection's XAResource in the transaction.
 * Entry point: ConnectionManager.allocateConnection.
 * written.
 *
 * 2. When a ConnectionClosed event is received from the
 *    ConnectionEventListener, it must reduce the handle count.  If
 *    the handle count is zero, the XAResource should be delisted from
 *    the Transaction, if any. The CachedConnectionManager must be
 *    notified that the connection is closed.
 * Entry point: ConnectionEventListener.ConnectionClosed.
 * written
 *
 *3. When a transaction begun notification is received from the
 * UserTransaction (via the CachedConnectionManager, all
 * managedConnections associated with the current object must be
 * enlisted in the transaction.
 *  Entry point: (from
 * CachedConnectionManager)
 * ConnectionCacheListener.transactionStarted(Transaction,
 * Collection). The collection is of ConnectionRecord objects.
 * written.
 *
 * 4. When a synchronization beforeCompletion event is received, any
 *    enlisted XAResources must be delisted.
 * Entry point: Synchronization.beforeCompletion() (implemented in
 * XAConnectionEventListener))
 * written.
 *
 * 5. When an "entering object" notification is received from the
 * CachedConnectionInterceptor, all the connections for the current
 * object must be associated with a ManagedConnection.  if there is a
 * Transaction, the XAResource must be enlisted with it. 
 *  Entry point: ConnectionCacheListener.reconnect(Collection conns) The Collection
 * is of ConnectionRecord objects.
 * written.
 *
 * 6. When a "leaving object" notification is received from the
 * CachedConnectionInterceptor, all the managedConnections for the
 * current object must have their XAResources delisted from the
 * current Transaction, if any, and cleanup called on each
 * ManagedConnection.
 * Entry point: ConnectionCacheListener.disconnect(Collection conns).
 * written.
 *
 * In addition it inherits behavior from BaseConnectionManager2,  including
 *  functionality to obtain managed connections from 
 * a ManagedConnectionPool mbean, find the Subject from a SubjectSecurityDomain, 
 * and interact with the CachedConnectionManager for connections held over 
 * transaction and method boundaries.  Important mbean references are to a 
 * ManagedConnectionPool supplier (typically a JBossManagedConnectionPool), and a 
 * RARDeployment representing the ManagedConnectionFactory.
 * 
 * Created: Sat Jan 12 11:13:28 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $$
 *
 * @jmx:mbean name="jboss.jca:service=XATxConnectionManager"
 *            extends="BaseConnectionManager2MBean"
 */

public class XATxConnectionManager 
   extends BaseConnectionManager2 implements XATxConnectionManagerMBean 
{

   private String tmName;
   private TransactionManager tm;

   /**
    * Default managed XATxConnectionManager constructor for mbean instances.
    * @jmx:managed-constructor
    */
   public XATxConnectionManager()
   {
   }
   
   /**
    * Creates a new <code>XATxConnectionManager</code> instance.
    *for TESTING ONLY!!! not a managed constructor!!
    * @param mcf a <code>ManagedConnectionFactory</code> value
    * @param ccm a <code>CachedConnectionManager</code> value
    * @param poolingStrategy a <code>ManagedConnectionPool</code> value
    * @param tm a <code>TransactionManager</code> value
    */
   public XATxConnectionManager (final ManagedConnectionFactory mcf,
                                 final CachedConnectionManager ccm,
                                 final ManagedConnectionPool poolingStrategy, 
                                 final TransactionManager tm)
   {
      super(mcf, ccm, poolingStrategy); 
      this.tm = tm;     
   }
   
   /**
    * The TransactionManager attribute holds the jndi name of the 
    * TransactionManager. This is normally java:/TransactionManager
    *
    * @param name The JNDI name the transaction manager is bound under.
    * @jmx:managed-attribute
    */
   public void setTransactionManager(final String tmName)
   {
      this.tmName = tmName;
   }

   /**
    *
    * @return an <code>String</code> value
    * @jmx:managed-attribute
    */
   public String getTransactionManager()
   {
      return this.tmName;
   }

   protected void startService() throws Exception
   {
      this.tm = (TransactionManager)new InitialContext().lookup(tmName);
      
      super.startService();
   }

   protected void stopService() throws Exception
   {
      this.tm = null;
      super.stopService();
   }

   protected ConnectionListener registerConnectionEventListener(ManagedConnection mc) throws ResourceException
   {
      ConnectionListener cli = new XAConnectionEventListener(mc);
      mc.addConnectionEventListener(cli);
      return cli;
   }
   //reimplementation from ConnectionCacheListener interface.

   public void transactionStarted(Collection crs) throws SystemException
   {
      Set mcs = new HashSet();
      for (Iterator i = crs.iterator(); i.hasNext(); )
      {
         ConnectionRecord cr = (ConnectionRecord)i.next();
         ManagedConnection mc = cr.mc;
         if (!mcs.contains(mc)) 
         {
            mcs.add(mc);
            XAConnectionEventListener cel = (XAConnectionEventListener)getConnectionEventListener(mc);
            cel.enlist();
         } // end of if ()
         
      } // end of for ()
      mcs.clear();//maybe help the gc.
   }

   protected void managedConnectionReconnected(ManagedConnection mc) throws ResourceException
   {
      XAConnectionEventListener cel = (XAConnectionEventListener)getConnectionEventListener(mc);
      try 
      {
         cel.enlist();
      }
      catch (SystemException se)
      {
         log.info("Could not enlist in transaction on entering meta-aware object!", se);
         throw new ResourceException("Could not enlist in transaction on entering meta-aware object!" + se);
      } // end of try-catch
      
   }

   protected void managedConnectionDisconnected(ManagedConnection mc) throws ResourceException
   {
      XAConnectionEventListener cel = (XAConnectionEventListener)getConnectionEventListener(mc);
      if (cel == null) 
      {
         throw new IllegalStateException("ManagedConnection with no ConnectionEventListener! " + mc);
      } // end of if ()
      cel.delist();
      //if there are no more handles, we can return to pool.
      if (cel.isManagedConnectionFree()) 
      {
         returnManagedConnection(mc, false);
      } // end of if ()
      
   }


   // implementation of javax.resource.spi.ConnectionEventListener interface
   //there is one of these for each ManagedConnection instance.  It lives as long as the ManagedConnection.
   private class XAConnectionEventListener 
      extends BaseConnectionManager2.BaseConnectionEventListener
   {
   
      private Transaction currentTx;

      //private Collection knownTx = new ArrayList();
   
      public XAConnectionEventListener(ManagedConnection mc) throws ResourceException
      {
         super(mc);
      }
   
      public void enlist() throws SystemException
      {
         if (currentTx != null) 
         {
            log.warn("in Enlisting tx, illegal state: " + currentTx);
            
            throw new IllegalStateException("Can't enlist - already a tx!");
         } // end of if ()
         
         if (tm.getStatus() != Status.STATUS_NO_TRANSACTION)
         {
            currentTx = tm.getTransaction();
         } // end of if ()
         if (currentTx != null) 
         {
            try 
            {
               XAResource xar = getManagedConnection().getXAResource();
               currentTx.enlistResource(xar);
            }
            catch (ResourceException re)
            {
               throw new SystemException("Could not get XAResource from ManagedConnection!" + re);
            } // end of try-catch
            catch (RollbackException re)
            {
               log.info("Could not enlist XAResource!", re);
               throw new SystemException("Could not enlist XAResource!" + re);
            } // end of catch
         
         } // end of if ()
      
      }
   
      public void delist() throws ResourceException
      {
         try
         {
            if (tm.getStatus() != Status.STATUS_NO_TRANSACTION) 
            {
               tm.getTransaction().delistResource(getManagedConnection().getXAResource(),
                                                  XAResource.TMSUCCESS);
                                                  
            } // end of if ()
         }
         catch (SystemException se)
         {
            throw new ResourceException("SystemException in delist!" + se);  
         } // end of try-catch
         if (log.isTraceEnabled()) 
         {
            log.info("about to set currentTx null, managedCOnnection: " + getManagedConnection() + ", thread: " + Thread.currentThread());
         } // end of if ()
         
         currentTx = null;
      }

   
      /**
       *
       * @param param1 <description>
       */
      public void connectionClosed(ConnectionEvent ce)
      {
         log.trace("connectionClosed called");
         if (getManagedConnection() != (ManagedConnection)ce.getSource()) 
         {
            throw new IllegalArgumentException("ConnectionClosed event received from wrong ManagedConnection! Expected: " + getManagedConnection() + ", actual: " + ce.getSource());
         } // end of if ()
         //log.trace("about to call unregisterConnection");
         try 
         {
            getCcm().unregisterConnection(XATxConnectionManager.this, ce.getConnectionHandle());         }
         catch (Throwable t)
         {
            log.info("throwable from unregister connection", t);
         } // end of try-catch
         
         //log.trace("unregisterConnection returned from");
         try 
         {
            //log.trace("about to call unregisterAssociation");
            unregisterAssociation(getManagedConnection(), ce.getConnectionHandle());
            if (isManagedConnectionFree())
            {
               //log.trace("called unregisterAssociation, delisting");
               //no more handles
               delist();
               //log.trace("called unregisterAssociation, returning");
               returnManagedConnection(getManagedConnection(), false);
            }
            //log.trace("called unregisterAssociation");
         }
         catch (ResourceException re)
         {
            log.error("ResourceException while closing connection handle!", re);
         } // end of try-catch
      
      }

      /**
       *
       * @param param1 <description>
       */
      public void localTransactionStarted(ConnectionEvent ce)
      {
         if (currentTx != null) 
         {
            throw new IllegalStateException("Attempt to start local transaction while xa transaction is active!");
         } // end of if ()
         
      }
      
      /**
       *
       * @param param1 <description>
       */
      public void localTransactionCommitted(ConnectionEvent ce)
      {
         if (currentTx != null) 
         {
            throw new IllegalStateException("Attempt to commit local transaction while xa transaction is active!");
         } // end of if ()
      }
      
      /**
       *
       * @param param1 <description>
       */
      public void localTransactionRolledback(ConnectionEvent ce)
      {
         if (currentTx != null) 
         {
            throw new IllegalStateException("Attempt to roll back local transaction while xa transaction is active!");
         } // end of if ()
      }
      
      /**
       *
       * @param param1 <description>
       */
      public void connectionErrorOccurred(ConnectionEvent ce)
      {
         //Maybe we should unregister every handle? Not really critical I think.
         getCcm().unregisterConnection(XATxConnectionManager.this, ce.getConnectionHandle());
         ManagedConnection mc = (ManagedConnection)ce.getSource();
         returnManagedConnection(mc, true);
      }
   }//end of XAConnectionEventListener.
}//
