/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;



import org.jboss.ejb.Container;
import org.jboss.ejb.BeanLock;
import org.jboss.ejb.BeanLockManager;
import org.jboss.ejb.EntityContainer;
import org.jboss.invocation.Invocation;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.EntityEnterpriseContext;

/**
 * The lock interceptors role is to schedule thread wanting to invoke method on a target bean
 *
* <p>The policies for implementing scheduling (pessimistic locking etc) is implemented by pluggable
*    locks
*
* <p>We also implement serialization of calls in here (this is a spec
*    requirement). This is a fine grained notify, notifyAll mechanism. We
*    notify on ctx serialization locks and notifyAll on global transactional
*    locks.
*   
* <p><b>WARNING: critical code</b>, get approval from senior developers
*    before changing.
*    
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
* @version $Revision: 1.1.1.1 $
*
* <p><b>Revisions:</b><br>
* <p><b>2001/07/30: marcf</b>
* <ol>
*   <li>Initial revision
*   <li>Factorization of the lock out of the context in cache
*   <li>The new locking is implement as "scheduling" in the lock which allows for pluggable locks
* </ol>
* <p><b>2001/08/07: billb</b>
* <ol>
*   <li>Removed while loop and moved it to SimplePessimisticEJBLock where it belongs.
* </ol>
*/
public class EntityConcurrencyTesterInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
 
   // Attributes ----------------------------------------------------
 
   protected EntityContainer container;
 
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
 
   // Public --------------------------------------------------------
 
   public void setContainer(Container container)
   {
      this.container = (EntityContainer)container;
 
   }
 
   public Container getContainer()
   {
      return container;
   }
 
   // Interceptor implementation --------------------------------------
 
   public Object invokeHome(Invocation mi)
      throws Exception
   {  
      // Invoke through interceptors
      return getNext().invokeHome(mi);
  
   }
 
   public Object invoke(Invocation mi)
      throws Exception
   {
  
      EntityEnterpriseContext ctx = (EntityEnterpriseContext)mi.getEnterpriseContext();
      ctx.lock();
      if (ctx.getNumLocks() > 1) { log.warn("************* Num locks > 1!!!!!!!!"); }
      try 
      {
         
         return getNext().invoke(mi); 
      }
      finally
      {
         ctx.unlock();
      }
   }
}

