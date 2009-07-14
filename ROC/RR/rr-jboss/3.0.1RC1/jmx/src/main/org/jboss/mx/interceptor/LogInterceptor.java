/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.interceptor;

/**
 * Log interceptor.
 *
 * @see org.jboss.mx.interceptor.Interceptor
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *   
 */
public final class LogInterceptor
   extends Interceptor
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public LogInterceptor() {
      super("Log Interceptor");
   }
   
   // Interceptor overrides -----------------------------------------
   public Object invoke(Invocation invocation) throws InvocationException 
   {
      return getNext().invoke(invocation);
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
      



