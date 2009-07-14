/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management.modelmbean;

import org.jboss.mx.modelmbean.ModelMBeanConstants;

import javax.management.MBeanNotificationInfo;
import javax.management.Descriptor;
import javax.management.DescriptorAccess;
/**
 * Represents a notification in a Model MBean's management interface.
 *
 * @see javax.management.modelmbean.ModelMBeanInfo
 * @see javax.management.modelmbean.ModelMBeanAttributeInfo
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 */
public class ModelMBeanNotificationInfo
         extends MBeanNotificationInfo
         implements DescriptorAccess, Cloneable
{

   // Attributes ----------------------------------------------------
   private Descriptor descriptor = null;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public ModelMBeanNotificationInfo(String[] notifTypes, String name, String description)
   {
      super(notifTypes, name, description);
      setDescriptor(createDefaultDescriptor());
   }

   public ModelMBeanNotificationInfo(String[] notifTypes, String name, String description,
                                     Descriptor descriptor)
   {
      this(notifTypes, name, description);

      if (descriptor == null || !descriptor.isValid())
         setDescriptor(createDefaultDescriptor());
      else
         setDescriptor(descriptor);
   }

   public ModelMBeanNotificationInfo(ModelMBeanNotificationInfo info)
   {
      this(info.getNotifTypes(), info.getName(), info.getDescription(), info.getDescriptor());
   }

   // Public --------------------------------------------------------
   public Descriptor getDescriptor()
   {
      return (Descriptor)descriptor.clone();
   }

   public void setDescriptor(Descriptor inDescriptor)
   {
      if (inDescriptor == null)
         inDescriptor = createDefaultDescriptor();

      if (!inDescriptor.isValid())
         // FIXME: give more detailed error
         throw new IllegalArgumentException("Invalid descriptor.");

      this.descriptor = inDescriptor;
   }


   // Cloneable implementation --------------------------------------
   public Object clone() throws CloneNotSupportedException
   {
      ModelMBeanNotificationInfo clone = (ModelMBeanNotificationInfo)super.clone();
      clone.descriptor  = (Descriptor)this.descriptor.clone();

      return clone;
   }

   // Object overrides ----------------------------------------------
   public String toString()
   {
      // FIXME: human readable string
      return super.toString();
   }

   // Private -------------------------------------------------------
   private Descriptor createDefaultDescriptor()
   {
      DescriptorSupport descr = new DescriptorSupport();
      descr.setField(ModelMBeanConstants.NAME, super.getName());
      descr.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.NOTIFICATION_DESCRIPTOR);
      return descr;
   }

}




