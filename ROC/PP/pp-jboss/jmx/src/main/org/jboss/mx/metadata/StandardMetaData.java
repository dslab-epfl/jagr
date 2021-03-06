/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.metadata;

import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcaster;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 */
public class StandardMetaData implements MetaDataBuilder
{

   // Attributes ----------------------------------------------------
   private Object mbeanInstance = null;
   private Class mbeanClass = null;
   private Class mbeanInterface = null;

   // Constructors --------------------------------------------------
   public StandardMetaData(Object resourceInstance)
   {
      this(resourceInstance.getClass());
      this.mbeanInstance = resourceInstance;
   }

   public StandardMetaData(Class resourceClass)
   {
      this.mbeanClass = resourceClass;
      this.mbeanInterface = StandardMetaData.findStandardInterface(resourceClass);
   }

   // MetaDataBuilder implementation --------------------------------
   public MBeanInfo build() throws NotCompliantMBeanException
   {
      try
      {
         // First build the constructors
         Constructor[] constructors = mbeanClass.getConstructors();
         MBeanConstructorInfo[] constructorInfo = new MBeanConstructorInfo[constructors.length];

         for (int i = 0; i < constructors.length; ++i)
         {
            constructorInfo[i] = new MBeanConstructorInfo("MBean Constructor.", constructors[i]);
         }

         // Next we have to figure out how the methods in the mbean class map
         // to attributes and operations
         Method[] methods = mbeanInterface.getMethods();
         HashMap getters = new HashMap();
         HashMap setters = new HashMap();

         HashMap operInfo = new HashMap();
         List attrInfo = new ArrayList();

         for (int i = 0; i < methods.length; ++i)
         {
            String methodName = methods[i].getName();
            Class[] signature = methods[i].getParameterTypes();
            Class returnType = methods[i].getReturnType();

            if (methodName.startsWith("set") && signature.length == 1 && returnType == Void.TYPE)
            {
               String key = methodName.substring(3, methodName.length());
               Method setter = (Method) setters.get(key);
               if (setter != null && setter.getParameterTypes()[0].equals(signature[0]) == false)
               {
                  throw new IntrospectionException("overloaded type for attribute set: " + key);
               }
               setters.put(key, methods[i]);
            }
            else if (methodName.startsWith("get") && signature.length == 0 && returnType != Void.TYPE)
            {
               String key = methodName.substring(3, methodName.length());
               Method getter = (Method) getters.get(key);
               if (getter != null && getter.getName().startsWith("get") == false)
               {
                  throw new IntrospectionException("mixed use of get/is for attribute " + key);
               }
               getters.put(key, methods[i]);
            }
            else if (methodName.startsWith("is") && signature.length == 0 && (returnType == Boolean.class || returnType == Boolean.TYPE))
            {
               String key = methodName.substring(2, methodName.length());
               Method getter = (Method) getters.get(key);
               if (getter != null && getter.getName().startsWith("is") == false)
               {
                  throw new IntrospectionException("mixed use of get/is for attribute " + key);
               }
               getters.put(key, methods[i]);
            }
            else
            {
               MBeanOperationInfo info = new MBeanOperationInfo("MBean Operation.", methods[i]);
               operInfo.put(methods[i].toString(), info);
            }
         }

         Object[] keys = getters.keySet().toArray();
         for (int i = 0; i < keys.length; ++i)
         {
            String attrName = (String) keys[i];
            Method getter = (Method) getters.remove(attrName);
            Method setter = (Method) setters.remove(attrName);
            MBeanAttributeInfo info = new MBeanAttributeInfo(attrName, "MBean Attribute.", getter, setter);
            attrInfo.add(info);
         }

         Iterator it = setters.keySet().iterator();
         while (it.hasNext())
         {
            String attrName = (String) it.next();
            Method setter = (Method) setters.get(attrName);
            MBeanAttributeInfo info = new MBeanAttributeInfo(attrName, "MBean Attribute.", null, setter);
            attrInfo.add(info);
         }

         // save away the attribute and operation info objects
         MBeanAttributeInfo[] attributeInfo = (MBeanAttributeInfo[]) attrInfo.toArray(new MBeanAttributeInfo[0]);
         MBeanOperationInfo[] operationInfo = (MBeanOperationInfo[]) operInfo.values().toArray(new MBeanOperationInfo[0]);

         MBeanNotificationInfo[] notifications = null;
         if (mbeanInstance instanceof NotificationBroadcaster)
         {
            notifications = ((NotificationBroadcaster) mbeanInstance).getNotificationInfo();
         }
         else
         {
            notifications = new MBeanNotificationInfo[0];
         }

         return new MBeanInfo(mbeanClass.getName(), "Management Bean.", // FIXME is that a valid description?
                              attributeInfo, constructorInfo, operationInfo, notifications);

      }
      catch (IntrospectionException e)
      {
         throw new NotCompliantMBeanException(e.getMessage());
      }
   }

   public static Class findStandardInterface(Class mbeanClass)
   {
      Class concrete = mbeanClass;
      Class stdInterface = null;
      while (null != concrete)
      {
         stdInterface = findStandardInterface(concrete, concrete.getInterfaces());
         if (null != stdInterface)
         {
            return stdInterface;
         }
         concrete = concrete.getSuperclass();
      }
      return null;
   }

   public static Class findStandardInterface(Class concrete, Class[] interfaces)
   {
      String stdName = concrete.getName() + "MBean";
      Class retval = null;

      // look to see if this class implements MBean std interface
      for (int i = 0; i < interfaces.length; ++i)
      {
         if (interfaces[i].getName().equals(stdName))
         {
            retval = interfaces[i];
            break;
         }
      }

      return retval;
   }
}

