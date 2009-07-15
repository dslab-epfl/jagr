/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.modelmbean;

import java.net.URL;
import java.net.MalformedURLException;

import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.InvalidAttributeValueException;
import javax.management.NotCompliantMBeanException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;

import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.InvalidTargetObjectTypeException;

import org.jboss.mx.metadata.XMLMetaData;
import org.jboss.mx.metadata.StandardMetaData;
import org.jboss.mx.metadata.MBeanInfoConversion;
import org.jboss.mx.interceptor.ModelMBeanInterceptor;

public class XMBean
   extends ModelBase
   implements MBeanRegistration, XMBeanConstants
{

   // Constructors --------------------------------------------------
   public XMBean()
   {
      super();
   }

   public XMBean(ModelMBeanInfo info) throws MBeanException
   {
      super(info);
   }

   public XMBean(Object resource, URL config) throws MBeanException, NotCompliantMBeanException
   {
      try
      {
         setManagedResource(resource, config.toString());
         info = new ModelMBeanInfoSupport(
            (ModelMBeanInfo)new XMLMetaData(resource.getClass().getName(), config).build());
      }
      catch (InstanceNotFoundException e)
      {
         throw new MBeanException(e);
      }
      catch (InvalidTargetObjectTypeException e)
      {
         throw new MBeanException(e, "Unsupported resource type: " + config);
      }
   }

   public XMBean(Object resource, String resourceType/*, Object resourceInfo*/)
   throws MBeanException, NotCompliantMBeanException
   {
      try
      {
         //if (resourceType.startsWith("CLASS_NAME"))
         //{
         //   String
         setManagedResource(resource, resourceType);


         if (resourceType.equals(STANDARD_INTERFACE))
         {
            final boolean CREATE_ATTRIBUTE_OPERATION_MAPPING = true;
            
            MBeanInfo standardInfo = new StandardMetaData(resource).build();
            info = MBeanInfoConversion.toModelMBeanInfo(standardInfo, CREATE_ATTRIBUTE_OPERATION_MAPPING);
         }
         if (resourceType.endsWith(".xml"))
         {
            info = new ModelMBeanInfoSupport((ModelMBeanInfo)new XMLMetaData(resource.getClass().getName(), resourceType).build());
         }
      }
      catch (InstanceNotFoundException e)
      {
         throw new MBeanException(e);
      }
      catch (InvalidTargetObjectTypeException e)
      {
         if (resourceType.endsWith(".xml"))
            throw new MBeanException(e, "Malformed URL: " + resourceType);

         throw new MBeanException(e, "Unsupported resource type: " + resourceType);
      }
      catch (MalformedURLException e)
      {
         throw new MBeanException(e, "Malformed URL: " + resourceType);
      }
   }

   public XMBean(String resourceClass, String resourceType, Object resourceInfo)
   throws MBeanException, NotCompliantMBeanException
   {


   }

   // Public --------------------------------------------------------
   public boolean isSupportedResourceType(String resourceType)
   {
      return true;

      /*
      if (resourceType == null)
         return false;

      StringTokenizer strTokenizer = new StringTokenizer(resourceType, "/");
      String referenceType = strTokenizer.nextToken();
      String interfaceType = "/" + strTokenizer.nextToken();
      String resourceInfo  = strTokenizer.nextToken();

      if (referenceType.equals(OBJET_REF))
         return true;
      if (resourceType.equals(OBJECT_REF))
         return true;
      if (resourceType.equals(STANDARD_INTERFACE))
         return true;

      if (resourceType.endsWith(".xml"))
         try
         {
            new URL(resourceType);
            return true;
         }
         catch (MalformedURLException e)
         {
            return false;
         }

      return false;
      */
   }

   // DynamicMBean implementation -----------------------------------
   public MBeanInfo getMBeanInfo()
   {
      return info;
   }




}

