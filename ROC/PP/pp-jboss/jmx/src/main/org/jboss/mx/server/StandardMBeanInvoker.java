/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.server;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import javax.management.DynamicMBean;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.JMException;
import javax.management.NotCompliantMBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.loading.DefaultLoaderRepository;

import org.jboss.mx.metadata.StandardMetaData;
import org.jboss.mx.interceptor.Interceptor;
import org.jboss.mx.interceptor.Invocation;
import org.jboss.mx.interceptor.StandardMBeanInterceptor;
import org.jboss.mx.interceptor.LogInterceptor;
import org.jboss.mx.interceptor.SecurityInterceptor;
import org.jboss.mx.interceptor.InvocationException;


/**
 * Represents standard MBean in the server.
 *
 * @see org.jboss.mx.interceptor.StandardMBeanInterceptor
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *
 */
public class StandardMBeanInvoker
   extends MBeanInvoker
{

   // Attributes ----------------------------------------------------

   // Constructors --------------------------------------------------
   public StandardMBeanInvoker(Object resource) throws NotCompliantMBeanException, ReflectionException
   {
      super.resource = resource;
      this.info = new StandardMetaData(resource).build();

      Interceptor security = new SecurityInterceptor();
      security.insertLast(new LogInterceptor());
      security.insertLast(new StandardMBeanInterceptor(resource, info));
      stack = security;
   }

   // Public --------------------------------------------------------
   public static Class getMBeanInterface(Object resource)
   {
      Class clazz = resource.getClass();

      while (clazz != null)
      {
         Class[] interfaces = clazz.getInterfaces();

         for (int i = 0; i < interfaces.length; ++i)
         {
            if (interfaces[i].getName().equals(clazz.getName() + "MBean"))
               return interfaces[i];

            Class[] superInterfaces = interfaces[i].getInterfaces();
            for (int j = 0; j < superInterfaces.length; ++j)
            {
               if (superInterfaces[j].getName().equals(clazz.getName() + "MBean"))
                  return superInterfaces[j];
            }
         }
         clazz = clazz.getSuperclass();
      }

      return null;
   }

   // DynamicMBean implementation -----------------------------------

   public MBeanInfo getMBeanInfo()
   {
      return info;
   }

}


