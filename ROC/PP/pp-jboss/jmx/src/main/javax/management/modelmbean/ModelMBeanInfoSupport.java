/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management.modelmbean;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

import javax.management.MBeanInfo;
import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanInfo;

import org.jboss.mx.modelmbean.ModelMBeanConstants;

/**
 * Support class for <tt>ModelMBeanInfo</tt> interface.
 *
 * @see javax.management.modelmbean.ModelMBeanInfo
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 */
public class ModelMBeanInfoSupport
      extends MBeanInfo
      implements ModelMBeanInfo, Serializable
{

   // Attributes ----------------------------------------------------
   private Descriptor mbeanDescriptor  = null;

   // Constructors --------------------------------------------------
   public ModelMBeanInfoSupport(ModelMBeanInfo mbi)
   {
      super(mbi.getClassName(), mbi.getDescription(), mbi.getAttributes(),
            mbi.getConstructors(), mbi.getOperations(), mbi.getNotifications());

      try
      {
         setMBeanDescriptor(mbi.getMBeanDescriptor());
      }
      catch (MBeanException e)
      {
         throw new IllegalArgumentException(e.toString() /* FIXME: message */ );
      }
   }

   public ModelMBeanInfoSupport(String className, String description,
                                ModelMBeanAttributeInfo[] attributes,
                                ModelMBeanConstructorInfo[] constructors,
                                ModelMBeanOperationInfo[] operations,
                                ModelMBeanNotificationInfo[] notifications)
   {
      super(className, description,
            (null == attributes) ? new ModelMBeanAttributeInfo[0] : attributes,
            (null == constructors) ? new ModelMBeanConstructorInfo[0] : constructors,
            (null == operations) ? new ModelMBeanOperationInfo[0] : operations,
            (null == notifications) ? new ModelMBeanNotificationInfo[0] : notifications);

      try
      {
         setMBeanDescriptor(createDefaultDescriptor(className));
      }
      catch (MBeanException e)
      {
         throw new IllegalArgumentException(e.toString() /* FIXME: message */ );
      }
   }

   public ModelMBeanInfoSupport(String className, String description,
                                ModelMBeanAttributeInfo[] attributes,
                                ModelMBeanConstructorInfo[] constructors,
                                ModelMBeanOperationInfo[] operations,
                                ModelMBeanNotificationInfo[] notifications,
                                Descriptor mbeandescriptor)
   {
      this(className, description, attributes, constructors, operations, notifications);
      try
      {
         setMBeanDescriptor(mbeandescriptor);
      }
      catch (MBeanException e)
      {
         throw new IllegalArgumentException(e.toString() /* FIXME: message */ );
      }
   }


   // Public --------------------------------------------------------
   public Descriptor[] getDescriptors(String descrType) throws MBeanException
   {
      if (descrType == null)
      {
         List list = new ArrayList(100);
         list.add(mbeanDescriptor);
         list.addAll(getAttributeDescriptors().values());
         list.addAll(getOperationDescriptors().values());
         list.addAll(getNotificationDescriptors().values());
         list.addAll(getConstructorDescriptors().values());
         return (Descriptor[])list.toArray(new Descriptor[0]);
      }

      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.MBEAN_DESCRIPTOR))
         return new Descriptor[] { mbeanDescriptor };

      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.ATTRIBUTE_DESCRIPTOR))
         return (Descriptor[])getAttributeDescriptors().values().toArray(new Descriptor[0]);

      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.OPERATION_DESCRIPTOR))
         return (Descriptor[])getOperationDescriptors().values().toArray(new Descriptor[0]);

      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.NOTIFICATION_DESCRIPTOR))
         return (Descriptor[])getNotificationDescriptors().values().toArray(new Descriptor[0]);

      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.CONSTRUCTOR_DESCRIPTOR))
         return (Descriptor[])getConstructorDescriptors().values().toArray(new Descriptor[0]);

      throw new IllegalArgumentException("unknown descriptor type: " + descrType);
   }

   /**
    * Adds or replaces the descriptors in this Model MBean. All descriptors
    * must be valid. <tt>Null</tt> references will be ignored.
    *
    * @param   inDescriptors  array of descriptors
    */
   public void setDescriptors(Descriptor[] inDescriptors) throws MBeanException
   {
      for (int i = 0; i < inDescriptors.length; ++i)
      {
         if (inDescriptors[i] != null && inDescriptors[i].isValid())
         {
            setDescriptor(
                  inDescriptors[i],
                  (String)inDescriptors[i].getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE)
            );
         }
      }
   }

   public Descriptor getDescriptor(String descrName) throws MBeanException
   {
      if (descrName.equals(mbeanDescriptor.getFieldValue(ModelMBeanConstants.NAME)))
         return mbeanDescriptor;

      Descriptor descr = null;

      descr = (Descriptor)getAttributeDescriptors().get(descrName);
      if (descr != null)
         return descr;

      descr = (Descriptor)getOperationDescriptors().get(descrName);
      if (descr != null)
         return descr;

      descr = (Descriptor)getNotificationDescriptors().get(descrName);
      if (descr != null)
         return descr;

      descr = (Descriptor)getConstructorDescriptors().get(descrName);
      if (descr != null)
         return descr;

      return null;
   }

   public Descriptor getDescriptor(String descrName, String descrType) throws MBeanException
   {
      if (descrType == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("null descriptor type"));

      if (descrType.equalsIgnoreCase(ModelMBeanConstants.MBEAN_DESCRIPTOR))
         return mbeanDescriptor;
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.ATTRIBUTE_DESCRIPTOR))
         return (Descriptor)getAttributeDescriptors().get(descrName);
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.OPERATION_DESCRIPTOR))
         return (Descriptor)getOperationDescriptors().get(descrName);
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.CONSTRUCTOR_DESCRIPTOR))
         return (Descriptor)getConstructorDescriptors().get(descrName);
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.NOTIFICATION_DESCRIPTOR))
         return (Descriptor)getNotificationDescriptors().get(descrName);

      throw new IllegalArgumentException("unknown descriptor type: " + descrType);
   }

   public void setDescriptor(Descriptor descr, String descrType) throws MBeanException
   {
      if (descrType == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("null descriptor type"));

      if (descrType.equalsIgnoreCase(ModelMBeanConstants.MBEAN_DESCRIPTOR))
      {
         setMBeanDescriptor(descr);
      }
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.ATTRIBUTE_DESCRIPTOR))
      {
         ModelMBeanAttributeInfo info = getAttribute((String)descr.getFieldValue(ModelMBeanConstants.NAME));
         info.setDescriptor(descr);
      }
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.OPERATION_DESCRIPTOR))
      {
         ModelMBeanOperationInfo info = getOperation((String)descr.getFieldValue(ModelMBeanConstants.NAME));
         info.setDescriptor(descr);
      }
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.CONSTRUCTOR_DESCRIPTOR))
      {
         ModelMBeanConstructorInfo info = getConstructor((String)descr.getFieldValue(ModelMBeanConstants.NAME));
         info.setDescriptor(descr);
      }
      else if (descrType.equalsIgnoreCase(ModelMBeanConstants.NOTIFICATION_DESCRIPTOR))
      {
         ModelMBeanNotificationInfo info = getNotification((String)descr.getFieldValue(ModelMBeanConstants.NAME));
         info.setDescriptor(descr);
      }
      else
         throw new IllegalArgumentException("unknown descriptor type: " + descrType);
   }

   public ModelMBeanAttributeInfo getAttribute(String inName) throws MBeanException
   {
      for (int i = 0; i < attributes.length; ++i)
         if (attributes[i].getName().equals(inName))
            return (ModelMBeanAttributeInfo)attributes[i];

      throw new RuntimeOperationsException(new IllegalArgumentException("MBean does not contain attribute " + inName));
   }

   public ModelMBeanOperationInfo getOperation(String inName) throws MBeanException
   {
      for (int i = 0; i < operations.length; ++i)
         if (operations[i].getName().equals(inName))
            return (ModelMBeanOperationInfo)operations[i];

      throw new RuntimeOperationsException(new IllegalArgumentException("MBean does not contain operation " + inName));
   }

   public ModelMBeanConstructorInfo getConstructor(String inName) throws MBeanException
   {
      for (int i = 0; i < constructors.length; ++i)
         if (constructors[i].getName().equals(inName))
            return (ModelMBeanConstructorInfo)constructors[i];

      throw new RuntimeOperationsException(new IllegalArgumentException("MBean does not contain constructor " + inName));
   }

   public ModelMBeanNotificationInfo getNotification(String inName) throws MBeanException
   {
      for (int i = 0; i < notifications.length; ++i)
         if (notifications[i].getName().equals(inName))
            return (ModelMBeanNotificationInfo)notifications[i];

      throw new RuntimeOperationsException(new IllegalArgumentException("MBean does not contain notification " + inName));
   }

   public MBeanAttributeInfo[] getAttributes()
   {
      return super.getAttributes();
   }

   public MBeanOperationInfo[] getOperations()
   {
      return super.getOperations();
   }

   public MBeanConstructorInfo[] getConstructors()
   {
      return super.getConstructors();
   }

   public MBeanNotificationInfo[] getNotifications()
   {
      return super.getNotifications();
   }

   public Descriptor getMBeanDescriptor() throws MBeanException
   {
      return mbeanDescriptor;
   }

   public void setMBeanDescriptor(Descriptor inMBeanDescriptor) throws MBeanException
   {
      Descriptor descr = (Descriptor)inMBeanDescriptor.clone();
      addDefaultMBeanDescriptorFields(descr);

      this.mbeanDescriptor = descr;
   }


   // Y overrides ---------------------------------------------------
   public Object clone()
   {
      try
      {
         ModelMBeanInfoSupport clone = (ModelMBeanInfoSupport)super.clone();
         clone.mbeanDescriptor = (Descriptor)mbeanDescriptor.clone();

         return clone;
      }
      catch (CloneNotSupportedException e)
      {
         return null;
      }
   }

   // Private -------------------------------------------------------
   private void addDefaultMBeanDescriptorFields(Descriptor descr)
   {
      if (descr.getFieldValue(ModelMBeanConstants.NAME) == null || descr.getFieldValue(ModelMBeanConstants.NAME).equals(""))
         descr.setField(ModelMBeanConstants.NAME, className);
      if (descr.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE) == null)
         descr.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.MBEAN_DESCRIPTOR);
      if (!(((String)descr.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE)).equalsIgnoreCase(ModelMBeanConstants.MBEAN_DESCRIPTOR)))
         descr.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.MBEAN_DESCRIPTOR);
      if (descr.getFieldValue(ModelMBeanConstants.DISPLAY_NAME) == null)
         descr.setField(ModelMBeanConstants.DISPLAY_NAME, className);
      if (descr.getFieldValue(ModelMBeanConstants.PERSIST_POLICY) == null)
         descr.setField(ModelMBeanConstants.PERSIST_POLICY, ModelMBeanConstants.NEVER);
      if (descr.getFieldValue(ModelMBeanConstants.LOG) == null)
         descr.setField(ModelMBeanConstants.LOG, "F");
      if (descr.getFieldValue(ModelMBeanConstants.EXPORT) == null)
         descr.setField(ModelMBeanConstants.EXPORT, "F");
      if (descr.getFieldValue(ModelMBeanConstants.VISIBILITY) == null)
         descr.setField(ModelMBeanConstants.VISIBILITY, ModelMBeanConstants.HIGH_VISIBILITY);
   }

   private Descriptor createDefaultDescriptor(String className) {

      return new DescriptorSupport(new String[] {
            ModelMBeanConstants.NAME            + "=" + className,
            ModelMBeanConstants.DESCRIPTOR_TYPE + "=" + ModelMBeanConstants.MBEAN_DESCRIPTOR,
            ModelMBeanConstants.DISPLAY_NAME    + "=" + className,
            ModelMBeanConstants.PERSIST_POLICY  + "=" + ModelMBeanConstants.NEVER,
            ModelMBeanConstants.LOG             + "=" + "F",
            ModelMBeanConstants.EXPORT          + "=" + "F",
            ModelMBeanConstants.VISIBILITY      + "=" + ModelMBeanConstants.HIGH_VISIBILITY
      });
   }

   private Map getAttributeDescriptors()
   {
      Map map = new HashMap();

      for (int i = 0; i < attributes.length; ++i)
      {
         map.put(attributes[i].getName(), ((ModelMBeanAttributeInfo)attributes[i]).getDescriptor());
      }

      return map;
   }

   private Map getOperationDescriptors()
   {
      Map map = new HashMap();

      for (int i = 0; i < operations.length; ++i)
      {
         map.put(operations[i].getName(), ((ModelMBeanOperationInfo)operations[i]).getDescriptor());
      }

      return map;
   }

   private Map getConstructorDescriptors()
   {
      Map map = new HashMap();

      for (int i = 0; i < constructors.length; ++i)
      {
         map.put(constructors[i].getName(), ((ModelMBeanConstructorInfo)constructors[i]).getDescriptor());
      }

      return map;
   }

   private Map getNotificationDescriptors()
   {
      Map map = new HashMap();

      for (int i = 0; i < notifications.length; ++i)
      {
         map.put(notifications[i].getName(), ((ModelMBeanNotificationInfo)notifications[i]).getDescriptor());
      }

      return map;
   }

}




