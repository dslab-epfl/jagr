/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.mx.metadata;

import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jboss.mx.modelmbean.ModelMBeanConstants;

/**
 * Routines for converting MBeanInfo to ModelMBeanInfoSupport and stripping ModelMBeanOperationInfos that
 * are referred to in ModelMBeanAttributeInfos
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 */
public class MBeanInfoConversion
   implements ModelMBeanConstants
{
   /**
    * Convert regular MBeanInfo into ModelMBeanInfo
    */
   public static ModelMBeanInfoSupport toModelMBeanInfo(MBeanInfo info)
   {
      return toModelMBeanInfo(info, false);
   }
   
   public static ModelMBeanInfoSupport toModelMBeanInfo(MBeanInfo info, boolean createAttributeOperationMapping)
   {
      MBeanAttributeInfo[] attributes = info.getAttributes();
      ModelMBeanAttributeInfo[] mmbAttributes = new ModelMBeanAttributeInfo[attributes.length];
      List accessorOperations = new ArrayList();
      
      for (int i = 0; i < attributes.length; ++i)
      {
         mmbAttributes[i] = new ModelMBeanAttributeInfo(
            attributes[i].getName(),
            attributes[i].getType(),
            attributes[i].getDescription(),
            attributes[i].isReadable(),
            attributes[i].isWritable(),
            attributes[i].isIs()
         );
         
         if (createAttributeOperationMapping)
         {
            String getterOperationName  = null;
            String setterOperationName  = null;
            Descriptor getterDescriptor = null;
            Descriptor setterDescriptor = null;
            
            if (attributes[i].isReadable())
            {
               if (attributes[i].isIs())
                  getterOperationName = "is" + attributes[i].getName();
               else 
                  getterOperationName = "get" + attributes[i].getName();
                  
               getterDescriptor = new DescriptorSupport();
               getterDescriptor.setField(NAME, getterOperationName);
               getterDescriptor.setField(DESCRIPTOR_TYPE, OPERATION_DESCRIPTOR);
               getterDescriptor.setField(ROLE, GETTER);
               
               ModelMBeanOperationInfo opInfo = new ModelMBeanOperationInfo(
                     getterOperationName,
                     "Read accessor operation for '" + attributes[i].getName() + "' attribute.",
                     new MBeanParameterInfo[0],    // void signature
                     attributes[i].getType(),      // return type
                     MBeanOperationInfo.INFO,      // impact
                     getterDescriptor
               );

               Descriptor attrDescriptor = mmbAttributes[i].getDescriptor();
               attrDescriptor.setField(GET_METHOD, getterOperationName);
               mmbAttributes[i].setDescriptor(attrDescriptor);
               
               accessorOperations.add(opInfo);
            }
            
            if (attributes[i].isWritable())
            {
               setterOperationName = "set" + attributes[i].getName();   
               
               setterDescriptor = new DescriptorSupport();
               setterDescriptor.setField(NAME, setterOperationName);
               setterDescriptor.setField(DESCRIPTOR_TYPE, OPERATION_DESCRIPTOR);
               setterDescriptor.setField(ROLE, SETTER);
               
               ModelMBeanOperationInfo opInfo = new ModelMBeanOperationInfo(
                     setterOperationName,
                     "Write accessor operation for '" + attributes[i].getName() + "' attribute.",
                     
                     new MBeanParameterInfo[] {
                        new MBeanParameterInfo("value", attributes[i].getType(), "Attribute's value.")
                     },
                     
                     Void.TYPE.getName(),
                     MBeanOperationInfo.ACTION,
                     setterDescriptor
               );
               
               Descriptor attrDescriptor = mmbAttributes[i].getDescriptor();
               attrDescriptor.setField(SET_METHOD, setterOperationName);
               mmbAttributes[i].setDescriptor(attrDescriptor);
               
               accessorOperations.add(opInfo);
            }
         }            
      }

      MBeanOperationInfo[] operations = info.getOperations();
      ModelMBeanOperationInfo[] mmbOperations = new ModelMBeanOperationInfo[operations.length + accessorOperations.size()];

      for (int i = 0; i < operations.length; ++i)
      {
         mmbOperations[i] = new ModelMBeanOperationInfo(
            operations[i].getName(),
            operations[i].getDescription(),
            operations[i].getSignature(),
            operations[i].getReturnType(),
            operations[i].getImpact()
         );
      }
      
      for (int i = operations.length; i < mmbOperations.length; ++i)
         mmbOperations[i] = (ModelMBeanOperationInfo)accessorOperations.get(i - operations.length);

      MBeanConstructorInfo[] constructors = info.getConstructors();
      ModelMBeanConstructorInfo[] mmbConstructors = new ModelMBeanConstructorInfo[constructors.length];

      for (int i = 0; i < constructors.length; ++i)
      {
         mmbConstructors[i] = new ModelMBeanConstructorInfo(
            constructors[i].getName(),
            constructors[i].getDescription(),
            constructors[i].getSignature()
         );
      }

      MBeanNotificationInfo[] notifications = info.getNotifications();
      ModelMBeanNotificationInfo[] mmbNotifications = new ModelMBeanNotificationInfo[notifications.length];

      for (int i = 0; i < notifications.length; ++i)
      {
         mmbNotifications[i] = new ModelMBeanNotificationInfo(
            notifications[i].getNotifTypes(),
            notifications[i].getName(),
            notifications[i].getDescription()
         );
      }

      return new ModelMBeanInfoSupport(info.getClassName(), info.getDescription(),
                                       mmbAttributes, mmbConstructors, mmbOperations, mmbNotifications);
   }

   /**
    * Returns a ModelMBeanInfoSupport where ModelMBeanOperationInfos that are referred to by ModelMBeanAttributeInfo
    * getMethod or setMethod descriptor fields are stripped out.  If the stripAllRoles parameter is true
    * then all the referred-to operations will be stripped.  Otherwise only referred-to operations with a
    * role of "getter" or "setter" will be stripped.
    */
   public static ModelMBeanInfoSupport stripAttributeOperations(ModelMBeanInfo info, boolean stripAllRoles) throws MBeanException
   {
      HashMap opsMap = new HashMap();
      ModelMBeanOperationInfo[] operations = (ModelMBeanOperationInfo[]) info.getOperations();

      for (int i = 0; i < operations.length; i++)
      {
         opsMap.put(MethodMapper.operationSignature(operations[i]), operations[i]);
      }

      ModelMBeanAttributeInfo[] attributes = (ModelMBeanAttributeInfo[]) info.getAttributes();

      for (int i = 0; i < attributes.length; i++)
      {
         if (attributes[i].isReadable() && (attributes[i].getDescriptor().getFieldValue("getMethod") != null))
         {
            String key = MethodMapper.getterSignature(attributes[i]);
            ModelMBeanOperationInfo opinfo = (ModelMBeanOperationInfo) opsMap.get(key);
            String role = (String) opinfo.getDescriptor().getFieldValue("role");
            if ("getter".equals(role) || stripAllRoles)
            {
               opsMap.remove(key);
            }
         }

         if (attributes[i].isWritable() && (attributes[i].getDescriptor().getFieldValue("setMethod") != null))
         {
            String key = MethodMapper.getterSignature(attributes[i]);
            ModelMBeanOperationInfo opinfo = (ModelMBeanOperationInfo) opsMap.get(key);
            String role = (String) opinfo.getDescriptor().getFieldValue("role");
            if ("setter".equals(role) || stripAllRoles)
            {
               opsMap.remove(key);
            }
         }
      }

      operations = new ModelMBeanOperationInfo[opsMap.size()];
      int position = 0;
      for (Iterator iterator = opsMap.values().iterator(); iterator.hasNext(); position++)
      {
         operations[position] = (ModelMBeanOperationInfo) iterator.next();
      }

      return new ModelMBeanInfoSupport(info.getClassName(), info.getDescription(), (ModelMBeanAttributeInfo[]) info.getAttributes(),
                                       (ModelMBeanConstructorInfo[]) info.getConstructors(), operations, (ModelMBeanNotificationInfo[]) info.getNotifications(),
                                       info.getMBeanDescriptor());
   }
}
