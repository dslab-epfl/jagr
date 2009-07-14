/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.interceptor;


/**
 * Base class for all interceptors.
 *
 * @see org.jboss.mx.interceptor.StandardMBeanInterceptor
 * @see org.jboss.mx.interceptor.LogInterceptor
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *   
 */
public class Interceptor
{

   // Attributes ----------------------------------------------------
   protected Interceptor next = null;
   protected String name = null;

   // Constructors --------------------------------------------------
   public Interceptor(String name)
   {
      this.name = name;
   }

   // Public --------------------------------------------------------   
   public Object invoke(Invocation invocation) throws InvocationException
   {
      return getNext().invoke(invocation);
   }

   public Interceptor getNext()
   {
      return next;
   }

   public void insert(Interceptor interceptor)
   {
      Interceptor ic = interceptor;
      while(ic.getNext() != null)
         ic = ic.getNext();

      ic.next = next;
      next = interceptor;
   }

   public void insertLast(Interceptor  interceptor)
   {
      if (next == null) {
         next = interceptor;
         return;
      }
         
      Interceptor ic = next;
      while(ic.getNext() != null)
         ic = ic.getNext();
      ic.next = interceptor;
   }
}


