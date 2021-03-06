package org.jboss.ejb.plugins.local;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/** The EJBLocal proxy for a stateful session

 @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 @version $Revision: 1.1.1.1 $
 */
class StatefulSessionProxy extends LocalProxy
   implements InvocationHandler
{
   static final long serialVersionUID = -3113762511947535929L;
   private Object id;

   StatefulSessionProxy(String jndiName, Object id, BaseLocalContainerInvoker invoker)
   {
      super(jndiName, invoker);
      this.id = id;
   }

   protected Object getId()
   {
      return id;
   }

   public final Object invoke(final Object proxy, final Method m,
      Object[] args)
      throws Throwable
   {
      if (args == null)
         args = EMPTY_ARGS;

      Object retValue = super.invoke( proxy, m, args );
      if (retValue != null)
         return retValue;
      // If not taken care of, go on and call the container
      else
      {
         return invoker.invoke(id, m, args);
      }
   }
}
