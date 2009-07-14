/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.util;

import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.util.HashMap;
import java.util.ArrayList;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.InvalidAttributeValueException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;

/**
 *
 * @see java.lang.reflect.Proxy
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *   
 */
public class MBeanProxy
{

   // Static --------------------------------------------------------
   
   // FIXME: unify the exceptions thrown by the static get and create methods
   
   /**
    * Creates a proxy to an MBean in the given (local) MBean server.
    *
    * @param   intrface    the interface this proxy implements
    * @param   name        object name of the MBean this proxy connects to
    * @param   agentID     agent ID of the MBean server this proxy connects to
    *
    * @return  proxy instance
    *
    * @throws MBeanProxyCreationException if the proxy could not be created
    */
   public static Object get(Class intrface, ObjectName name, String agentID) throws MBeanProxyCreationException
   {
      return get(intrface, name, (MBeanServer)MBeanServerFactory.findMBeanServer(agentID).get(0));
   }

   /**
    * Creates a proxy to an MBean in the given (local) MBean server.
    *
    * @param   intrface the interface this proxy implements
    * @param   name     object name of the MBean this proxy connects to
    * @param   server   MBean server this proxy connects to
    *
    * @return proxy instance
    *
    * @throws MBeanProxyCreationException if the proxy could not be created
    */
   public static Object get(Class intrface, ObjectName name, MBeanServer server)  throws MBeanProxyCreationException
   {
      return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
            new Class[] {intrface}, new JMXInvocationHandler(server, name));
   }


   /**
    * Convenience method for registering an MBean and retrieving a proxy for it.
    *
    * @param   instance MBean instance to be registered
    * @param   intrface the interface this proxy implements
    * @param   name     object name of the MBean
    * @param   agentID  agent ID of the MBean server this proxy connects to
    *
    * @return proxy instance
    *
    * @throws MBeanProxyCreationException if the proxy could not be created
    */
   public static Object create(Class instance, Class intrface, ObjectName name, String agentID) throws MBeanProxyCreationException
   {
      return create(instance, intrface, name, (MBeanServer)MBeanServerFactory.findMBeanServer(agentID).get(0));
   }   
   
   /**
    * Convenience method for registering an MBean and retrieving a proxy for it.
    *
    * @param   instance MBean instance to be registered
    * @param   intrface the interface this proxy implements
    * @param   name     object name of the MBean
    * @param   server   MBean server this proxy connects to
    *
    * @throws MBeanProxyCreationException if the proxy could not be created
    */
   public static Object create(Class instance, Class intrface, ObjectName name, MBeanServer server) throws MBeanProxyCreationException
   {
      try
      {
         server.createMBean(instance.getName(), name);
         return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
               new Class[] {intrface}, new JMXInvocationHandler(server, name));
      }
      catch (ReflectionException e) {
         throw new MBeanProxyCreationException("Creating the MBean failed: " + e.toString());
      }
      catch (InstanceAlreadyExistsException e) {
         throw new MBeanProxyCreationException("Instance already exists: " + name);
      }
      catch (MBeanRegistrationException e) {
         throw new MBeanProxyCreationException("Error registering the MBean to the server: " + e.toString());
      }
      catch (MBeanException e) {
         throw new MBeanProxyCreationException(e.toString());
      }
      catch (NotCompliantMBeanException e) {
         throw new MBeanProxyCreationException("Not a compliant MBean " + instance.getClass().getName() + ": " + e.toString());
      }
   }

   
   // Inner Classes -------------------------------------------------
   
   /**
    * Proxy invocation handler
    */
   private static class JMXInvocationHandler implements InvocationHandler
   {  
      
      // Attributes -------------------------------------------------
      private MBeanServer server    = null;
      private ObjectName objectName = null;
      private HashMap attributeMap  = new HashMap();
      
      // Constructors -----------------------------------------------
      public JMXInvocationHandler(MBeanServer server, ObjectName name) throws MBeanProxyCreationException
      {
         try
         {
            // make sure we were able to resolve the server
            if (server == null)
               throw new InstanceNotFoundException("null agent reference");
               
            this.server     = server;
            this.objectName = name;
           
            MBeanInfo info = server.getMBeanInfo(objectName);
            MBeanAttributeInfo[] attributes = info.getAttributes();
   
            for (int i = 0; i < attributes.length; ++i)
               attributeMap.put(attributes[i].getName(), attributes[i]);
         }
         catch (InstanceNotFoundException e)
         {
            throw new MBeanProxyCreationException("Object name " + name + " not found: " + e.toString());
         }
         catch (IntrospectionException e)
         {
            throw new MBeanProxyCreationException(e.toString());
         }
         catch (ReflectionException e)
         {
            throw new MBeanProxyCreationException(e.toString());
         }
      }
      
      // InvocationHandler implementation ---------------------------
      public Object invoke(Object proxy, Method method, Object[] args) throws Exception
      {
         try {
            String methodName = method.getName();
            
            if (methodName.startsWith("get") && args == null)
            {
               String attrName = methodName.substring(3, methodName.length());
               
               MBeanAttributeInfo info = (MBeanAttributeInfo)attributeMap.get(attrName);
               if (info != null)
               {
                  String retType  = method.getReturnType().getName();
                  
                  if (retType.equals(info.getType())) 
                  {
                     return server.getAttribute(objectName, attrName);
                  }
               }
            }
            
            else if (methodName.startsWith("is") && args == null)
            {
               String attrName = methodName.substring(2, methodName.length());
               
               MBeanAttributeInfo info = (MBeanAttributeInfo)attributeMap.get(attrName);
               if (info != null && info.isIs())
               {
                  Class retType = method.getReturnType();
                  
                  if (retType.equals(Boolean.class) || retType.equals(Boolean.TYPE))
                  {
                     return server.getAttribute(objectName, attrName);
                  }
               }
            }
            
            else if (methodName.startsWith("set") && args != null && args.length == 1)
            {
               String attrName = methodName.substring(3, methodName.length());
               
               MBeanAttributeInfo info = (MBeanAttributeInfo)attributeMap.get(attrName);
               if (info != null && method.getReturnType().equals(Void.TYPE))
               {
                  if (info.getType().equals(args[0].getClass().getName()))
                  {
                     server.setAttribute(objectName, new Attribute(attrName, args[0]));
                     return null;
                  }
               }
            }

            String[] signature = null;
            
            if (args != null)
            {
               signature = new String[args.length];
               Class[] sign = method.getParameterTypes();
               
               for (int i = 0; i < sign.length; ++i)
                  signature[i] = sign[i].getName();
            }
            
            return server.invoke(objectName, methodName, args, signature);
         }
         
         // InstanceNotFound, AttributeNotFound and InvalidAttributeValue
         // are not exceptions declared in the mgmt interface and therefore
         // must be rethrown as runtime exceptions to avoid UndeclaredThrowable
         // exceptions on the client
         catch (InstanceNotFoundException e)
         {
            throw new RuntimeException("Instance not found: " + e.toString());
         }
         catch (AttributeNotFoundException e)
         {
            throw new RuntimeException("Attribute not found: " + e.toString());
         }
         catch (InvalidAttributeValueException e)
         {
            throw new RuntimeException("Invalid attribute value: " + e.toString());
         }
         catch (MBeanException e)
         {
            // assuming MBeanException only wraps mgmt interface "application" 
            // exceptions therefore we can safely rethrow the target exception
            // as its declared in the mgmt interface
            throw e.getTargetException();
         }
         catch (ReflectionException e)
         {
            // use of reflection exception is inconsistent in the API so the 
            // safest bet is to rethrow a runtime exception
            
            Exception target = e.getTargetException();
            if (target instanceof RuntimeException)
               throw target;
            else
               throw new RuntimeException(target.toString());
         }
         catch (RuntimeOperationsException e)
         {
            // target is always a runtime exception, so its ok to throw it from here
            throw e.getTargetException();
         }
         catch (RuntimeMBeanException e)
         {
            // target is always a runtime exception, so its ok to throw it from here
            throw e.getTargetException();
         }
         catch (RuntimeErrorException e)
         {
            // just unwrap and throw the actual error
            throw e.getTargetError();
         }
      }
   }
   
}



