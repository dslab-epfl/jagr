/* $Id: ClientContainer.java,v 1.2 2003/12/14 13:18:12 candea Exp $ */

/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.proxy;

import java.io.Externalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.PayloadKey;

import roc.rr.*;

/**
 * An invocation handler whichs sets up the client invocation and
 * starts the invocation interceptor call chain.
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.2 $
 */
public class ClientContainer
   implements Externalizable, InvocationHandler
{
   /** The serialVersionUID. @since 1.5 */
   private static final long serialVersionUID = -4061374432170701306L;

   /** An empty method parameter list. */
   protected static final Object[] EMPTY_ARGS = {};

   /**
    * The <em>static</em> information that gets attached to every invocation. 
    */ 
   public InvocationContext context;
   
   /** The first interceptor in the chain. */
   public Interceptor next;
   
   /**
    * Exposed for externalization.
    */
   public ClientContainer()
   {
      super();
   }
   
   public ClientContainer(final InvocationContext context) 
   {
      this.context = context;
   }
   
   public Object invoke(final Object proxy,
                        final Method m,
                        Object[] args)
      throws Throwable
   {
      // Normalize args to always be an array
      // Isn't this a bug in the proxy call??
      if (args == null)
         args = EMPTY_ARGS;
        
      // Create the invocation object
      Invocation invocation = new Invocation();
      
      // Contextual information for the interceptors
      invocation.setInvocationContext(context);
      
      invocation.setObjectName(context.getObjectName());
      invocation.setMethod(m);
      invocation.setArguments(args);
      invocation.setValue(InvocationKey.INVOKER_PROXY_BINDING,
                          context.getInvokerProxyBinding(),
                          PayloadKey.AS_IS);
      
      // send the invocation down the client interceptor chain
      // try a call three times before giving up
      for (int nTries=3 ; true ; nTries--)  
      {
         try {
            return next.invoke( invocation );
         }
         catch( RetryLaterException rle ) {
            if( nTries > 0 )
            {
               System.out.println("+++++ ClientContainer retrying call to " + 
                                  m.getDeclaringClass().getName() + ":" + m.getName() + "()" );
               continue;
            }
            throw rle;  // if we ran out of tries, throw the exception
         }
      }
   }
   
   public Interceptor setNext(Interceptor interceptor) 
   {
      next = interceptor;
      
      return interceptor;
   }
   
   /**
    * Externalization support.
    */
   public void writeExternal(final ObjectOutput out)
      throws IOException
   {
      out.writeObject(next);
      out.writeObject(context);
   }

   /**
    * Externalization support.
    */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      next = (Interceptor) in.readObject();
      context = (InvocationContext) in.readObject();
   }
}
 
