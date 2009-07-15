/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

import java.lang.reflect.Method;
import java.io.Serializable;

/**
 * Represents a management attribute in an MBeans' management interface.
 *
 * @see javax.management.MBeanInfo
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *   
 */
public class MBeanAttributeInfo extends MBeanFeatureInfo
   implements Serializable, Cloneable
{

   // Attributes ----------------------------------------------------
   
   /**
    * Attribute type string. This is a fully qualified class name of the type.
    */
   private String type        = null;
   
   /**
    * Is attribute readable.
    */
   private boolean isReadable = false;
   
   /**
    * Is attribute writable.
    */
   private boolean isWritable = false;
   
   /**
    * Is attribute using the boolean <tt>isAttributeName</tt> naming convention.
    */
   private boolean isIs       = false;

   
   // Constructors --------------------------------------------------
   
   /**
    * Creates an MBean attribute info object.
    *
    * @param   name name of the attribute
    * @param   type the fully qualified class name of the attribute's type
    * @param   description human readable description string of the attribute
    * @param   isReadable if attribute is readable
    * @param   isWritable if attribute is writable
    * @param   isIs if attribute is using the boolean <tt>isAttributeName</tt> naming convention for its getter method
    */
   public MBeanAttributeInfo(String name, String type, String description,
                             boolean isReadable, boolean isWritable, boolean isIs)
   {
      super(name, description);
   
      this.type = type;
      this.isReadable = isReadable;
      this.isWritable = isWritable;
      this.isIs = isIs;
   }

   /**
    * Creates an MBean attribute info object using the given accessor methods.
    *
    * @param   name        Name of the attribute.
    * @param   description Human readable description string of the attribute's type.
    * @param   getter      The attribute's read accessor. May be <tt>null</tt> if the attribute is write-only.
    * @param   setter      The attribute's write accessor. May be <tt>null</tt> if the attribute is read-only.
    *
    * @throws  IntrospectionException if the accessor methods are not valid for the attribute
    */
   public MBeanAttributeInfo(String name, String description, Method getter, Method setter)
         throws IntrospectionException
   {
      super(name, description);

      if (getter != null)
      {
         // getter must always be no args method, return type cannot be void
         if (getter.getParameterTypes().length != 0)
            throw new IntrospectionException("Expecting getter method to be of the form 'AttributeType getAttributeName()': found getter with " + getter.getParameterTypes().length + " parameters.");
         if (getter.getReturnType() == Void.TYPE)
            throw new IntrospectionException("Expecting getter method to be of the form 'AttributeType getAttributeName()': found getter with void return type.");
            
         this.isReadable = true;
         
         if (getter.getName().startsWith("is"))
            this.isIs = true;
         
         this.type = getter.getReturnType().getName();
      }

      if (setter != null)
      {
         // setter must have one argument, no less, no more. Return type must be void.
         if (setter.getParameterTypes().length != 1)
            throw new IntrospectionException("Expecting the setter method to be of the form 'void setAttributeName(AttributeType value)': found setter with " + setter.getParameterTypes().length + " parameters.");
         if (setter.getReturnType() != Void.TYPE)
            throw new IntrospectionException("Expecting the setter method to be of the form 'void setAttributeName(AttributeType value)': found setter with " + setter.getReturnType() + " return type.");
            
         this.isWritable = true;

         if (type == null)
         {
            try
            {
               type = setter.getParameterTypes() [0].getName();
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
               throw new IntrospectionException("Attribute setter is lacking type: " + name);
            }
         }

         if (!(type.equals(setter.getParameterTypes() [0].getName())))
            throw new IntrospectionException("Attribute type mismatch: " + name);
      }
   }

   
   // Public --------------------------------------------------------

   /**
    * Returns the type string of this attribute.
    *
    * @return fully qualified class name of the attribute's type
    */
   public String getType()
   {
      return type;
   }
   
   /**
    * If the attribute is readable.
    *
    * @return true if attribute is readable; false otherwise
    */
   public boolean isReadable()
   {
      return isReadable;
   }

   /**
    * If the attribute is writable.
    *
    * @return true if attribute is writable; false otherwise
    */
   public boolean isWritable()
   {
      return isWritable;
   }

   /**
    * If the attribute is using the boolean <tt>isAttributeName</tt> naming convention
    * for its read accessor.
    *
    * @param   true if using <tt>isAttributeName</tt> getter; false otherwise
    */
   public boolean isIs()
   {
      return isIs;
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
      MBeanAttributeInfo clone = (MBeanAttributeInfo)super.clone();
      clone.type        = this.type;
      clone.isReadable  = this.isReadable;
      clone.isWritable  = this.isWritable;
      clone.isIs        = this.isIs;

      return clone;
   }

}


