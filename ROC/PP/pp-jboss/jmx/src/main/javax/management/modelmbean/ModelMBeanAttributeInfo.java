/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management.modelmbean;

import java.lang.reflect.Method;

import javax.management.Descriptor;
import javax.management.DescriptorAccess;
import javax.management.MBeanAttributeInfo;
import javax.management.IntrospectionException;

import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.logging.Logger;

/**
 * Represents a Model MBean's management attribute.
 *
 * @see javax.management.MBeanAttributeInfo
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020320 Juha Lindfors:</b>
 * <ul>
 * <li>toString() implementation</li>
 * </ul>
 */
public class ModelMBeanAttributeInfo
   extends MBeanAttributeInfo
   implements DescriptorAccess, Cloneable
{

   // Attributes ----------------------------------------------------
   /**
    * The descriptor associated with this attribute.
    */
   private Descriptor descriptor = null;

   // Static --------------------------------------------------------

   private static final Logger log = Logger.getLogger(ModelMBeanAttributeInfo.class);

   // Constructors --------------------------------------------------
   /**
    * Creates a new attribute info with a default descriptor.
    *
    * @param  name name of the attribute
    * @param  description human readable description string
    * @param  getter a <tt>Method</tt> instance representing a read method for this attribute
    * @param  setter a <tt>Method</tt> instance representing a write method for this attribute
    *
    * @throws IntrospectionException if the accessor methods are not valid for this attribute
    */
   public ModelMBeanAttributeInfo(String name, String description, Method getter, Method setter)
         throws IntrospectionException
   {
      super(name, description, getter, setter);
      setDescriptor(createDefaultDescriptor());
   }

   /**
    * Creates a new attribute info object. If a <tt>null</tt> or
    * invalid descriptor is passed as a parameter, a default descriptor will be created
    * for the attribute.
    *
    * @param  name name of the attribute
    * @param  description human readable description string
    * @param  getter a <tt>Method</tt> instance representing a read method for this attribute
    * @param  setter a <tt>Method</tt> instance representing a write method for this attribute
    * @param  descriptor a descriptor to associate with this attribute
    *
    * @throws IntrospectionException if the accessor methods are not valid for this attribute
    */
   public ModelMBeanAttributeInfo(String name, String description, Method getter, Method setter, Descriptor descriptor)
         throws IntrospectionException
   {
      this(name, description, getter, setter);

      if (descriptor == null || !descriptor.isValid())
         setDescriptor(createDefaultDescriptor());
      else
         setDescriptor(descriptor);
   }

   /**
    * Creates a new attribute info object with a default descriptor.
    *
    * @param   name  name of the attribute
    * @param   type  fully qualified class name of the attribute's type
    * @param   description human readable description string
    * @param   isReadable true if attribute is readable; false otherwise
    * @param   isWritable true if attribute is writable; false otherwise
    * @param   isIs (not used for Model MBeans; false)
    */
   public ModelMBeanAttributeInfo(String name, String type, String description,
                                  boolean isReadable, boolean isWritable, boolean isIs)
   {
      // JPL:  As far as I can tell, the isIs boolean has no use in the Model MBean
      //       attribute info (since attributes will map to methods through operations)
      //       I'm setting this boolean to false, until someone complains.

      super(name, type, description, isReadable, isWritable, false /*isIs*/);
      setDescriptor(createDefaultDescriptor());

      if (isIs == true)
         log.warn("WARNING: supplied isIS=true, set to false");
   }

   /**
    * Creates a new attribute info object with a given descriptor. If a <tt>null</tt> or invalid
    * descriptor is passed as a parameter, a default descriptor will be created for the attribute.
    *
    * @param  name   name of the attribute
    * @param  type   fully qualified class name of the attribute's type
    * @param  description human readable description string
    * @param  isReadable true if the attribute is readable; false otherwise
    * @param  isWritable true if the attribute is writable; false otherwise
    * @param  isIs  (not used for Model MBeans; false)
    */
   public ModelMBeanAttributeInfo(String name, String type, String description,
                                  boolean isReadable, boolean isWritable, boolean isIs, Descriptor descriptor)
   {
      // JPL:  As far as I can tell, the isIs boolean has no use in the Model MBean
      //       attribute info (since attributes will map to methods through operations)
      //       I'm setting this boolean to false, until someone complains.
      super(name, type, description, isReadable, isWritable, false/*isIs*/);

      if (descriptor == null || !descriptor.isValid())
         setDescriptor(createDefaultDescriptor());
      else
         setDescriptor(descriptor);

      if (isIs == true)
         log.warn("WARNING: supplied isIS=true, set to false");
   }

   /**
    * Copy constructor.
    *
    * @param   inInfo the attribute info to copy
    */
   public ModelMBeanAttributeInfo(ModelMBeanAttributeInfo info)
   {
      // THS - javadoc says a default descriptor will be created but that's not
      // consistent with the other *Info classes.
      // I'm also assuming that getDescriptor returns a clone.
      this(info.getName(), info.getType(), info.getDescription(), info.isReadable(),
           info.isWritable(), info.isIs(), info.getDescriptor());
   }

   // DescriptorAccess implementation -------------------------------
   /**
    * Returns a copy of the descriptor associated with this attribute.
    *
    * @return a copy of this attribute's descriptor
    */
   public Descriptor getDescriptor()
   {
      return (Descriptor)descriptor.clone();
   }

   /**
    * Replaces the descriptor associated with this attribute. If the <tt>inDescriptor</tt>
    * argument is <tt>null</tt> then the existing descriptor is replaced with a default
    * descriptor.
    *
    * @param   inDescriptor   descriptor used for replacing the existing operation descriptor
    * @throws IllegalArgumentException if the new descriptor is not valid
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
   /**
    * Creates a copy of this object.
    *
    * @return clone of this object
    * @throws CloneNotSupportedException if there was a failure creating the copy
    */
   public synchronized Object clone() throws CloneNotSupportedException
   {
      ModelMBeanAttributeInfo clone = (ModelMBeanAttributeInfo)super.clone();
      clone.descriptor  = (Descriptor)this.descriptor.clone();

      return clone;
   }

   // Object override -----------------------------------------------
   /**
    * Returns a string representation of this Model MBean attribute info object.
    * The returned string is in the form: <pre>
    *
    *   ModelMBeanAttributeInfo[Name=&lt;attribute name&gt;,
    *   Type=&lt;class name of the attribute type&gt;,
    *   Access= RW | RO | WO,
    *   Descriptor=(fieldName1=fieldValue1, ... , fieldName&lt;n&gt;=fieldValue&lt;n&gt;)]
    *
    * </pre>
    *
    * @return string representation of this object
    */
   public String toString()
   {
      return "ModelMBeanAttributeInfo[" +
             "Name=" + getName() +
             ",Type=" + getType() +
             ",Access=" + ((isReadable() && isWritable()) ? "RW" : (isReadable()) ? "RO" : "WO") +
             ",Descriptor(" + getDescriptor() + ")]";
   }

   // Private -------------------------------------------------------
   private Descriptor createDefaultDescriptor()
   {
      DescriptorSupport descr = new DescriptorSupport();
      descr.setField(ModelMBeanConstants.NAME, super.getName());
      descr.setField(ModelMBeanConstants.DESCRIPTOR_TYPE, ModelMBeanConstants.ATTRIBUTE_DESCRIPTOR);

      // FIXME: check the spec for all required descriptor fields!

      return descr;
   }

}

