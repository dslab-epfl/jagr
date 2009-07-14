/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.Map;
import java.util.HashMap;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import javax.management.modelmbean.ModelMBeanInfo;

/**
 * 
 *
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *   
 */
public class ModelMBeanInterceptor 
   extends Interceptor
{

   // Attributes ----------------------------------------------------   
   private Map methodMap = new HashMap();
//   private Class invocationInterface;
   private MBeanInfo info;
   private Object resource;
   
   // Constructors --------------------------------------------------
   public ModelMBeanInterceptor(Object resource, ModelMBeanInfo info) throws ReflectionException
   {
      super("Model MBean Interceptor");
      
      this.resource = resource;
//      this.invocationInterface = StandardMBeanInvoker.getMBeanInterface(resource);

      MBeanOperationInfo[] operations = info.getOperations();

      for (int i = 0; i < operations.length; ++i)
      {
         try
         {
            String name = operations[i].getName();
            MBeanParameterInfo[] params = operations[i].getSignature();
            
            StringBuffer strBuf = new StringBuffer(500);
            
            for (int j = 0; j < params.length; ++j) {
               strBuf.append(params[j].getType());
            }

            methodMap.put(name + strBuf.toString(), resource.getClass().getMethod(
                          name, StandardMBeanInterceptor.getSignatureAsClassArray(params, resource.getClass().getClassLoader())));
         }
         catch (ClassNotFoundException e)
         {
            throw new ReflectionException(e, "Unable to load operation " + operations[i].getName() + " parameter types: " + e.getMessage());
         }
         catch (NoSuchMethodException e)
         {
            throw new ReflectionException(e);
         }
      }      
   }
   
   // Public ------------------------------------------------------------
      
   // Interceptor overrides ----------------------------------------------
   public Object invoke(Invocation invocation) throws InvocationException 
   {
      try
      {
         Method m = (Method)methodMap.get(invocation.getOperationWithSignature());
         return m.invoke(resource, invocation.getArgs());
      }
      catch (IllegalAccessException e)
      {
         throw new InvocationException(e, "Illegal access to method " + invocation.getName());
      }
      catch (IllegalArgumentException e)
      {
         throw new InvocationException(e, "Illegal operation arguments in " + invocation.getName() + ": " + e.getMessage());
      }
      catch (InvocationTargetException e)
      {
         if (e.getTargetException() instanceof Exception)
         {
            Exception e2 = (Exception)e.getTargetException();
            throw new InvocationException(e2, "Operation " + invocation.getName() + " on MBean " + info.getClassName() + " has thrown an exception: " + e2.toString());
         }
         else
         {
            Error err = (Error)e.getTargetException();
            throw new InvocationException(err, "Operation " + invocation.getName() + " on MBean " + info.getClassName() + " has thrown an errpr: " + err.toString());
         }
      }
      catch (NullPointerException e)
      {
         throw new InvocationException(e, "Operation " + invocation.getName() + " is not a declared management operation.");
      }
   }
   
}


