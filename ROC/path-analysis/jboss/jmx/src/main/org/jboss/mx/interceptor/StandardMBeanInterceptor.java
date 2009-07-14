/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.interceptor;

import java.util.Map;
import java.util.HashMap;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.loading.DefaultLoaderRepository;

import org.jboss.mx.server.StandardMBeanInvoker;


/**
 * Standard MBean interceptor is the last interceptor in the chain that
 * fields the call to the resource class.
 *
 * @see org.jboss.mx.interceptor.Interceptor
 * @see org.jboss.mx.server.MBeanServerImpl
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *   
 */
public class StandardMBeanInterceptor 
   extends Interceptor
{

   // Attributes ----------------------------------------------------   
   private Map methodMap               = new HashMap();
   private Class invocationInterface   = null;
   private MBeanInfo info              = null;
   private Object resource             = null;
   
   // Constructors --------------------------------------------------
   public StandardMBeanInterceptor(Object resource, MBeanInfo info) throws ReflectionException
   {
      super("Standard MBean Interceptor");
      
      this.info = info;
      this.resource = resource;
      this.invocationInterface = StandardMBeanInvoker.getMBeanInterface(resource);
      
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

            // FIXME: separate operation and attribute maps!
            
            methodMap.put(name + strBuf.toString(), invocationInterface.getMethod(
                          name, getSignatureAsClassArray(params, resource.getClass().getClassLoader())));
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
      
      MBeanAttributeInfo[] attributes = info.getAttributes();
      
      for (int i = 0; i < attributes.length; ++i)
      {
         String name = attributes[i].getName();
         String type = attributes[i].getType();
      
         try 
         {
            boolean isReadable = attributes[i].isReadable();
            boolean isWritable = attributes[i].isWritable();
            boolean isIs = attributes[i].isIs();
            
            if (isReadable)
               if (isIs)
                  methodMap.put("get" + name, invocationInterface.getMethod("is" + name, null));
               else
                  methodMap.put("get" + name, invocationInterface.getMethod("get" + name, null));
                  
            if (isWritable)
               methodMap.put("set" + name + type, invocationInterface.getMethod(
                             "set" + name, getSignatureAsClassArray(
                                    new String[] { type },
                                    resource.getClass().getClassLoader()))); 
         }
         catch (ClassNotFoundException e) 
         {
            throw new ReflectionException(e, "Unable to load type for attribute " + name + ": " + type);
         }
         catch (NoSuchMethodException e) 
         {
            throw new ReflectionException(e);
         }
      }  
   }
   
   
   // Public ------------------------------------------------------------
   public static Class[] getSignatureAsClassArray(String[] signature, ClassLoader cl) throws ClassNotFoundException
   {
      Class[] sign = new Class[signature.length];
      for (int i = 0; i < signature.length; ++i)
      {
         try 
         {
            sign[i] = getClassForType(signature[i], cl);
         }
         catch (ClassNotFoundException e) 
         {
            // if the explicit CL fails, go to the repository... allow CNFE to be thrown
            DefaultLoaderRepository.loadClass(signature[i]);
         }
      }
      
      return sign;
   }
   
   public static Class[] getSignatureAsClassArray(MBeanParameterInfo[] signature, ClassLoader cl) throws ClassNotFoundException
   {
      Class[] sign = new Class[signature.length];
      for (int i = 0; i < signature.length; ++i)
      {
         try
         {
            String type = signature[i].getType();
            sign[i] = getClassForType(type, cl);
         }
         catch (ClassNotFoundException e)
         {
            // if the explicit CL fails, go to the repository... allow CNFE to be thrown
            DefaultLoaderRepository.loadClass(signature[i].getName());
         }
      }

      return sign;
   }

   public static Class getClassForType(String type, ClassLoader cl) throws ClassNotFoundException
   {
      if (int.class.getName().equals(type))
         return Integer.TYPE;
      else if (float.class.getName().equals(type))
         return Float.TYPE;
      else if (double.class.getName().equals(type))
         return Double.TYPE;
      else if (long.class.getName().equals(type))
         return Long.TYPE;
      else if (byte.class.getName().equals(type))
         return Byte.TYPE;
      else if (boolean.class.getName().equals(type))
         return Boolean.TYPE;
      else if (char.class.getName().equals(type))
         return Character.TYPE;
      else if (void.class.getName().equals(type))
         return Void.TYPE;
      else if (short.class.getName().equals(type))
         return Short.TYPE;
      return cl.loadClass(type);
   }   
      
   // Interceptor overrides ----------------------------------------------
   public Object invoke(Invocation invocation) throws InvocationException 
   {
      Method method = null;
      
      try
      {
         method = (Method)methodMap.get(invocation.getOperationWithSignature());
         return method.invoke(resource, invocation.getArgs());
      }
      
      catch (IllegalAccessException e)
      {
         throw new InvocationException(new ReflectionException(e,"Illegal access to method " + method.getName()));
      }
      
      catch (IllegalArgumentException e)
      {
         throw new InvocationException(new ReflectionException(e, 
               "Illegal arguments for " + ((invocation.getInvocationType() == Invocation.ATTRIBUTE)
                                        ? "attribute "
                                        : "operation ")
                                        + invocation.getName()
                                        + ": " + e.getMessage())
         );
      }
      
      catch (InvocationTargetException e)
      {
         // exception or error from the MBean implementation ('business exception')
         
         if (e.getTargetException() instanceof RuntimeException)
         {
            // runtime exceptions from mbean wrapped in RuntimeMBeanException
            throw new InvocationException(new RuntimeMBeanException((RuntimeException)e.getTargetException(),
                  "RuntimeException thrown by " + ((invocation.getInvocationType() == Invocation.ATTRIBUTE)
                                                ? "attribute "
                                                : "operation ")
                                                + invocation.getName() + " in MBean "
                                                + info.getClassName() + ": "
                                                + e.getTargetException().toString())
            );                  
         }
         
         else if (e.getTargetException() instanceof Exception)
         {
            // checked exceptions from mbean wrapped in MBeanException
            throw new InvocationException(new MBeanException((Exception)e.getTargetException(),
                  "Exception thrown by " + ((invocation.getInvocationType() == Invocation.ATTRIBUTE)
                                         ? "attribute "
                                         : "operation ")
                                         + invocation.getName() + " in MBean " 
                                         + info.getClassName() + ": "
                                         + e.getTargetException().toString())
            );
         }
         
         else if (e.getTargetException() instanceof Error)
         {
            // errors from mbean wrapped in RuntimeErrorException
            throw new InvocationException(new RuntimeErrorException((Error)e.getTargetException(),
                  "Error thrown by " + ((invocation.getInvocationType() == Invocation.ATTRIBUTE)
                                     ? "attribute "
                                     : "operation ")
                                     + invocation.getName() + " in MBean "
                                     + info.getClassName() + ": "
                                     + e.getTargetException().toString())
            );
         }
         
         else
         {
            throw new InvocationException(e.getTargetException());
         }
      }
      catch (NullPointerException e)
      {
         // method (operation or getter/setter) is not part of the management interface
         
         if (invocation.getInvocationType() == Invocation.ATTRIBUTE)
            throw new InvocationException(new AttributeNotFoundException("Attribute '" + invocation.getName() + "' not found on " + info.getClassName() + "."), "plaah");
         else 
            throw new InvocationException(new ReflectionException(new NullPointerException("Operation '" + invocation.getName() + "' not found on " + info.getClassName() + ".")));
      }
   }
   
}


