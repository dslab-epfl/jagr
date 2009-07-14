/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.security;

import java.lang.reflect.Method;
import java.util.HashMap;
import javax.ejb.EJBContext;

/**
 * An abstract implementation of SecurityProxy that wraps a non-SecurityProxy
 * object. Subclasses of this class are used to create a SecurityProxy given
 * a security delegate that implements methods in the EJB home or remote
 * interface for security checks. This allows custom security classes to be
 * written without using a JBoss specific interface. It also allows the security
 * delegate to follow a natural proxy pattern implementation.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public abstract class AbstractSecurityProxy implements SecurityProxy
{
   private HashMap methodMap;
   private Method setContextMethod;
   private Method setBeanMethod;
   protected Object delegate;
   /**
    * Flag which sets whether the method mapping will be performed in a "strict"
    * fashion. The proxy delegate must provide an implementation of all methods.
    * If set to 'true' (the default), a security exception will be thrown during
    * initialisation if a method is found for which the delegate doesn't have
    * a matching method.
    */
   protected boolean strict = true;

   AbstractSecurityProxy(Object delegate)
   {
      this.delegate = delegate;
      methodMap = new HashMap();
   }

   /**
    * Subclasses implement this method to actually invoke the given home
    * method on the proxy delegate.
    *
    * @param m, the delegate method that was mapped from the ejb home method.
    * @param args, the method invocation arguments.
    * @param delegate, the proxy delegate object associated with the AbstractSecurityProxy
    * 
    * @see invokeHome(Method, Object[])
    */
   protected abstract void invokeHomeOnDelegate(Method m, Object[] args, Object delegate) throws SecurityException;

   /**
    * Subclasses implement this method to actually invoke the given remote
    * method on the proxy delegate.
    *
    * @param m, the delegate method that was mapped from the ejb remote method.
    * @param args, the method invocation arguments.
    * @param delegate, the proxy delegate object associated with the AbstractSecurityProxy
    * 
    * @see invoke(Method, Object[], Object)
    */
   protected abstract void invokeOnDelegate(Method m, Object[] args, Object delegate) throws SecurityException;

   /**
    * This method is called by the container SecurityInterceptor to intialize
    * the proxy with the EJB home and remote interface classes that the
    * container is housing. This method creates a mapping from the home and
    * remote classes to the proxy delegate instance. The mapping is based on
    * method name and paramter types. In addition, the proxy delegate is
    * inspected for a setEJBContext(EJBContext) and a setBean(Object) method
    * so that the active EJBContext and EJB instance can be passed to the
    * delegate prior to method invocations.
    *
    * @param beanHome, the class for the EJB home interface
    * @param beanRemote, the class for the EJB remote interface
    * @param securityMgr, The security manager instance assigned to the container.
    * It is not used by this class.
    */
   public void init(Class beanHome, Class beanRemote, Object securityMgr) throws InstantiationException
   {
      mapHomeMethods(beanHome);
      mapRemoteMethods(beanRemote);
      try
      {
         Class[] parameterTypes = {EJBContext.class};
         setContextMethod = delegate.getClass().getMethod("setEJBContext", parameterTypes);
      }
      catch(Exception e)
      {
      }
      try
      {
         Class[] parameterTypes = {Object.class};
         setBeanMethod = delegate.getClass().getMethod("setBean", parameterTypes);
      }
      catch(Exception e)
      {
      }
   }

   /**  */
   public void setEJBContext(EJBContext ctx)
   {
      if(setContextMethod != null)
      {
         Object[] args = {ctx};
         try
         {
            setContextMethod.invoke(delegate, args);
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }
      }
   }

   /**
    * Called by the SecurityProxyInterceptor to allow the proxy delegate to perform
    * a security check of the indicated home interface method.
    *
    * @param m, the EJB home interface method
    * @param args, the method arguments
    */
   public void invokeHome(final Method m, Object[] args) throws SecurityException
   {
      Method delegateMethod = (Method)methodMap.get(m);
      if( delegateMethod != null )
         invokeHomeOnDelegate(delegateMethod, args, delegate);
   }

   /**
    * Called by the SecurityProxyInterceptor to allow the proxy delegate to perform
    * a security check of the indicated remote interface method.
    * @param m, the EJB remote interface method
    * @param args, the method arguments
    * @param bean, the EJB bean instance
    */
   public void invoke(final Method m, final Object[] args, final Object bean) throws SecurityException
   {
      Method delegateMethod = (Method)methodMap.get(m);
      if( delegateMethod != null )
      {
         if( setBeanMethod != null )
         {
            Object[] bargs = {bean};
            try
            {
               setBeanMethod.invoke(delegate, bargs);
            }
            catch(Exception e)
            {
               e.printStackTrace();
               throw new SecurityException("Failed to set bean on proxy" + e.getMessage());
            }
         }
         invokeOnDelegate(delegateMethod, args, delegate);
      }
   }

   /**
    * Performs a mapping from the methods declared in the beanHome class to the proxy delegate class.
    */
   protected void mapHomeMethods(Class beanHome)
   {
      Class delegateClass = delegate.getClass();
      Method[] methods = beanHome.getMethods();
      for(int m = 0; m < methods.length; m++)
      {
       // Check for ejbCreate... methods
         Method hm = methods[m];
         Class[] parameterTypes = hm.getParameterTypes();
         String name = hm.getName();
         name = "ejb" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
         try
         {
            Method match = delegateClass.getMethod(name, parameterTypes);
            methodMap.put(hm, match);
         }
         catch(NoSuchMethodException e)
         {
            if( strict )
               throw new SecurityException("Missing home method in delegate, " + e);
         }
      }
   }

   /**
    * Performs a mapping from the methods declared in the beanRemote class to the proxy delegate class.
    */
   protected void mapRemoteMethods(Class beanRemote)
   {
      Class delegateClass = delegate.getClass();
      Method[] methods = beanRemote.getMethods();
      for(int m = 0; m < methods.length; m++)
      {
         Method rm = methods[m];
         Class[] parameterTypes = rm.getParameterTypes();
         String name = rm.getName();
         try
         {
            Method match = delegateClass.getMethod(name, parameterTypes);
            methodMap.put(rm, match);
         }
         catch(NoSuchMethodException e)
         {
            if( strict )
               throw new SecurityException("Missing method in delegate, " + e);
         }
      }
   }
}
