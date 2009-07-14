/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management.modelmbean;

import java.lang.reflect.Constructor;

import javax.management.Descriptor;
import javax.management.DescriptorAccess;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanParameterInfo;

import org.jboss.mx.modelmbean.ModelMBeanConstants;

/**
 * Represents constructor.
 *
 * @see javax.management.modelmbean.ModelMBeanInfo
 * @see javax.management.modelmbean.ModelMBeanInfoSupport
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *   
 */
public class ModelMBeanConstructorInfo
   extends MBeanConstructorInfo
   implements DescriptorAccess, Cloneable
{

   // Attributes ----------------------------------------------------
   
   /**
    * The descriptor associated with this constructor.
    */
   private Descriptor descriptor = null;
   
   // Constructors --------------------------------------------------
   /**
    * Creates a new constructor info with a default descriptor.
    *
    * @param   description human readable description string
    * @param   constructorMethod a <tt>Constructor</tt> instance representing the MBean constructor
    */
   public ModelMBeanConstructorInfo(String description, Constructor constructorMethod)
   {
      super(description, constructorMethod);
      setDescriptor(createDefaultDescriptor());
   }

   /**
    * Creates a new constructor info with a given descriptor. If a <tt>null</tt> or invalid descriptor
    * is passed as a parameter, a default descriptor will be created for the constructor.
    *
    * @param   description human readable description string
    * @param   constructorMethod a <tt>Constructor</tt> instance representing the MBean constructor
    * @param   descriptor a descriptor to associate with this constructor
    */
   public ModelMBeanConstructorInfo(String description, Constructor constructorMethod, Descriptor descriptor)
   {
      this(description, constructorMethod);
      
      if (descriptor == null || !descriptor.isValid())
         setDescriptor(createDefaultDescriptor());
      else 
         setDescriptor(descriptor);
   }

   /**
    * Creates a new constructor info with default descriptor.
    *
    * @param   name  name for the constructor
    * @param   description human readable description string
    * @param   signature constructor signature
    */
   public ModelMBeanConstructorInfo(String name, String description, MBeanParameterInfo[] signature)
   {
      super(name, description, signature);
      setDescriptor(createDefaultDescriptor());
   }

   /**
    * Creates a new constructor info with a given descriptor. If a <tt>null</tt> or invalid descriptor
    * is passed as a parameter, a default descriptor will be created for the constructor.
    *
    * @param name name for the constructor
    * @param description human readable description string
    * @param signature constructor signature
    * @param descriptor a descriptor to associate with this constructor
    */
   public ModelMBeanConstructorInfo(String name, String description, MBeanParameterInfo[] signature,
                                    Descriptor descriptor)
   {
      this(name, description, signature);
      
      if (descriptor == null || !descriptor.isValid())
         setDescriptor(createDefaultDescriptor());
      else
         setDescriptor(descriptor);
   }
   
   // DescriptorAccess implementation -------------------------------
   
   /**
    * Returns a copy of the descriptor associated with this constructor.
    *
    * @return a copy of this constructor's descriptor instance
    */
   public Descriptor getDescriptor()
   {
      return (Descriptor)descriptor.clone();
   }
   
   /**
    * Replaces the descriptor associated with this constructor. If the <tt>inDescriptor</tt>
    * argument is <tt>null</tt> then the existing descriptor is replaced with a default
    * descriptor.
    *
    * @param   inDescriptor   descriptor used for replacing the existing constructor descriptor
    * @throws  IllegalArgumentException if the new descriptor is not valid
    */
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
   
   public synchronized Object clone() throws CloneNotSupportedException
   {
      ModelMBeanConstructorInfo clone = (ModelMBeanConstructorInfo)super.clone();
      clone.descriptor = (Descriptor)this.descriptor.clone();
      
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
      descr.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.CONSTRUCTOR_DESCRIPTOR);
      descr.setField(ModelMBeanConstants.ROLE, ModelMBeanConstants.CONSTRUCTOR);
      
      // FIXME: check the spec for all mandatory fields!
      
      return descr;
   }
   
}
