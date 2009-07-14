/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.management.relation;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A role is a role name and an ordered list of object names to
 * the MBeans in the role.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class Role
  implements Serializable
{
   // Attributes ----------------------------------------------------

   /**
    * The role name
    */
   private String name;

   /**
    * An ordered list of MBean object names.
    */
   private ArrayList value;

   // Static --------------------------------------------------------

   /**
    * Formats the role value for output.<p>
    *
    * The spec says it should be a comma separated list of object names.
    * But the RI uses new lines which makes more sense for object names.
    *
    * @param roleValue the role value to print
    * @return the string representation
    * @exception IllegalArgumentException for null value.
    */
   public static String roleValueToString(List roleValue)
     throws IllegalArgumentException
   {
     if (roleValue == null)
       throw new IllegalArgumentException("null roleValue");
     StringBuffer buffer = new StringBuffer();
     Iterator iterator = roleValue.iterator(); 
     while (iterator.hasNext())
     {
       buffer.append(iterator.next());
       if (iterator.hasNext())
         buffer.append("\n");
     }
     return buffer.toString();
   }

   // Constructors --------------------------------------------------

   /**
    * Construct a new role.<p>
    *
    * No validation is performed until the role is set of in a
    * relation. Passed parameters must not be null.<p>
    * 
    * The passed list must be an ArrayList.
    *
    * @param roleName the role name
    * @param roleValue the MBean object names in the role
    * @exception IllegalArgumentException for null values.
    */
   public Role(String roleName, List roleValue)
     throws IllegalArgumentException
   {
     setRoleName(roleName);
     setRoleValue(roleValue); 
   }

   // Public ---------------------------------------------------------

   /**
    * Retrieve the role name.
    * 
    * @return the role name.
    */
   public String getRoleName()
   {
     return name;
   }

   /**
    * Retrieve the role value.
    * 
    * @return a list of MBean object names.
    */
   public List getRoleValue()
   {
     return new ArrayList(value);
   }

   /**
    * Set the role name.
    * 
    * @param roleName the role name.
    * @exception IllegalArgumentException for a null value
    */
   public void setRoleName(String roleName)
     throws IllegalArgumentException
   {
     if (roleName == null)
       throw new IllegalArgumentException("Null roleName");
     name = roleName;
   }

   /**
    * Set the role value it must be an ArrayList.
    * A list of mbean object names.
    * 
    * @param roleValue the role value.
    * @exception IllegalArgumentException for a null value or not an
    *            array list
    */
   public void setRoleValue(List roleValue)
     throws IllegalArgumentException
   {
     if (roleValue == null)
       throw new IllegalArgumentException("Null roleValue");
     value = new ArrayList(roleValue);
   }

   // Object Overrides -------------------------------------------------

   /**
    * Clones the object.
    *
    * @info.todo shouldn't use the copy constructor?
    *
    * @return a copy of the role
    */
   public Object clone()
   {
     return new Role(name, value);
   }

   /**
    * Formats the role for output.
    *
    * @return a human readable string
    */
   public String toString()
   {
     StringBuffer buffer = new StringBuffer("Role Name (");
     buffer.append(name);
     buffer.append(") Object Names (");
     Iterator iterator = value.iterator(); 
     while (iterator.hasNext())
     {
       buffer.append(iterator.next());
       if (iterator.hasNext())
         buffer.append(" & ");
     }
     buffer.append(")");
     return buffer.toString();
   }
}

