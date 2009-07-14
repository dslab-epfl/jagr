/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.interceptor;

import org.jboss.mx.capability.DispatcherFactory;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.Map;
import java.util.HashMap;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import javax.management.DynamicMBean;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.Attribute;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;

/**
 * Lifted the functionality seen in MBeanAttributeInterceptor to
 * delegate to a dispatcher which implements DynamicMBean.
 *
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 */
public class ObjectReferenceInterceptor
   extends Interceptor
{

   // Attributes ----------------------------------------------------
   private MBeanInfo info;
   private DynamicMBean resource;

   // Constructors --------------------------------------------------
   public ObjectReferenceInterceptor(Object resource, ModelMBeanInfo info) throws ReflectionException
   {
      super("Model MBean Interceptor");
      try
      {
         this.resource = DispatcherFactory.create(new ModelMBeanInfoSupport(info), resource);
      }
      catch (IntrospectionException e)
      {
         throw new ReflectionException(e);
      }
   }

   // Public ------------------------------------------------------------

   // Interceptor overrides ----------------------------------------------
   public Object invoke(Invocation invocation) throws InvocationException
   {
      try
      {
         if (invocation.getInvocationType() == Invocation.OPERATION)
            return resource.invoke(invocation.getName(), invocation.getArgs(), invocation.getSignature());

         if (invocation.getImpact() == Invocation.WRITE)
         {
            resource.setAttribute(new Attribute(invocation.getName(), invocation.getArgs()[0]));
            return null;
         }
         else
         {
            return resource.getAttribute(invocation.getName());
         }
      }
      catch (Throwable t)
      {
         // FIXME: need to check this exception handling
         throw new InvocationException(t);
      }
   }

}


