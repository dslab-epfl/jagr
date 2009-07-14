/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management;

import java.io.Serializable;

/**
 * A representation of an MBean attribute. It is a pair,
 * a {@link #getName() Name} and a {@link #getValue() Value}.<p>
 *
 * An Attribute is returned by a getter operation or passed to a
 * a setter operation.
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 1.1.1.1 $
 *
 */
public class Attribute
   extends Object
   implements Serializable
{
   // Constants -----------------------------------------------------

   // Attributes --------------------------------------------------------

   /**
    * The name of the attribute.
    */
   private String name = null;
   /**
    * The value of the attribute.
    */
   private Object value = null;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Contruct a new attribute given a name and value.
    *
    * @param name the name of the attribute.
    * @param value the value of the attribute.
    */
   public Attribute(String name, Object value)
   {
      this.name = name;
      this.value = value;
   }

   // Public --------------------------------------------------------

   /**
    * Retrieves the name of the attribute.
    *
    * @return the name of the attribute.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Retrieves the value of the attribute.
    *
    * @return the value of the attribute.
    */
   public Object getValue()
   {
      return value;
   }

   /**
    * Compares two attributes for equality.
    *
    * @return true when the name value objects are equal, false otherwise.
    */
    public boolean equals(Object object)
    {
      if (!(object instanceof Attribute))
         return false;
        
      Attribute attr = (Attribute) object;
      
      return (name.equals(attr.getName()) && value.equals(attr.getValue()));
   }

   // Overrides -----------------------------------------------------

   // Implementation ------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
