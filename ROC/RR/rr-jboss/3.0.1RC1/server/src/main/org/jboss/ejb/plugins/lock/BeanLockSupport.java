/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.lock;

import java.lang.reflect.Method;

import javax.transaction.Transaction;
import javax.ejb.EJBObject;

import org.jboss.ejb.BeanLock;
import org.jboss.ejb.plugins.lock.Entrancy;
import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.logging.Logger;
import java.util.HashMap;
import java.util.HashSet;


/**
 * Support for the BeanLock
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.1.1.1 $
 */
public abstract class BeanLockSupport
   implements BeanLock
{
   protected Container container = null;
   /**
    * Number of threads that retrieved this lock from the manager
    * (0 means removing)
    */ 
   protected int refs = 0;
   
   /** The Cachekey corresponding to this Bean */
   protected Object id = null;
 
   /** Logger instance */
   static Logger log = Logger.getLogger(BeanLock.class);
 
   /** Transaction holding lock on bean */
   private Transaction tx = null;
   
   /** Thread holding lock on bean */
   protected Thread holdingThread = null;
 
   protected boolean synched = false;

   protected int txTimeout;

	
   public void setId(Object id) { this.id = id;}
   public Object getId() { return id;}
   public void setTimeout(int timeout) {txTimeout = timeout;}
   public void setContainer(Container container) { this.container = container; }
	
   public void sync()
   {
      synchronized(this)
      {
         while(synched)
         {
            try
            {
               this.wait();
            }
            catch (InterruptedException ex) 
            {
               /* ignored */
            }
         }
         synched = true;
      }
   }
 
   public void releaseSync()
   {
      synchronized(this)
      {
         synched = false;
         this.notify();
      }
   }
 
   public abstract void schedule(Invocation mi) throws Exception;
	
   /**
    * The setTransaction associates a transaction with the lock.
    * The current transaction is associated by the schedule call.
    */
   public void setTransaction(Transaction tx){this.tx = tx;}
   public Transaction getTransaction(){return tx;}
   
   public abstract void endTransaction(Transaction tx);
   public abstract void wontSynchronize(Transaction tx);
	
   public abstract void endInvocation(Invocation mi);
   
   public void addRef() { refs++;}
   public void removeRef() { refs--;}
   public int getRefs() { return refs;}
   
   // Private --------------------------------------------------------
   
   // This following is for deadlock detection
   protected static HashMap waiting = new HashMap();

   public void deadlockDetection(Transaction miTx) throws Exception
   {
      if (Thread.currentThread().equals(holdingThread))
      {
         throw new ApplicationDeadlockException("Application deadlock detected: Current thread already has tx lock in different transaction.");
      }
      if (miTx == null) return;

      HashSet set = new HashSet();
      set.add(miTx);
      
      Object checkTx = this.tx;
      synchronized(waiting)
      {
	 while (checkTx != null)
	 {
	    Object waitingFor = waiting.get(checkTx);
	    if (waitingFor != null)
	    { 
	       if (set.contains(waitingFor))
	       {
		  log.error("Application deadlock detected: " + miTx + " has deadlock conditions.  Two or more transactions contending for same resources and each have locks eachother need.");
		  throw new ApplicationDeadlockException("Application deadlock detected: Two or more transactions contention.");
	       }
	       set.add(waitingFor);
	    }
	    checkTx = waitingFor;
	 }
      }
   }
}
