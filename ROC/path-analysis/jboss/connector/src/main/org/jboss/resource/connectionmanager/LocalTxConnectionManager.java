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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.jboss.system.Registry;

/**
 * The LocalTxConnectionManager is a JBoss ConnectionManager 
 * implementation for jca adapters implementing LocalTransaction support. 
 * It implements a ConnectionEventListener that implements XAResource to 
 * manage transactions through the Transaction Manager. To assure that all 
 * work in a local transaction occurs over the same ManagedConnection, it 
 * includes a xid to ManagedConnection map.  When a Connection is requested 
 * or a transaction started with a connection handle in use, it checks to 
 * see if a ManagedConnection already exists enrolled in the global 
 * transaction and uses it if found. Otherwise a free ManagedConnection 
 * has its LocalTransaction started and is used.  From the 
 * BaseConnectionManager2, it includes functionality to obtain managed 
 * connections from 
 * a ManagedConnectionPool mbean, find the Subject from a SubjectSecurityDomain, 
 * and interact with the CachedConnectionManager for connections held over 
 * transaction and method boundaries.  Important mbean references are to a 
 * ManagedConnectionPool supplier (typically a JBossManagedConnectionPool), and a 
 * RARDeployment representing the ManagedConnectionFactory.
 *
 *
 *
 * This connection manager has to perform the following operations:
 *
 * 1. When an application component requests a new ConnectionHandle,
 *    it must find a ManagedConnection, and make sure a
 *    ConnectionEventListener is registered. It must inform the
 *    CachedConnectionManager that a connection handle has been given
 *    out. It needs to count the number of handles for each
 *    ManagedConnection.  If there is a current transaction, it must
 *    enlist the ManagedConnection's LocalTransaction in the transaction 
 *    using the ConnectionEventListeners XAResource XAResource implementation.
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
 * Created: Thurs March 21 11:13:28 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $$
 *
 *
 * @jmx:mbean name="jboss.jca:service=LocalTxConnectionManager"
 *            extends="BaseConnectionManager2MBean"
 */

public class LocalTxConnectionManager 
   extends BaseConnectionManager2 
   implements LocalTxConnectionManagerMBean 
{

   private String tmName;
   private TransactionManager tm;

   private final Map txToManagedConnectionMap = new HashMap();

   /**
    * Default managed LocalTxConnectionManager constructor for mbean instances.
    * @jmx:managed-constructor
    */
   public LocalTxConnectionManager()
   {
   }
   
   /**
    * Creates a new <code>LocalTxConnectionManager</code> instance.
    *for TESTING ONLY!!! not a managed constructor!!
    * @param mcf a <code>ManagedConnectionFactory</code> value
    * @param ccm a <code>CachedConnectionManager</code> value
    * @param poolingStrategy a <code>ManagedConnectionPool</code> value
    * @param tm a <code>TransactionManager</code> value
    */
   public LocalTxConnectionManager (final ManagedConnectionFactory mcf,
                                    final CachedConnectionManager ccm,
                                    final ManagedConnectionPool poolingStrategy, 
                                    final TransactionManager tm)
   {
      super(mcf, ccm, poolingStrategy); 
      this.tm = tm;     
   }
   
   /**
    *  The TransactionManager attribute contains the jndi name of the 
    * TransactionManager.  This is normally java:/TransactionManager.
    *
    * @param name an <code>String</code> value
    * @jmx:managed-attribute
    */
   public void setTransactionManager(final String tmName)
   {
      this.tmName = tmName;
   }

   /**
    * Describe <code>getTransactionManager</code> method here.
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


   public ManagedConnection getManagedConnection(Subject subject, ConnectionRequestInfo cri) 
      throws ResourceException
   {
      try 
      {
         if (tm.getStatus() != Status.STATUS_NO_TRANSACTION)
         {
            Transaction tx = tm.getTransaction();
            ManagedConnection mc = null;
            synchronized (txToManagedConnectionMap)
            {
               mc = (ManagedConnection)txToManagedConnectionMap.get(tx);
            }
            if (mc != null) 
            {
               if (log.isTraceEnabled()) 
               {
                  log.trace("getManagedConnection returning connection " + mc + " already associated with tx " + tx);
               } // end of if ()
               return mc;
            } // end of if ()
         } // end of if ()
      }
      catch (SystemException xae)
      {
         throw new ResourceException("couldn't find current tx" + xae);
      } // end of try-catch
      if (log.isTraceEnabled()) 
      {
         log.info("getManagedConnection returning unassociated connection");
      } // end of if ()
      
      return super.getManagedConnection(subject, cri);      
   }

   protected ConnectionListener registerConnectionEventListener(ManagedConnection mc) throws ResourceException
   {
      ConnectionListener cli = new LocalConnectionEventListener(mc);
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
            LocalConnectionEventListener cel = (LocalConnectionEventListener)getConnectionEventListener(mc);
            cel.enlist();
         } // end of if ()
         
      } // end of for ()
      mcs.clear();//maybe help the gc.
   }

   protected void managedConnectionReconnected(ManagedConnection mc) throws ResourceException
   {
      LocalConnectionEventListener cel = (LocalConnectionEventListener)getConnectionEventListener(mc);
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
      LocalConnectionEventListener cel = (LocalConnectionEventListener)getConnectionEventListener(mc);
      if (cel == null) 
      {
         throw new IllegalStateException("ManagedConnection with no ConnectionEventListener! " + mc);
      } // end of if ()
      cel.delist();
      //if there are no more handles and tx is complete, we can return to pool.
      if (cel.isManagedConnectionFree()) 
      {
         returnManagedConnection(mc, false);
      } // end of if ()
      
   }


   // implementation of javax.resource.spi.ConnectionEventListener interface
   //there is one of these for each ManagedConnection instance.  It lives as long as the ManagedConnection.
   private class LocalConnectionEventListener 
      extends BaseConnectionManager2.BaseConnectionEventListener
      implements XAResource
   {
   
      private Transaction currentTx;
      private Xid currentXid;

      //private Collection knownTx = new ArrayList();
   
      public LocalConnectionEventListener(ManagedConnection mc) throws ResourceException
      {
         super(mc);
      }
   
      public void enlist() throws SystemException
      {
         if (tm.getStatus() != Status.STATUS_NO_TRANSACTION)
         {
            Transaction newCurrentTx = tm.getTransaction();
            if (currentTx != null && currentTx != newCurrentTx) 
            {
               log.warn("in Enlisting tx, trying to change tx. illegal state: old: " + currentTx + ", new: " + newCurrentTx + ", cel: " + this);
               throw new IllegalStateException("Trying to change Tx in enlist!");
            } // end of if ()
            currentTx = newCurrentTx;
            if (log.isTraceEnabled()) 
            {
               log.info("enlisting currenttx: " + currentTx + ", cel: " + this);
            } // end of if ()

         } // end of if ()
         if (currentTx != null) 
         {
            try 
            {
               currentTx.enlistResource(this);
            }
            catch (SystemException se)
            {
               throw new SystemException("Could not get XAResource from ManagedConnection!" + se);
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
         if (log.isTraceEnabled()) 
         {
            log.trace("delisting currenttx: " + currentTx + ", cel: " + this);
         } // end of if ()
         
         try
         {
            if (tm.getStatus() != Status.STATUS_NO_TRANSACTION) 
            {
               tm.getTransaction().delistResource(this, XAResource.TMSUSPEND);
            } // end of if ()
         }
         catch (SystemException se)
         {
            throw new ResourceException("SystemException in delist!" + se);  
         } // end of try-catch
      }

   
      /**
       *
       * @param param1 <description>
       */
      public void connectionClosed(ConnectionEvent ce)
      {
         log.trace("connectionClosed called");
         if (this.getManagedConnection() != (ManagedConnection)ce.getSource()) 
         {
            throw new IllegalArgumentException("ConnectionClosed event received from wrong ManagedConnection! Expected: " + this.getManagedConnection() + ", actual: " + ce.getSource());
         } // end of if ()
         //log.trace("about to call unregisterConnection");
         try 
         {
            getCcm().unregisterConnection(LocalTxConnectionManager.this, ce.getConnectionHandle());         }
         catch (Throwable t)
         {
            log.info("throwable from unregister connection", t);
         } // end of try-catch
         
         //log.trace("unregisterConnection returned from");
         try 
         {
            //log.trace("about to call unregisterAssociation");
            unregisterAssociation(this.getManagedConnection(), ce.getConnectionHandle());
            if (isManagedConnectionFree())
            {
               //log.trace("called unregisterAssociation, delisting");
               //no more handles
               delist();
               //log.trace("called unregisterAssociation, returning");
               returnManagedConnection(this.getManagedConnection(), false);
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
         try
         {
            this.getManagedConnection().getLocalTransaction().rollback();
         }
         catch (Throwable t)
         {
            //ignore
         }
         if (currentTx != null)
         {
            synchronized(txToManagedConnectionMap)
            {
               txToManagedConnectionMap.remove(currentTx);
            }
         }
         currentXid = null;
         currentTx = null;

         //Maybe we should unregister every handle? Not really critical I think.
         unregisterConnections();
         //getCcm().unregisterConnection(LocalTxConnectionManager.this, ce.getConnectionHandle());
         ManagedConnection mc = (ManagedConnection)ce.getSource();
         returnManagedConnection(mc, true);
      }

      // implementation of javax.transaction.xa.XAResource interface

      /**
       *
       * @param param1 <description>
       * @param param2 <description>
       * @exception javax.transaction.xa.XAException <description>
       */
      public void start(Xid xid, int flags) throws XAException 
      {
         if (currentTx == null) 
         {
            throw new IllegalStateException("start called, but no currentTx");
         } // end of if ()
         
         if (log.isTraceEnabled())
         {
            log.trace("start, xid: " + xid + ", flags: " + flags);
         } // end of if ()
         if (currentXid  != null && flags == XAResource.TMNOFLAGS) 
         {
            throw new XAException("Trying to start a new tx when old is not complete! old: " + currentXid  + ", new " + xid + ", flags " + flags);
         } // end of if ()
         if (currentXid  == null && flags != XAResource.TMNOFLAGS) 
         {
            throw new XAException("Trying to start a new tx with wrong flags!  new " + xid + ", flags " + flags);
         } // end of if ()
         if (currentXid == null) 
         {
            try 
            {
               this.getManagedConnection().getLocalTransaction().begin();
            }
            catch (ResourceException re)
            {
               throw new XAException("Error trying to start local tx: " + re);
            } // end of try-catch
            
            currentXid = xid;
            synchronized(txToManagedConnectionMap)
            {
               txToManagedConnectionMap.put(currentTx, this.getManagedConnection());
            }
         } // end of if ()
      }

      /**
       *
       * @param param1 <description>
       * @param param2 <description>
       * @exception javax.transaction.xa.XAException <description>
       */
      public void end(Xid xid, int flags) throws XAException 
      {
         if (log.isTraceEnabled()) 
         {
            log.trace("end on xid: " + xid + " called with flags " + flags);
         } // end of if ()
         
         //nothing to do
      }

      /**
       *
       * @param param1 <description>
       * @param param2 <description>
       * @exception javax.transaction.xa.XAException <description>
       */
      public void commit(Xid xid, boolean onePhase) throws XAException 
      {
         if (currentTx == null) 
         {
            throw new IllegalStateException("commit called but no currentTx");
         } // end of if ()
         
         if (xid != currentXid) 
         {
            throw new XAException("wrong xid in commit: expected: " + currentXid + ", got: " + xid);
         } // end of if ()
         synchronized(txToManagedConnectionMap)
         {
            txToManagedConnectionMap.remove(currentTx);
         }
         currentXid = null;
         currentTx = null;
         try 
         {
            this.getManagedConnection().getLocalTransaction().commit();
         }
         catch (ResourceException re)
         {
            returnManagedConnection(this.getManagedConnection(), true);
            if (log.isTraceEnabled())
            {
               log.trace("commit problem: ", re);
            }
            throw new XAException("could not commit local tx" + re);
         } // end of try-catch   
         if (isManagedConnectionFree())
         {
            returnManagedConnection(this.getManagedConnection(), false);
         }
      }

      /**
       *
       * @param param1 <description>
       * @exception javax.transaction.xa.XAException <description>
       */
      public void forget(Xid xid) throws XAException 
      {
         throw new XAException("forget not supported in local tx");
      }

      /**
       *
       * @return <description>
       * @exception javax.transaction.xa.XAException <description>
       */
      public int getTransactionTimeout() throws XAException 
      {
         // TODO: implement this javax.transaction.xa.XAResource method
         return 0;
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       * @exception javax.transaction.xa.XAException <description>
       */
      public boolean isSameRM(XAResource xaResource) throws XAException 
      {
        return xaResource == this;
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       * @exception javax.transaction.xa.XAException <description>
       */
      public int prepare(Xid xid) throws XAException 
      {
         log.warn("prepare called on a local tx. You are not getting the semantics you expect!");
         return XAResource.XA_OK;//??????
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       * @exception javax.transaction.xa.XAException <description>
       */
      public Xid[] recover(int flag) throws XAException 
      {
         throw new XAException("no recover with local-tx only resource managers");
      }

      /**
       *
       * @param param1 <description>
       * @exception javax.transaction.xa.XAException <description>
       */
      public void rollback(Xid xid) throws XAException 
      {
         if (currentTx == null) 
         {
            throw new IllegalStateException("rollback called but no current tx");
         } // end of if ()
         
         if (xid != currentXid) 
         {
            throw new XAException("wrong xid in rollback: expected: " + currentXid + ", got: " + xid);
         } // end of if ()
         synchronized(txToManagedConnectionMap)
         {
            txToManagedConnectionMap.remove(currentTx);
         }
         currentXid = null;
         currentTx = null;
         try 
         {
            this.getManagedConnection().getLocalTransaction().rollback();
         }
         catch (ResourceException re)
         {
            returnManagedConnection(this.getManagedConnection(), true);
            if (log.isTraceEnabled())
            {
               log.trace("rollback problem: ", re);
            }
            throw new XAException("could not rollback local tx" + re);
         } // end of try-catch   
         if (isManagedConnectionFree())
         {
            returnManagedConnection(this.getManagedConnection(), false);
         }
      }

      /**
       *
       * @param param1 <description>
       * @return <description>
       * @exception javax.transaction.xa.XAException <description>
       */
      public boolean setTransactionTimeout(int seconds) throws XAException {
         // TODO: implement this javax.transaction.xa.XAResource method
         return false;
      }

      //Important method!!
      public boolean isManagedConnectionFree()
      {
         if (currentTx != null) 
         {
            return false;
         } // end of if ()
         return super.isManagedConnectionFree();
      }

   }//end of LocalConnectionEventListener.


}//
