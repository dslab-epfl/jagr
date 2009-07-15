/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.transaction.Transaction;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Synchronization;
import java.util.Iterator;
import java.util.Collection;
import org.jboss.logging.Logger;
import java.util.Map;
import javax.transaction.TransactionRolledbackException;

/**
 * This class provides a way to find out what entities are contained in
 * what transaction.  It is used, to find which entities to call ejbStore()
 * on when a ejbFind() method is called within a transaction. EJB 2.0- 9.6.4
 * also, it is used to synchronize on a remove.
 * Used in EntitySynchronizationInterceptor, EntityContainer
 *
 * Entities are stored in an ArrayList to ensure specific ordering. 
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20021121 Steve Coy:</b>
 * <ul>
 * <li>Fix a performance bottleneck by adding a parallel map of sets of
 *     entities so that we can check for their existence in the trx in
 *     O(log N) time instead of O(N) time.
 */
public class GlobalTxEntityMap
{

   private final Logger log = Logger.getLogger(getClass());

   protected final Map mEntitySequenceListMap = new HashMap();	// map of fifo ordered entity lists
   protected final Map mEntitySetMap = new HashMap();		// map of entity sets for quick lookups
   
   /**
    * associate entity with transaction
    */
   public synchronized void associate(Transaction tx,
                                      EntityEnterpriseContext entity)
      throws RollbackException, SystemException
   {
      ArrayList entitySequenceList = (ArrayList)mEntitySequenceListMap.get(tx);
      HashSet entitySet;
      if (entitySequenceList == null)
      {
         entitySequenceList = new ArrayList();
         entitySet = new HashSet();
         
         mEntitySequenceListMap.put(tx, entitySequenceList);
         mEntitySetMap.put(tx, entitySet);
         tx.registerSynchronization(new GlobalTxEntityMapCleanup(this, tx));
      }
      else
         entitySet = (HashSet)mEntitySetMap.get(tx);
      if (!entitySet.contains(entity))
      {
         entitySequenceList.add(entity);
         entitySet.add(entity);
      }
   }

   /**
    * sync all EntityEnterpriseContext that are involved (and changed)
    * within a transaction.
    */
   public void syncEntities(Transaction tx) 
      throws TransactionRolledbackException
   {
      Collection entities = null;
      synchronized (this)
      {
         entities = (Collection)mEntitySequenceListMap.remove(tx);
         mEntitySetMap.remove(tx);
      }
      if (entities != null) // there are entities associated
      {
         // This is an independent point of entry. We need to make sure the
         // thread is associated with the right context class loader
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         
         try
         {
            EntityEnterpriseContext ctx = null;
            try
            {
               for (Iterator i = entities.iterator(); i.hasNext(); )
               {
                        //I don't know how this could happen without an exception in the loop,
                        //but this method can get to here after e.g. NoSuchEntity...
                        //new ConnectionManager won't enlist in rolled back tx.
                        //(old one did not use enlist of XAResource for local tx)
                        if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) 
                        {
                           break;
                        } // end of if ()
                        
                  //read-only will never get into this list.
                  ctx = (EntityEnterpriseContext)i.next();
                  EntityContainer container = (EntityContainer)ctx.getContainer();
                  Thread.currentThread().setContextClassLoader(container.getClassLoader());
                  container.storeEntity(ctx);
               }
            }
            catch (Exception e)
            {
              /*ejb 1.1 section 12.3.2
               *ejb 2 section 18.3.3
               *exception during store must log exception,
               * mark tx for rollback and throw 
               * a TransactionRolledBack[Local]Exception
               */
      
               //however we may need to ignore a NoSuchEntityException -- TODO
               log.error("Store failed on entity: " + ctx.getId(), e);
               try
               {
                  tx.setRollbackOnly();
               }
               catch(SystemException se)
               {
                  log.warn("SystemException while trying to rollback tx: " + tx, se);
               }
               catch (IllegalStateException ise)
               {
                  log.warn("IllegalStateException while trying to rollback tx: " + tx, ise);
               }
               finally
               {
                  //How do we distinguish local/remote??
                  throw new TransactionRolledbackException("Exception in store of entity :" + ctx.id);
               }
            }
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(oldCl);
         }
      }
   }




   /**
    * Store changed entities to resource manager in this Synchronization
    */
   private class GlobalTxEntityMapCleanup implements Synchronization
   {
      GlobalTxEntityMap map;
      Transaction tx;

      public GlobalTxEntityMapCleanup(GlobalTxEntityMap map,
                                      Transaction tx)
      {
         this.map = map;
         this.tx = tx;
      }

      // Synchronization implementation -----------------------------
  
      public void beforeCompletion()
      {
         // complete
         boolean trace = log.isTraceEnabled();
         if( trace )
            log.trace("beforeCompletion called for tx " + tx);
         try
         {
            syncEntities(tx);
         }
         catch (Exception e)
         {//ignore - we can't throw any exceptions, it is already logged
         }

      }
  
      public void afterCompletion(int status)
      {
	 //no-op
      }
   }
   
}
